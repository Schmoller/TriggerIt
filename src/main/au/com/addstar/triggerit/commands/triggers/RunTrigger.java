package au.com.addstar.triggerit.commands.triggers;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.TriggerManager;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.commands.CommandSenderType;
import au.com.addstar.triggerit.commands.ICommand;
import au.com.addstar.triggerit.triggers.CustomTrigger;

public class RunTrigger implements ICommand
{
	@Override
	public String getName()
	{
		return "run";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"trigger"};
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
		return "Runs a custom trigger. Depending on who calls this, it may provide extra information to the trigger";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.allOf(CommandSenderType.class);
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
		
		if(!(trigger instanceof CustomTrigger))
			throw new BadArgumentException(0, "Trigger is not a Custom trigger");

		sender.sendMessage(ChatColor.GREEN + "Running " + trigger.getName());
		
		if(sender instanceof Player)
		{
			trigger.trigger(new ImmutableMap.Builder<String, Object>()
				.put("player", sender)
				.put("location", ((Player)sender).getLocation())
				.put("world", ((Player)sender).getWorld())
				.build());
		}
		else if(sender instanceof BlockCommandSender)
		{
			trigger.trigger(new ImmutableMap.Builder<String, Object>()
				.put("block", ((BlockCommandSender)sender).getBlock())
				.put("location", ((BlockCommandSender)sender).getBlock().getLocation())
				.put("world", ((BlockCommandSender)sender).getBlock().getWorld())
				.build());
		}
		else
		{
			Map<String,Object> map = Collections.emptyMap(); 
			trigger.trigger(map);
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
