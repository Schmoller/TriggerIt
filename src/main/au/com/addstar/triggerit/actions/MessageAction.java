package au.com.addstar.triggerit.actions;

import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import au.com.addstar.triggerit.Action;
import au.com.addstar.triggerit.StringWithPlaceholders;
import au.com.addstar.triggerit.TriggerIt;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.targets.Target;

public class MessageAction implements Action
{
	private String mMessage;
	private Target<? extends CommandSender> mTarget;
	
	@Override
	public void execute( Map<String, Object> arguments )
	{
		mTarget.setArgumentMap(arguments);
		String messagePre = Utilities.replaceArguments(mMessage, arguments);
		StringWithPlaceholders message = StringWithPlaceholders.from(messagePre, arguments);
		
		if(message != null)
		{
			for(String messagePost : message)
			{
				for(CommandSender target : mTarget.getTargets())
					target.sendMessage(messagePost);
			}
		}
		else
		{
			for(CommandSender target : mTarget.getTargets())
				target.sendMessage(messagePre);
		}
	}
	
	@Override
	public void load( ConfigurationSection section ) throws InvalidConfigurationException
	{
		mMessage = section.getString("message");
		mTarget = TriggerIt.parseTargets(section.getString("target"), CommandSender.class);
	}
	
	@Override
	public void save( ConfigurationSection section )
	{
		section.set("message", mMessage);
		section.set("target", mTarget.toString());
	}
	
	@Override
	public String[] describe()
	{
		return new String[] {
			ChatColor.GOLD + "Message action:",
			ChatColor.GRAY + " Message: " + ChatColor.YELLOW + mMessage,
			ChatColor.GRAY + " Target: " + ChatColor.YELLOW + mTarget.describe()
		};
	}
	
	@Override
	public String toString()
	{
		return String.format("Message '%s' to %s", mMessage + ChatColor.RESET, mTarget.describe());
	}
	
	public static MessageAction newAction(CommandSender sender, String[] args) throws IllegalArgumentException, IllegalStateException, BadArgumentException
	{
		if(args.length < 2)
			throw new IllegalStateException("<target> <message>");
		
		MessageAction action = new MessageAction();
		try
		{
			action.mTarget = TriggerIt.parseTargets(args[0], CommandSender.class);
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
