package au.com.addstar.triggerit.actions;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.triggerit.Action;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.commands.BadArgumentException;

public class MessageAction implements Action
{
	private String mMessage;
	private String mTarget;
	

	@Override
	public void execute( Map<String, Object> arguments )
	{
		String message = Utilities.replaceArguments(mMessage, arguments, this);
		
		if(mTarget.equals("~"))
			Bukkit.getConsoleSender().sendMessage(message);
		else if(mTarget.equals("*"))
			Bukkit.broadcastMessage(message);
		else if(mTarget.startsWith("#"))
		{
			String perm = mTarget.substring(1);
			Bukkit.broadcast(message, perm);
		}
		else
		{
			Player player = Bukkit.getPlayerExact(Utilities.replaceArguments(mTarget, arguments, this));
			if(player != null)
				player.sendMessage(message);
		}
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
	
	public static MessageAction newAction(CommandSender sender, String[] args) throws IllegalArgumentException, IllegalStateException, BadArgumentException
	{
		if(args.length < 2)
			throw new IllegalStateException("<target> <message>");
		
		MessageAction action = new MessageAction();
		action.mTarget = args[0];
		
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
