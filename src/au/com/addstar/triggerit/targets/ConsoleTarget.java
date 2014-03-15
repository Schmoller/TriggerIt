package au.com.addstar.triggerit.targets;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

public class ConsoleTarget extends TargetCS
{
	@Override
	public List<? extends CommandSender> getTargets()
	{
		return Arrays.asList((CommandSender)Bukkit.getConsoleSender());
	}
	
	@Override
	protected void load( ConfigurationSection section ) throws InvalidConfigurationException
	{
	}
	
	@Override
	public void save( ConfigurationSection section )
	{
		super.save(section);
	}

	@Override
	public String describe()
	{
		return "Console";
	}
}
