package au.com.addstar.triggerit.targets;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandSender;

import au.com.addstar.triggerit.NullCommandSender;

public class NullTarget extends Target<CommandSender>
{
	public NullTarget(Set<? extends Class<? extends CommandSender>> specifics)
	{
		super(specifics);
	}
	
	@Override
	public List<? extends CommandSender> getTargets()
	{
		return Arrays.asList(NullCommandSender.instance);
	}

	@Override
	public String describe()
	{
		return "Null Sender";
	}
	
	@Override
	public String toString()
	{
		return "-";
	}
}
