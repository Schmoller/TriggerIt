package au.com.addstar.triggerit.commands.triggers;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.TriggerManager;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.commands.CommandSenderType;
import au.com.addstar.triggerit.commands.ICommand;
import au.com.addstar.triggerit.flags.Flag;

public class TriggerSetCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "set";
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
		return label + " <trigger> <option> [value]";
	}

	@Override
	public String getDescription()
	{
		return "Sets an option for a trigger";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if(args.length <= 1)
			return false;
		
		TriggerManager manager = TriggerItPlugin.getInstance().getTriggerManager();
		
		Trigger trigger = manager.getTrigger(args[0]);
		if(trigger == null)
			throw new BadArgumentException(0, "Unknown trigger");
		
		Flag<Object> flag = (Flag<Object>)trigger.getFlag(args[1]);
		
		if(flag == null)
		{
			sender.sendMessage(ChatColor.RED + "Unknown option " + args[1] + ". Available options:");
			String options = "";
			
			for(String name : trigger.getFlags().keySet())
			{
				if(!options.isEmpty())
					options += ", ";
				options += name;
			}
			
			sender.sendMessage(ChatColor.RED + options);
			return true;
		}
		
		if(args.length == 2)
		{
			sender.sendMessage(ChatColor.GREEN + args[1] + " is set to " + flag.getValueString());
			return true;
		}
		
		Object result = null;
		try
		{
			Player player = (sender instanceof Player ? (Player)sender : null);
			result = flag.parse(player, Arrays.copyOfRange(args, 2, args.length));
		}
		catch(IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + String.format("Usage: %s%s %s %s %s", parent, label, args[0], args[1], e.getMessage()));
			return true;
		}
		catch(BadArgumentException e)
		{
			String cmdString = ChatColor.GRAY + parent + label;
			for(int i = 0; i < args.length; ++i)
			{
				cmdString += " ";
				if(i == e.getArgument() + 2)
					cmdString += ChatColor.RED + args[i] + ChatColor.GRAY;
				else
					cmdString += args[i];
			}
			
			sender.sendMessage(ChatColor.RED + "Error in command: " + cmdString);
			sender.sendMessage(ChatColor.RED + " " + e.getMessage());
			return true;
		}
		
		Object lastValue = flag.getValue();
		flag.setValue(result);
		trigger.onFlagChanged(args[1], flag, lastValue);
		manager.saveTrigger(trigger);
		
		sender.sendMessage(ChatColor.GREEN + args[1] + " has been set to " + flag.getValueString());
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length > 1)
		{
			Trigger trigger = TriggerItPlugin.getInstance().getTriggerManager().getTrigger(args[0]);
			if(trigger == null)
				return null;
			
			if(args.length == 2)
				return Utilities.matchString(args[1], trigger.getFlags().keySet());
			else
			{
				@SuppressWarnings( "unchecked" )
				Flag<Object> flag = (Flag<Object>)trigger.getFlag(args[1]);
				if(flag == null)
					return null;
				
				Player player = (sender instanceof Player ? (Player)sender : null);
				return flag.tabComplete(player, Arrays.copyOfRange(args, 2, args.length));
			}
		}
		return null;
	}

}
