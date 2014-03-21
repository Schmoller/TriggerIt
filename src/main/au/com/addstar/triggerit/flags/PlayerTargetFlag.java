package au.com.addstar.triggerit.flags;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableSet;

import au.com.addstar.triggerit.TriggerIt;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.targets.Target;

public class PlayerTargetFlag extends Flag<Target<? extends CommandSender>>
{
	@Override
	public Target<? extends CommandSender> parse( Player sender, String[] args ) throws IllegalArgumentException, BadArgumentException
	{
		if(args.length != 1)
			throw new IllegalArgumentException("<target>");
		
		try
		{
			return TriggerIt.parseTargets(args[0], CommandSender.class, ImmutableSet.of(Player.class));
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
		section.set("value", value.toString());
	}

	@Override
	public void read( ConfigurationSection section ) throws InvalidConfigurationException
	{
		value = TriggerIt.parseTargets(section.getString("value"), CommandSender.class, ImmutableSet.of(Player.class));
	}

	@Override
	public String getValueString()
	{
		return value.describe();
	}

}
