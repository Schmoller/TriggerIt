package au.com.addstar.triggerit.commands.actions;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.addstar.triggerit.Action;
import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.TriggerManager;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.commands.CommandSenderType;
import au.com.addstar.triggerit.commands.ICommand;

public class RemoveActionCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "remove";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "delete", "rm" };
	}

	@Override
	public String getPermission()
	{
		return null;
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <trigger> <index>";
	}

	@Override
	public String getDescription()
	{
		return "Removes an action from a trigger.";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Console, CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if(args.length != 2)
			return false;
		
		TriggerManager manager = TriggerItPlugin.getInstance().getTriggerManager();
		Trigger trigger = manager.getTrigger(args[0]);
		if(trigger == null)
			throw new BadArgumentException(0, "Unknown trigger");
		
		int index;
		
		try
		{
			index = Integer.parseInt(args[1]);
			if(index < 0)
				throw new BadArgumentException(1, "Index must be an integer 0 or higher");
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(1, "Index must be an integer 0 or higher");
		}
		
		int size = trigger.getActions().size();
		if(index >= size)
			throw new BadArgumentException(1, "Index is too high. Highest index can be " + (size - 1));
		
		Action action = trigger.getActions().get(index);
		
		trigger.removeAction(index);
		
		sender.sendMessage(ChatColor.GOLD + "Removed action '" + action.toString() + "' from trigger " + trigger.getName());
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
