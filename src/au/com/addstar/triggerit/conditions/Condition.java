package au.com.addstar.triggerit.conditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import au.com.addstar.triggerit.conditions.ComparisonCondition.Operator;

public abstract class Condition
{
	public abstract boolean isTrue(Map<String, Object> arguments);
	
	public abstract String toString();
	
	private static int getCharType(char c)
	{
		if(Character.isLetter(c) || Character.isDigit(c) || c == '@' || c == '_')
			return 0;
		else
			return 1;
	}
	
	@SuppressWarnings( "unchecked" )
	private static List<Object>[] tokenize(String string, int start)
	{
		ArrayList<Object> tokens = new ArrayList<Object>();
		ArrayList<Object> locations = new ArrayList<Object>();
		int depth = 0;
		int tokenStart = 0;
		int lastType = 0;
		
		for(int pos = 0; pos < string.length(); ++pos)
		{
			char c = string.charAt(pos);
			if(c == '(')
			{
				if(depth == 0)
					tokenStart = pos+1;
				++depth;
			}
			else if(c == ')')
			{
				--depth;
				if(depth == 0)
				{
					Object[] res = tokenize(string.substring(tokenStart, pos), start + tokenStart); 
					tokens.add(res[0]);
					locations.add(res[1]);
					tokenStart = pos+1;
				}
			}
			else if(depth == 0)
			{
				int type = getCharType(c);
				
				if(c == ' ')
				{
					if(pos - tokenStart > 0)
					{
						tokens.add(string.substring(tokenStart, pos));
						locations.add(start + tokenStart);
					}
					tokenStart = pos+1;
				}
				else if(type != lastType)
				{
					if(pos - tokenStart > 0)
					{
						tokens.add(string.substring(tokenStart, pos));
						locations.add(start + tokenStart);
					}
					tokenStart = pos;
					lastType = type;
				}
			}
		}
		
		if(string.length() - tokenStart > 0)
		{
			tokens.add(string.substring(tokenStart));
			locations.add(start + tokenStart);
		}
		
		return new List[] {tokens, locations};
	}
	
	@SuppressWarnings( "unchecked" )
	private static int getTokenLocation(int token, int start, List<Object> locations)
	{
		int cur = start;
		for(int i = 0; i < locations.size(); ++i)
		{
			Object loc = locations.get(i);
			if(loc instanceof List)
			{
				int tokenLoc = getTokenLocation(token, cur, (List<Object>)loc);
				if(tokenLoc != -1)
					return tokenLoc;
				
				cur += getSizeBefore(Integer.MAX_VALUE, (List<Object>)loc);
			}
			else
			{
				if(cur == token)
					return (Integer)loc;
				++cur;
			}
		}
		
		return -1;
	}
	
	@SuppressWarnings( "unchecked" )
	public static Condition parse(String condition) throws IllegalArgumentException
	{
		List<Object>[] tokenObjects = tokenize(condition.toLowerCase(), 0);
		List<Object> tokens = tokenObjects[0];
		List<Object> locations = tokenObjects[1];

		// If you put parenthesis around the entire thing, this happens
		if(tokens.size() == 1 && tokens.get(0) instanceof List)
		{
			tokens = (List<Object>)tokens.get(0);
			locations = (List<Object>)locations.get(0);
		}
		
		try
		{
			return parseCondition(tokens);
		}
		catch(ConditionParseException e)
		{
			int location = getTokenLocation(e.getToken(), 0, locations);
			throw new IllegalArgumentException(e.getMessage() + " at '" + condition.substring(location, Math.min(condition.length(), location + 10)) + "'");
		}
	}
	
	@SuppressWarnings( "unchecked" )
	private static int getSizeBefore(int index, List<Object> tokens)
	{
		int count = 0;
		for(int i = 0; i < tokens.size(); ++i)
		{
			if(i == index)
				return count;
			
			Object token = tokens.get(i);
			if(token instanceof List)
			{
				count += getSizeBefore(((List<Object>)token).size(), (List<Object>)token);
			}
			else
			{
				++count;
			}
		}
		
		return count;
	}
	
	private static Condition parseCondition(List<Object> tokens) throws ConditionParseException
	{
		try
		{
			return parseCompoundCondition(tokens);
		}
		catch(ConditionParseException e)
		{
			if(!e.isSevere())
				throw e;
		}
		
		try
		{
			return parseComparison(tokens);
		}
		catch(ConditionParseException e)
		{
			if(!e.isSevere())
				throw e;
			
			throw e;
		}
				
	}
	
	@SuppressWarnings( "unchecked" )
	private static Condition parseCompoundCondition(List<Object> tokens) throws ConditionParseException
	{
		Condition left = null;
		Condition right = null;
		boolean and = false;
		
		List<Object> subTokens = new ArrayList<Object>();
		
		boolean usedBrackets = false;
		
		ConditionParseException firstError = null;
		int rightStart = 0;
		
		for(int i = 0; i < tokens.size(); ++i)
		{
			Object token = tokens.get(i);
			if(left == null && (token.equals("and") || token.equals("or")))
			{
				and = token.equals("and");
				
				try
				{
					left = parseCondition(subTokens);
					subTokens = new ArrayList<Object>();
					usedBrackets = false;
					rightStart = i+1;
					continue;
				}
				catch(ConditionParseException e)
				{
					if(firstError == null)
					{
						firstError = e;
						firstError.setToken(e.getToken() + getSizeBefore(i, tokens));
					}
						
				}
			}
			
			if(token instanceof List)
			{
				if(!subTokens.isEmpty())
					throw new ConditionParseException(i, true, "Malformed condition. Brackets found mid condition.");
				subTokens = (List<Object>)token;
				usedBrackets = true;
			}
			else
			{
				if(usedBrackets)
					throw new ConditionParseException(i, true, "Malformed condition. Brackets found mid condition.");
				subTokens.add(token);
			}
		}
		
		if(left == null)
		{
			if(firstError != null)
				throw firstError;
			
			throw new ConditionParseException(0, true, "Not a compound statement");
		}
		
		try
		{
			right = parseCondition(subTokens);
		}
		catch(ConditionParseException e)
		{
			e.setToken(e.getToken() + getSizeBefore(rightStart, tokens));
			throw e;
		}
		
		if(and)
			return new AndCondition(left, right);
		else
			return new OrCondition(left, right);
	}
	
	private static ComparisonCondition parseComparison(List<Object> tokens) throws ConditionParseException
	{
		if(tokens.size() != 1 && tokens.size() != 3)
			throw new ConditionParseException(Math.min(tokens.size()-1, 3), false, "Too many input values");
		
		String left = (String)tokens.get(0);
		String op = null;
		String right = null;
		
		if(tokens.size() == 3)
		{
			op = (String)tokens.get(1);
			right = (String)tokens.get(2);
		}
		
		if(!left.startsWith("@"))
			throw new ConditionParseException(0, false, "Expected argument '" + left + "' to start with @");
		
		left = left.substring(1);
		
		Operator operator = Operator.Exists;
		if(op != null)
		{
			if(op.equals("="))
				operator = Operator.Equals;
			else if(op.equals("!="))
				operator = Operator.NotEquals;
			else
				throw new ConditionParseException(1, false, "Unknown operator " + op);
		}
		
		return new ComparisonCondition(left, operator, right);
	}
}
