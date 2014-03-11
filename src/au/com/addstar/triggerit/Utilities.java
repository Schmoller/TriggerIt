package au.com.addstar.triggerit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities
{
	public static List<String> matchString(String str, Collection<String> possibilities)
	{
		ArrayList<String> matches = new ArrayList<String>();
		str = str.toLowerCase();
		
		for(String possible : possibilities)
		{
			if(possible.toLowerCase().startsWith(str))
				matches.add(possible);
		}
		
		return matches;
	}
	
	private static Pattern mPattern = Pattern.compile("@\\[([a-zA-Z0-9_]+)\\]");
	
	public static String replaceArguments(String str, Map<String, Object> arguments, Action action)
	{
		Matcher match = mPattern.matcher(str);
		
		StringBuffer buffer = new StringBuffer();
		while(match.find())
		{
			Object obj = arguments.get(match.group(1));
			if(action != null)
				obj = action.resolveArgument(obj);
			
			match.appendReplacement(buffer, (obj != null ? obj.toString() : match.group(0)));
		}
		
		match.appendTail(buffer);
		
		return buffer.toString();
	}
}
