package au.com.addstar.triggerit;

import java.util.List;

public class Placeholder
{
	public final int start;
	public final int end;
	
	private List<? extends Object> mList;
	private int mIndex;
	
	public Placeholder(int start, int end, List<? extends Object> list)
	{
		this.start = start;
		this.end = end;
		mList = list;
		mIndex = 0;
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
	
	public Placeholder copy()
	{
		return new Placeholder(start, end, mList);
	}
}
