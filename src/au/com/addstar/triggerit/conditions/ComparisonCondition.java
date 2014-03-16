package au.com.addstar.triggerit.conditions;

import java.util.Map;

import au.com.addstar.triggerit.BasicTextifier;

public class ComparisonCondition extends Condition
{
	public enum Operator
	{
		Exists,
		Equals,
		NotEquals
	}
	private String mArgument;
	private Operator mOperator;
	
	private String mValue;
	
	public ComparisonCondition(String argument, Operator op, String value)
	{
		mArgument = argument;
		mOperator = op;
		mValue = value;
	}
	
	@Override
	public boolean isTrue( Map<String, Object> arguments )
	{
		Object argVal = arguments.get(mArgument);
		String argValString = (argVal != null ? BasicTextifier.instance.asString(argVal) : null);
		
		switch(mOperator)
		{
		case Exists:
			return argVal != null;
		case Equals:
			return argValString.equalsIgnoreCase(mValue);
		case NotEquals:
			return !argValString.equalsIgnoreCase(mValue);
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
		}
		return "";
	}

}
