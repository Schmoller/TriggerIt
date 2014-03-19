package au.com.addstar.triggerit.actions;

import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import au.com.addstar.triggerit.Action;
import au.com.addstar.triggerit.BasicArgumentProvider;
import au.com.addstar.triggerit.BasicTextifier;
import au.com.addstar.triggerit.StringWithPlaceholders;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.targets.Target;
import au.com.addstar.triggerit.targets.TargetCS;

public class CommandAction implements Action
{
	private String mCommand;
	private TargetCS mExecutor;
	
	public CommandAction()
	{
	}
	
	@Override
	public void execute( Map<String, Object> arguments )
	{
		mExecutor.setArgumentMap(arguments);
		
		String commandPre = Utilities.replaceArguments(mCommand, arguments, BasicArgumentProvider.instance, BasicTextifier.instance);
		StringWithPlaceholders command = StringWithPlaceholders.from(commandPre, arguments, BasicArgumentProvider.instance, BasicTextifier.instance);
		
		if(command != null)
		{
			for(String commandPost : command)
			{
				for(CommandSender sender : mExecutor.getTargets())
					Bukkit.dispatchCommand(sender, commandPost);
			}
		}
		else
		{
			for(CommandSender sender : mExecutor.getTargets())
				Bukkit.dispatchCommand(sender, commandPre);
		}
	}
	
	@Override
	public void load( ConfigurationSection section ) throws InvalidConfigurationException
	{
		mCommand = section.getString("command");
		mExecutor = (TargetCS)Target.loadTarget(section.getConfigurationSection("target"));
	}
	
	@Override
	public void save( ConfigurationSection section )
	{
		section.set("command", mCommand);
		ConfigurationSection target = section.createSection("target");
		mExecutor.save(target);
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
			action.mExecutor = TargetCS.parseTargets(args[0], true);
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
