package au.com.addstar.triggerit.triggers;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;

public class BlockTrigger implements Trigger
{
	public enum TriggerType
	{
		Remove,
		Place,
		LeftClick,
		RightClick,
		PhysInteract,
		BlockUpdate
	}
	
	@Override
	public boolean isValid()
	{
		return false;
	}

	@Override
	public boolean isEnabled()
	{
		return false;
	}

	@Override
	public void setEnabled( boolean enabled )
	{
	}
	
	public static BlockTrigger newTrigger(CommandSender sender, String[] args) throws IllegalArgumentException, IllegalStateException
	{
		return null;
	}
	
	public static List<String> tabComplete(CommandSender sender, String[] args)
	{
		return null;
	}
	
	public static void initializeType(TriggerItPlugin plugin)
	{
		Bukkit.getPluginManager().registerEvents(new BlockTriggerListener(), plugin);
	}
	
	private static class BlockTriggerListener implements Listener
	{
		
	}
}
