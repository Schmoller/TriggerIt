package au.com.addstar.triggerit.commands.actions;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.command.CommandSender;

import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.TriggerManager;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.commands.CommandSenderType;
import au.com.addstar.triggerit.commands.ICommand;

public class ClearActionsCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "clear";
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
		return label + " <trigger>";
	}

	@Override
	public String getDescription()
	{
		return "Clears all actions from a trigger";
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
		
		TriggerManager triggers = TriggerItPlugin.getInstance().getTriggerManager();
		
		Trigger trigger = triggers.getTrigger(args[0]);
		if(trigger == null)
			throw new BadArgumentException(0, "No trigger by that name exists");
		
		trigger.clearActions();
		triggers.saveTrigger(trigger);
		sender.sendMessage("Actions cleared");
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
