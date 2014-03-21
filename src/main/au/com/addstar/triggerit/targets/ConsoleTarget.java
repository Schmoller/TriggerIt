package au.com.addstar.triggerit.targets;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class ConsoleTarget extends Target<CommandSender>
{
	public ConsoleTarget(Set<? extends Class<? extends CommandSender>> specifics)
	{
		super(specifics);
	}
	
	@Override
	public List<? extends CommandSender> getTargets()
	{
		return Arrays.asList((CommandSender)Bukkit.getConsoleSender());
	}
	
	@Override
	public String describe()
	{
		return "Console";
	}
	
	@Override
	public String toString()
	{
		return "~";
	}
}
