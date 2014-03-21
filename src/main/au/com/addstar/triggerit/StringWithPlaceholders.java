package au.com.addstar.triggerit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringWithPlaceholders implements Iterable<String>
{
	private String mBase;
	private ArrayList<Placeholder> mPlaceholders;
	
	private StringWithPlaceholders(String base, ArrayList<Placeholder> placeholders)
	{
		mBase = base;
		mPlaceholders = placeholders;
	}
	
	@Override
	public Iterator<String> iterator()
	{
		return new PlaceholderIterator();
	}
	
	private static Pattern mPattern = Pattern.compile("@\\[([a-zA-Z0-9_]+(?:\\.[a-zA-Z0-9_]+)*)\\]");
	public static StringWithPlaceholders from(String string, Map<String, Object> arguments)
	{
		ArrayList<Placeholder> placeholders = new ArrayList<Placeholder>();
		HashMap<String, Placeholder> parentPlaceholders = new HashMap<String, Placeholder>();
		Matcher m = mPattern.matcher(string);
		
		while(m.find())
		{
			String[] parts = m.group(1).split("\\.");
			Object obj = arguments.get(parts[0]);
			
			if(obj instanceof Collection<?>)
			{
				List<Object> results = new ArrayList<Object>((Collection<?>)obj);
				
				for(int o = 0; o < results.size(); ++o)
				{
					Object sub = results.get(o);
					
					for(int i = 1; i < parts.length; ++i)
					{
						Map<String, Object> args = TriggerIt.getArgumentsFor(sub);
						if(args != null)
							sub = args.get(parts[i]);
						else
							sub = null;
					}
					
					results.set(o, sub);
				}
				
				Placeholder holder = new Placeholder(m.start(), m.end(), results);
				Placeholder parent = parentPlaceholders.get(parts[0]); 
				if(parent != null)
					holder.setLink(parent);
				else
					parentPlaceholders.put(parts[0], holder);
					
				placeholders.add(holder);
			}
			else
				placeholders.add(new Placeholder(m.start(), m.end(), Collections.emptyList()));
		}
		
		if(placeholders.isEmpty())
			return null;
		
		return new StringWithPlaceholders(string, placeholders);
	}
	
	private class PlaceholderIterator implements Iterator<String>
	{
		private ArrayList<Placeholder> mPlaceholderCoppies = new ArrayList<Placeholder>();
		private boolean mHasNext;
		
		public PlaceholderIterator()
		{
			for(Placeholder placeholder : mPlaceholders)
				mPlaceholderCoppies.add(placeholder.copy());
			
			mHasNext = !mPlaceholderCoppies.isEmpty();
			
			if(mHasNext)
			{
				for(Placeholder holder : mPlaceholderCoppies)
				{
					if(holder.isEmpty())
					{
						mHasNext = false;
						break;
					}
				}
			}
		}
		
		@Override
		public boolean hasNext()
		{
			return mHasNext;
		}

		@Override
		public String next()
		{
			// Build the string with substituted placeholders
			StringBuilder cmd = new StringBuilder();
			int lastPos = 0;
			for(Placeholder placeholder : mPlaceholderCoppies)
			{
				cmd.append(mBase.substring(lastPos, placeholder.start));
				lastPos = placeholder.end;
				
				cmd.append(TriggerIt.getArgumentString(placeholder.getCurrent()));
			}
			
			cmd.append(mBase.substring(lastPos));
			
			// Increment placeholders
			for(int i = 0; i < mPlaceholderCoppies.size(); ++i)
			{
				Placeholder placeholder = mPlaceholderCoppies.get(i);
				
				if(placeholder.getLink() == null)
				{
					// Progress linked placeholders
					for(Placeholder other : mPlaceholderCoppies)
					{
						if(placeholder.equals(other.getLink()))
							other.next();
					}
					
					if(placeholder.next())
						break;
				}
			
				if(i == mPlaceholderCoppies.size()-1)
				{
					mHasNext = false;
					break;
				}
			}
			
			return cmd.toString();
		}

		@Override
		public void remove()
		{
		}
		
	}
}
