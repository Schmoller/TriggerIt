package au.com.addstar.triggerit;

import java.util.List;

public class Placeholder
{
	public final int start;
	public final int end;
	
	private List<? extends Object> mList;
	private int mIndex;
	private Placeholder mLink;
	
	public Placeholder(int start, int end, List<? extends Object> list)
	{
		this.start = start;
		this.end = end;
		mList = list;
		mIndex = 0;
	}
	
	public Placeholder getLink()
	{
		return mLink;
	}
	
	public void setLink(Placeholder holder)
	{
		mLink = holder;
	}
	
	public Object getCurrent()
	{
		return mList.get(mIndex);
	}
	
	public boolean next()
	{
		++mIndex;
		if(mIndex >= mList.size())
		{
			mIndex = 0;
			return false;
		}
		
		return true;
	}
	
	public boolean isEmpty()
	{
		return mList.isEmpty();
	}
	
	@Override
	public boolean equals( Object obj )
	{
		if(!(obj instanceof Placeholder))
			return false;
		
		return ((Placeholder)obj).mList.equals(mList);
	}
	
	public Placeholder copy()
	{
		Placeholder copy = new Placeholder(start, end, mList);
		copy.setLink(mLink);
		return copy;
	}
}
