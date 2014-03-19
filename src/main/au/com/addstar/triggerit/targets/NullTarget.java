package au.com.addstar.triggerit.targets;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import au.com.addstar.triggerit.NullCommandSender;

public class NullTarget extends TargetCS
{
	@Override
	public List<? extends CommandSender> getTargets()
	{
		return Arrays.asList(NullCommandSender.instance);
	}

	@Override
	protected void load( ConfigurationSection section ) throws InvalidConfigurationException
	{
	}

	@Override
	public String describe()
	{
		return "Null Sender";
	}
	
}
