package au.com.addstar.triggerit.targets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

public class CSParametricTarget extends Target<CommandSender>
{
	private String mArgument;
	private Map<String, Object> mArguments;
	
	public CSParametricTarget(String argument, Set<? extends Class<? extends CommandSender>> specifics)
	{
		super(specifics);
		
		mArgument = argument;
	}
	
	@Override
	public void setArgumentMap(Map<String, Object> arguments)
	{
		mArguments = arguments;
	}
	
	
	@SuppressWarnings( "unchecked" )
	@Override
	public List<? extends CommandSender> getTargets()
	{
		Validate.notNull(mArguments, "Argument map not set for CSParametricTarget, use setArgumentMap()");
		
		Object argument = mArguments.get(mArgument);
		
		if(argument instanceof CommandSender && isValueAllowed((CommandSender)argument))
			return Arrays.asList((CommandSender)argument);
		else if(argument instanceof Collection<?>)
		{
			Collection<?> col = (Collection<?>)argument;
			if(!col.isEmpty())
			{
				Object ent1 = col.iterator().next();
				if(ent1 instanceof CommandSender && isValueAllowed((CommandSender)ent1))
					return new ArrayList<CommandSender>((Collection<? extends CommandSender>)col);
			}
		}
		
		return Collections.emptyList();
	}
	
	@Override
	public String describe()
	{
		return "Player resolved from argument '" + mArgument + "'";
	}
	
	@Override
	public String toString()
	{
		return "@" + mArgument;
	}
}
