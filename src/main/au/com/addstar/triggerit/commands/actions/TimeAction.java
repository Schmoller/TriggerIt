package au.com.addstar.triggerit.commands.actions;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import au.com.addstar.triggerit.Action;
import au.com.addstar.triggerit.BasicArgumentProvider;
import au.com.addstar.triggerit.BasicTextifier;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.flags.TimeFlag;

public class TimeAction implements Action
{
	private String mWorld;
	private int mTime;
	
	@Override
	public void execute( Map<String, Object> arguments )
	{
		String world = Utilities.replaceArguments(mWorld, arguments, BasicArgumentProvider.instance, BasicTextifier.instance);
		if(world.equals("*"))
		{
			for(World w : Bukkit.getWorlds())
				w.setTime(mTime);
		}
		else
		{
			World w = Bukkit.getWorld(world);
			if(w != null)
				w.setTime(mTime);
		}
	}

	@Override
	public void save( ConfigurationSection section )
	{
		section.set("world", mWorld);
		section.set("time", mTime);
	}

	@Override
	public void load( ConfigurationSection section ) throws InvalidConfigurationException
	{
		mWorld = section.getString("world");
		
		mTime = section.getInt("time");
	}

	@Override
	public String[] describe()
	{
		String worldName;
		
		if(mWorld.equals("*"))
			worldName = "All worlds";
		else
			worldName = mWorld;
		
		return new String[] {
			ChatColor.GRAY + "Time: " + ChatColor.YELLOW + TimeFlag.timeToString(mTime),
			ChatColor.GRAY + "World: " + ChatColor.YELLOW + worldName
		};
	}
	
	public static TimeAction newAction(CommandSender sender, String[] args) throws IllegalStateException, BadArgumentException
	{
		if(args.length != 1 && args.length != 2)
			throw new IllegalStateException("<time> [world]");
		
		int time = TimeFlag.parseTime(new String[] {args[0]});
		
		String world = "*";
		if(args.length == 2)
			world = args[1];
		
		TimeAction action = new TimeAction();
		action.mTime = time;
		action.mWorld = world;
		
		return action;
	}
	
	public static List<String> tabComplete(CommandSender sender, String[] args)
	{
		return null;
	}
	
	@Override
	public String toString()
	{
		return "Set time to " + TimeFlag.timeToString(mTime) + " in " + (mWorld.equals("*") ? "all worlds" : mWorld);
	}
}
