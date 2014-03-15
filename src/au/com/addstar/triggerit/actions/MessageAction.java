package au.com.addstar.triggerit.actions;

import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import au.com.addstar.triggerit.Action;
import au.com.addstar.triggerit.BasicTextifier;
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
		String message = Utilities.replaceArguments(mMessage, arguments, BasicTextifier.instance);
		
		for(CommandSender target : mTarget.getTargets())
			target.sendMessage(message);
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
