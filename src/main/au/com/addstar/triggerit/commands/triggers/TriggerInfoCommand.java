package au.com.addstar.triggerit.commands.triggers;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.commands.CommandSenderType;
import au.com.addstar.triggerit.commands.ICommand;

public class TriggerInfoCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "info";
	}

	@Override
	public String[] getAliases()
	{
		return new String [] {"describe"};
	}

	@Override
	public String getPermission()
	{
		return null;
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <name>";
	}

	@Override
	public String getDescription()
	{
		return "Describes a trigger";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if(args.length != 1)
			return false;
		
		Trigger trigger = TriggerItPlugin.getInstance().getTriggerManager().getTrigger(args[0]);
		if(trigger == null)
			throw new BadArgumentException(0, "Unknown trigger");
		
		List<String> description = trigger.describe();
		
		for(String line : description)
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));

		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
