package au.com.addstar.triggerit;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionAttachment;

public class CommandUtils
{
	public static void dispatchCommand(CommandSender sender, String command)
	{
		dispatchCommand(sender, command, null, false);
	}
	
	public static void dispatchCommand(CommandSender sender, String command, boolean opOverride)
	{
		dispatchCommand(sender, command, null, opOverride);
	}
	
	public static void dispatchCommand(CommandSender sender, String command, Map<String, Boolean> permissionOverrides)
	{
		dispatchCommand(sender, command, permissionOverrides, false);
	}
	
	public static void dispatchCommand(CommandSender sender, String command, Map<String, Boolean> permissionOverrides, boolean opOverride)
	{
		if(sender.isOp())
			opOverride = false;
		
		PermissionAttachment attachment = null;
		if(permissionOverrides != null)
		{
			attachment = sender.addAttachment(TriggerItPlugin.getInstance(), 1); // Add an attachment for 1 tick only just to be safe
			for(Entry<String, Boolean> permission : permissionOverrides.entrySet())
				attachment.setPermission(permission.getKey(), permission.getValue());
		}
		
		if(opOverride)
			sender.setOp(true);
		
		try
		{
			Bukkit.dispatchCommand(sender, command);
		}
		finally
		{
			if(opOverride)
				sender.setOp(false);
			
			if(attachment != null)
				sender.removeAttachment(attachment);
		}
	}
}
