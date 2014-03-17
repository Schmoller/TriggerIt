package au.com.addstar.triggerit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringWithPlaceholders implements Iterable<String>
{
	private String mBase;
	private ArrayList<Placeholder> mPlaceholders;
	private ITextifier mTextifier;
	
	private StringWithPlaceholders(String base, ArrayList<Placeholder> placeholders, ITextifier textifier)
	{
		mBase = base;
		mPlaceholders = placeholders;
		mTextifier = textifier;
	}
	
	@Override
	public Iterator<String> iterator()
	{
		return new PlaceholderIterator();
	}
	
	private static Pattern mPattern = Pattern.compile("@\\[([a-zA-Z0-9_]+)\\]");
	public static StringWithPlaceholders from(String string, Map<String, Object> arguments, ITextifier textifier)
	{
		ArrayList<Placeholder> placeholders = new ArrayList<Placeholder>();
		Matcher m = mPattern.matcher(string);
		
		while(m.find())
		{
			String arg = m.group(1);
			Object val = arguments.get(arg);
			if(val instanceof Collection<?>)
			{
				List<? extends Object> results = new ArrayList<Object>((Collection<?>)val);
				
				if(!results.isEmpty())
					placeholders.add(new Placeholder(m.start(), m.end(), results));
			}
		}
		
		if(placeholders.isEmpty())
			return null;
		
		return new StringWithPlaceholders(string, placeholders, textifier);
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
				
				cmd.append(mTextifier.asString(placeholder.getCurrent()));
			}
			
			cmd.append(mBase.substring(lastPos));
			
			// Increment placeholders
			for(int i = 0; i < mPlaceholderCoppies.size(); ++i)
			{
				Placeholder placeholder = mPlaceholderCoppies.get(i);
				
				if(placeholder.next())
					break;
			
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
