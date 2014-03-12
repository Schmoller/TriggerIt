package au.com.addstar.triggerit.targets;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

public class CSParametricTarget extends TargetCS
{
	private String mArgument;
	private Map<String, Object> mArguments;
	
	public CSParametricTarget(String argument)
	{
		mArgument = argument;
	}
	
	@Override
	public void setArgumentMap(Map<String, Object> arguments)
	{
		mArguments = arguments;
	}
	
	
	@Override
	public List<? extends CommandSender> getTargets()
	{
		Validate.notNull(mArguments, "Argument map not set for CSParametricTarget, use setArgumentMap()");
		
		Object argument = mArguments.get(mArgument);
		
		if(argument instanceof CommandSender)
			return Arrays.asList((CommandSender)argument);
		
		return Collections.emptyList();
	}

}
