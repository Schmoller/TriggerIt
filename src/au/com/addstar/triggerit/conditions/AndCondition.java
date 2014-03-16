package au.com.addstar.triggerit.conditions;

import java.util.Map;

public class AndCondition extends Condition
{
	private Condition mLeft;
	private Condition mRight;
	
	public AndCondition(Condition left, Condition right)
	{
		mLeft = left;
		mRight = right;
	}
	
	@Override
	public boolean isTrue( Map<String, Object> arguments )
	{
		return mLeft.isTrue(arguments) && mRight.isTrue(arguments);
	}

	@Override
	public String toString()
	{
		return "(" + mLeft.toString() + " and " + mRight.toString() + ")";
	}

}
