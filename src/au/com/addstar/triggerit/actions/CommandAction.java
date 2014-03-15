package au.com.addstar.triggerit.actions;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import au.com.addstar.triggerit.Action;
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
		
		String command = Utilities.replaceArguments(mCommand, arguments, this);
		
		for(CommandSender sender : mExecutor.getTargets())
			Bukkit.dispatchCommand(sender, command);
	}
	
	@Override
	public String resolveArgument( Object argument )
	{
		if(argument instanceof Player)
			return ((Player)argument).getName();
		else if(argument instanceof Block)
			return String.format("%d %d %d", ((Block) argument).getX(), ((Block) argument).getY(), ((Block) argument).getZ());
		else if(argument instanceof Location)
			return String.format("%d %d %d", ((Location) argument).getX(), ((Location) argument).getY(), ((Location) argument).getZ());
		
		return argument.toString();
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
