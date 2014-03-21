package au.com.addstar.triggerit.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import au.com.addstar.triggerit.Action;
import au.com.addstar.triggerit.CommandUtils;
import au.com.addstar.triggerit.StringWithPlaceholders;
import au.com.addstar.triggerit.TriggerIt;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.targets.Target;

public class CommandAction implements Action
{
	private String mCommand;
	private Target<? extends CommandSender> mExecutor;
	
	public CommandAction()
	{
	}
	
	private void runCommand(List<? extends CommandSender> targets, String command)
	{
		boolean opOverride = false;
		if(command.startsWith("*"))
		{
			opOverride = true;
			command = command.substring(1);
		}
		
		Map<String, Boolean> perms = null;
		while(command.startsWith("["))
		{
			String perm;
			int end = command.indexOf(']');
			if(end == -1)
				break;
			
			perm = command.substring(1, end);
			command = command.substring(end+1);
			
			boolean invert = false;
			if(perm.startsWith("-"))
			{
				invert = true;
				perm = perm.substring(1);
			}
			
			if(perms == null)
				perms = new HashMap<String, Boolean>();
			
			perms.put(perm, !invert);
		}
		
		for(CommandSender sender : targets)
			CommandUtils.dispatchCommand(sender, command, perms, opOverride);
	}
	
	@Override
	public void execute( Map<String, Object> arguments )
	{
		mExecutor.setArgumentMap(arguments);
		
		String commandPre = Utilities.replaceArguments(mCommand, arguments);
		StringWithPlaceholders command = StringWithPlaceholders.from(commandPre, arguments);
		
		if(command != null)
		{
			for(String commandPost : command)
				runCommand(mExecutor.getTargets(), commandPost);
		}
		else
			runCommand(mExecutor.getTargets(), commandPre);
	}
	
	@Override
	public void load( ConfigurationSection section ) throws InvalidConfigurationException
	{
		mCommand = section.getString("command");
		mExecutor = TriggerIt.parseTargets(section.getString("target"), CommandSender.class);
	}
	
	@Override
	public void save( ConfigurationSection section )
	{
		section.set("command", mCommand);
		section.set("target", mExecutor.toString());
	}
	
	@Override
	public String[] describe()
	{
		return new String[] {
			ChatColor.GOLD + "Command action:",
			ChatColor.GRAY + " Command: " + ChatColor.YELLOW + mCommand,
			ChatColor.GRAY + " Target: " + ChatColor.YELLOW + mExecutor.describe()
		};
	}
	
	@Override
	public String toString()
	{
		return String.format("Command '%s' as %s", mCommand, mExecutor.describe());
	}
	
	public static CommandAction newAction(CommandSender sender, String[] args) throws IllegalArgumentException, IllegalStateException, BadArgumentException
	{
		if(args.length < 2)
			throw new IllegalStateException("<sender> <command>");
		
		CommandAction action = new CommandAction();
		try
		{
			action.mExecutor = TriggerIt.parseTargets(args[0], CommandSender.class);
		}
		catch(IllegalArgumentException e)
		{
			throw new BadArgumentException(0, e.getMessage());
		}
		
		action.mCommand = "";
		for(int i = 1; i < args.length; ++i)
		{
			if(!action.mCommand.isEmpty())
				action.mCommand += " ";
			action.mCommand += args[i];
		}
		
		
		
		return action;
	}
	
	public static List<String> tabComplete(CommandSender sender, String[] args)
	{
		return null;
	}

}
