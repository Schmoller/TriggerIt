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
	
	private static Pattern mPattern = Pattern.compile("(@@)|@([a-zA-Z0-9_]+(?:\\.[a-zA-Z0-9_]+)*)");
	
	public static String replaceArguments(String str, Map<String, Object> arguments, ArgumentProvider provider, ITextifier textifier)
	{
		Matcher match = mPattern.matcher(str);
		
		StringBuffer buffer = new StringBuffer();
		while(match.find())
		{
			if(match.group(1) != null)
				match.appendReplacement(buffer, "@");
			else
			{
				String[] parts = match.group(2).split("\\.");
				
				Object obj = null;
				for(int i = 0; i < parts.length; ++i)
				{
					if(i == 0)
						obj = arguments.get(parts[i]);
					else
					{
						Map<String, Object> args = provider.provide(obj);
						if(args != null)
							obj = args.get(parts[i]);
						else
							obj = null;
					}
					
					if(obj == null)
						break;
				}
					
				if(textifier != null && obj != null)
					obj = textifier.asString(obj);
				
				match.appendReplacement(buffer, (obj != null ? obj.toString() : match.group(0)));
			}
		}
		
		match.appendTail(buffer);
		
		return buffer.toString();
	}
	
	private static Pattern mValidNameChars = Pattern.compile("[a-zA-Z0-9_\\-]+");
	
	public static boolean isValidName(String name)
	{
		Matcher m = mValidNameChars.matcher(name);
		return m.matches();
	}
}
