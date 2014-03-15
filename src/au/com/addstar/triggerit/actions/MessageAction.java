package au.com.addstar.triggerit.actions;

import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
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

public class MessageAction implements Action
{
	private String mMessage;
	private TargetCS mTarget;
	
	@Override
	public void execute( Map<String, Object> arguments )
	{
		mTarget.setArgumentMap(arguments);
		String message = Utilities.replaceArguments(mMessage, arguments, this);
		
		for(CommandSender target : mTarget.getTargets())
			target.sendMessage(message);
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
		mMessage = section.getString("message");
		mTarget = (TargetCS)Target.loadTarget(section.getConfigurationSection("target"));
	}
	
	@Override
	public void save( ConfigurationSection section )
	{
		section.set("message", mMessage);
		ConfigurationSection target = section.createSection("target");
		mTarget.save(target);
	}
	
	public static MessageAction newAction(CommandSender sender, String[] args) throws IllegalArgumentException, IllegalStateException, BadArgumentException
	{
		if(args.length < 2)
			throw new IllegalStateException("<target> <message>");
		
		MessageAction action = new MessageAction();
		try
		{
			action.mTarget = TargetCS.parseTargets(args[0], true);
		}
		catch(IllegalArgumentException e)
		{
			throw new BadArgumentException(0, e.getMessage());
		}
		
		action.mMessage = "";
		for(int i = 1; i < args.length; ++i)
		{
			if(!action.mMessage.isEmpty())
				action.mMessage += " ";
			action.mMessage += args[i];
		}
		
		action.mMessage = ChatColor.translateAlternateColorCodes('&', action.mMessage);
		
		return action;
	}
	
	public static List<String> tabComplete(CommandSender sender, String[] args)
	{
		return null;
	}
}
