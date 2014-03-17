package au.com.addstar.triggerit.conditions;

import java.util.Map;

import au.com.addstar.triggerit.BasicArgumentProvider;
import au.com.addstar.triggerit.BasicTextifier;
import au.com.addstar.triggerit.Utilities;

public class ComparisonCondition extends Condition
{
	public enum Operator
	{
		Exists,
		Equals,
		NotEquals,
		Less,
		Greater,
		LessEqual,
		GreaterEqual
	}
	private String mArgument;
	private Operator mOperator;
	
	private Object mValue;
	
	public ComparisonCondition(String argument, Operator op, String value)
	{
		mArgument = argument;
		mOperator = op;
		
		if(value == null)
		{
			mValue = null;
			return;
		}
		
		try
		{
			mValue = Long.parseLong(value);
			return;
		}
		catch(NumberFormatException e)
		{
		}
		
		try
		{
			mValue = Double.parseDouble(value);
			return;
		}
		catch(NumberFormatException e)
		{
		}
		
		mValue = value;
	}
	
	@Override
	public boolean isTrue( Map<String, Object> arguments )
	{
		Object argVal = Utilities.getArgument(mArgument, arguments, BasicArgumentProvider.instance);
		String argValString = (argVal != null ? BasicTextifier.instance.asString(argVal).toLowerCase() : null);
		
		int comparison = 0;
		
		if(argVal instanceof Double || argVal instanceof Float)
		{
			double number = ((Number) argVal).doubleValue();
			double number2;
			
			if(mValue instanceof Number)
			{
				number2 = ((Number) mValue).doubleValue();
				comparison = Double.valueOf(number).compareTo(number2);
			}
			else
				comparison = argValString.compareTo((String)mValue);
		}
		else if(argVal instanceof Number)
		{
			long number = ((Number) argVal).longValue();
			long number2;
			
			if(mValue instanceof Number)
			{
				number2 = ((Number) mValue).longValue();
				comparison = Long.valueOf(number).compareTo(number2);
			}
			else
				comparison = argValString.compareTo((String)mValue);
		}
		else if(argVal != null)
			comparison = argValString.compareTo((String)mValue);
		
		
		switch(mOperator)
		{
		case Exists:
			return argVal != null;
		case Equals:
			if(argVal instanceof Number)
				return comparison == 0;
			else
				return argValString.equalsIgnoreCase(String.valueOf(mValue));
		case NotEquals:
			if(argVal instanceof Number)
				return comparison != 0;
			else
				return !argValString.equalsIgnoreCase(String.valueOf(mValue));
		case Less:
			return comparison == -1;
		case Greater:
			return comparison == 1;
		case GreaterEqual:
			return comparison >= 0;
		case LessEqual:
			return comparison <= 0;
		}
		
		return false;
	}

	@Override
	public String toString()
	{
		switch(mOperator)
		{
		case Equals:
			return String.format("@%s=%s", mArgument, mValue);
		case Exists:
			return String.format("@%s", mArgument);
		case NotEquals:
			return String.format("@%s!=%s", mArgument, mValue);
		case Greater:
			return String.format("@%s>%s", mArgument, mValue);
		case GreaterEqual:
			return String.format("@%s>=%s", mArgument, mValue);
		case Less:
			return String.format("@%s<%s", mArgument, mValue);
		case LessEqual:
			return String.format("@%s<=%s", mArgument, mValue);
		}
		return "";
	}

}
