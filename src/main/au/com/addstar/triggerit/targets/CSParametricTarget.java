package au.com.addstar.triggerit.targets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

public class CSParametricTarget extends TargetCS
{
	private String mArgument;
	private Map<String, Object> mArguments;
	
	public CSParametricTarget(String argument)
	{
		mArgument = argument;
	}
	
	protected CSParametricTarget() {}
	
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
		
		if(argument instanceof CommandSender)
			return Arrays.asList((CommandSender)argument);
		else if(argument instanceof Collection<?>)
		{
			Collection<?> col = (Collection<?>)argument;
			if(!col.isEmpty())
			{
				Object ent1 = col.iterator().next();
				if(ent1 instanceof CommandSender)
					return new ArrayList<CommandSender>((Collection<? extends CommandSender>)col);
			}
		}
		
		return Collections.emptyList();
	}

	@Override
	protected void load( ConfigurationSection section ) throws InvalidConfigurationException
	{
		mArgument = section.getString("argument");
	}
	
	@Override
	public void save( ConfigurationSection section )
	{
		super.save(section);
		section.set("argument", mArgument);
	}
	
	@Override
	public String describe()
	{
		return "Player resolved from argument '" + mArgument + "'";
	}
}