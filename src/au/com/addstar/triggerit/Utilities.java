package au.com.addstar.triggerit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
}
