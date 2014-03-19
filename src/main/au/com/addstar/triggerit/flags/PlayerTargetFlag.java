package au.com.addstar.triggerit.flags;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.targets.Target;
import au.com.addstar.triggerit.targets.TargetCS;

public class PlayerTargetFlag extends Flag<TargetCS>
{
	@Override
	public TargetCS parse( Player sender, String[] args ) throws IllegalArgumentException, BadArgumentException
	{
		if(args.length != 1)
			throw new IllegalArgumentException("<target>");
		
		try
		{
			return TargetCS.parseTargets(args[0], false);
		}
		catch(IllegalArgumentException e)
		{
			throw new BadArgumentException(0, e.getMessage());
		}
	}

	@Override
	public List<String> tabComplete( Player sender, String[] args )
	{
		return null;
	}

	@Override
	public void save( ConfigurationSection section )
	{
		value.save(section);
	}

	@Override
	public void read( ConfigurationSection section ) throws InvalidConfigurationException
	{
		value = (TargetCS)Target.loadTarget(section);
	}

	@Override
	public String getValueString()
	{
		return value.describe();
	}

}
