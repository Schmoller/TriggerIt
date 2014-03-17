package au.com.addstar.triggerit.commands.actions;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.addstar.triggerit.Action;
import au.com.addstar.triggerit.ActionManager;
import au.com.addstar.triggerit.ActionManager.ActionDefinition;
import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.TriggerManager;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.commands.CommandSenderType;
import au.com.addstar.triggerit.commands.ICommand;

public class AddActionCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "add";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "triggerit.command.action.add";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <trigger> <type> <arguments>";
	}

	@Override
	public String getDescription()
	{
		return "Adds an action to the specified trigger";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Console, CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if(args.length < 2)
			return false;
		
		TriggerManager triggers = TriggerItPlugin.getInstance().getTriggerManager();
		ActionManager manager = TriggerItPlugin.getInstance().getActionManager();
		
		
		Trigger trigger = triggers.getTrigger(args[0]);
		if(trigger == null)
			throw new BadArgumentException(0, "No trigger by that name exists");
		
		ActionDefinition def = manager.getType(args[1]);
		
		if(def == null)
			throw new BadArgumentException(1, "Unknown action type");
		
		try
		{
			Action action = def.newAction(sender, Arrays.copyOfRange(args, 2, args.length));
			trigger.addAction(action);
			triggers.saveTrigger(trigger);
			sender.sendMessage(ChatColor.GREEN + "Action added to " + trigger.getName());
		}
		catch(BadArgumentException e)
		{
			e.setArgument(e.getArgument()+2);
			throw e;
		}
		catch(IllegalStateException e)
		{
			throw new IllegalArgumentException("Usage: " + parent + label + " <trigger> <type> " + e.getMessage());
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length == 2)
			return Utilities.matchString(args[1], TriggerItPlugin.getInstance().getActionManager().getTypeNames());
		else if(args.length > 2)
		{
			ActionDefinition def = TriggerItPlugin.getInstance().getActionManager().getType(args[1]);
			if(def != null)
				return def.tabComplete(sender, Arrays.copyOfRange(args, 2, args.length));
		}
		return null;
	}

}
