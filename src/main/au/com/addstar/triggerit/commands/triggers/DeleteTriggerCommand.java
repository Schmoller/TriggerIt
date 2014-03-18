package au.com.addstar.triggerit.commands.triggers;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.TriggerManager;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.commands.CommandSenderType;
import au.com.addstar.triggerit.commands.ICommand;

public class DeleteTriggerCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "delete";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "remove", "rm" };
	}

	@Override
	public String getPermission()
	{
		return null;
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <trigger>";
	}

	@Override
	public String getDescription()
	{
		return "Removes a trigger";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Console, CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if(args.length != 1)
			return false;
		
		TriggerManager manager = TriggerItPlugin.getInstance().getTriggerManager();
		
		Trigger trigger = manager.getTrigger(args[0]);
		if(trigger == null)
			throw new BadArgumentException(0, "Unknown trigger");
		
		manager.removeTrigger(trigger);
		
		sender.sendMessage(ChatColor.GOLD + "Trigger " + trigger.getName() + " was removed");
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
