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
import au.com.addstar.triggerit.conditions.Condition;

public class TriggerConditionCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "condition";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return null;
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <trigger> [condition]";
	}

	@Override
	public String getDescription()
	{
		return "Sets the condition for this trigger firing";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.allOf(CommandSenderType.class);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if(args.length < 1)
			return false;
		
		TriggerManager manager = TriggerItPlugin.getInstance().getTriggerManager();
		Trigger trigger = manager.getTrigger(args[0]);
		
		if(trigger == null)
			throw new BadArgumentException(0, "Unknown trigger");
		
		if(args.length == 1)
		{
			trigger.setCondition(null);
			
			sender.sendMessage(ChatColor.YELLOW + "Trigger condition cleared for " + trigger.getName());
			return true;
		}
		
		String condition = "";
		for(int i = 1; i < args.length; ++i)
		{
			if(!condition.isEmpty())
				condition += " ";
			condition += args[i];
		}
		
		Condition cond = Condition.parse(condition);
		
		trigger.setCondition(cond);
		manager.saveTrigger(trigger);
		
		sender.sendMessage(ChatColor.GREEN + "Set trigger " + trigger.getName() + "'s trigger condition to:");
		sender.sendMessage(ChatColor.GRAY + cond.toString());
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
