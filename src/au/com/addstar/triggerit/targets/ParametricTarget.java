package au.com.addstar.triggerit.targets;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

public class ParametricTarget extends Target<Object>
{
	private String mArgument;
	private Map<String, Object> mArguments;
	
	public ParametricTarget(String argument)
	{
		mArgument = argument;
	}
	
	@Override
	public void setArgumentMap(Map<String, Object> arguments)
	{
		mArguments = arguments;
	}
	
	
	@Override
	public List<? extends Object> getTargets()
	{
		Validate.notNull(mArguments, "Argument map not set for ParametricTarget, use setArgumentMap()");
		
		return Arrays.asList(mArguments.get(mArgument));
	}

}
