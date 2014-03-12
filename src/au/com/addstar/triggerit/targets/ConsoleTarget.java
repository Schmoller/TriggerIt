package au.com.addstar.triggerit.targets;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class ConsoleTarget extends TargetCS
{
	@Override
	public List<? extends CommandSender> getTargets()
	{
		return Arrays.asList((CommandSender)Bukkit.getConsoleSender());
	}

}
