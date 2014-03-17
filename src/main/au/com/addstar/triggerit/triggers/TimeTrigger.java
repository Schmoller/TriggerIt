
package au.com.addstar.triggerit.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.ImmutableMap;

import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.WorldSpecific;
import au.com.addstar.triggerit.commands.BadArgumentException;

public class TimeTrigger extends Trigger implements WorldSpecific
{
	private static List<TimeTrigger> mTriggers = new ArrayList<TimeTrigger>();
	private static TimeWatcher mWatcher;
	
	private int mTime;
	private UUID mWorld;
	
	public TimeTrigger() {}
	
	private TimeTrigger(String name)
	{
		super(name);
	}
	
	@Override
	public boolean isValid()
	{
		return true;
	}
	
	@Override
	public UUID getWorld()
	{
		return mWorld;
	}
	
	@Override
	public void onLoad()
	{
		mTriggers.add(this);
		mWatcher.start();
	}
	
	@Override
	public void onUnload()
	{
		mTriggers.remove(this);
		if(mTriggers.isEmpty())
			mWatcher.stop();
	}
	
	@Override
	protected void load( ConfigurationSection section )
	{
		mTime = section.getInt("time");
		if(section.isString("world"))
			mWorld = UUID.fromString(section.getString("world"));
	}
	
	@Override
	protected void save( ConfigurationSection section )
	{
		section.set("time", mTime);
		if(mWorld != null)
			section.set("world", mWorld.toString());
	}
	
	@Override
	protected String[] describeTrigger()
	{
		String worldName = "Any world";
		if(mWorld != null)
		{
			World world = Bukkit.getWorld(mWorld);
			if(world != null)
				worldName = world.getName();
			else
				worldName = mWorld.toString();
		}
		
		int ticks = mTime + 6000;
		if(ticks >= 24000)
			ticks -= 24000;
		
		int hours = ticks / 1000;
		int minutes = (ticks - (hours * 1000)) * 60 / 1000;
		
		return new String[] {
			ChatColor.GRAY + "Time: " + ChatColor.YELLOW + String.format("%02d:%02d", hours, minutes),
			ChatColor.GRAY + "World: " + ChatColor.YELLOW + worldName,
		};
	}
	
	private static Pattern mTimePattern = Pattern.compile("([\\d]+)ticks|(\\d{2}:\\d{2})|(\\d{1,2}(?::\\d{1,2})?)(am|pm)");
	
	public static TimeTrigger newTrigger(CommandSender sender, String name, String[] args) throws IllegalStateException, BadArgumentException
	{
		if(args.length != 1 && args.length != 2)
			throw new IllegalStateException("<time> [world]");
		
		TimeTrigger trigger = new TimeTrigger(name);
		
		if(args.length == 2)
		{
			World world = Bukkit.getWorld(args[1]);
			if(world == null)
				throw new BadArgumentException(1, "Unknown world");
			trigger.mWorld = world.getUID();
		}
		
		Matcher match = mTimePattern.matcher(args[0]);
		
		if(!match.matches())
		{
			BadArgumentException ex = new BadArgumentException(0, "Unknown time.");
			ex.addInfo(ChatColor.GOLD + "Expected time formats: " + ChatColor.GRAY + "1000ticks 10:30 3:20pm 4am");
		}
		
		if(match.group(1) != null)
			trigger.mTime = Integer.parseInt(match.group(1));
		else if(match.group(2) != null)
		{
			String[] parts = match.group(2).split(":");
			int hours = Integer.parseInt(parts[0]);
			int minutes = Integer.parseInt(parts[1]);
			
			int ticks = hours * 1000 + (minutes * 1000 / 60);
			ticks -= 6000;
			if(ticks < 0)
				ticks += 24000;
			while(ticks >= 24000)
				ticks -= 24000;
			trigger.mTime = ticks;
		}
		else if(match.group(3) != null)
		{
			boolean am = match.group(4).equalsIgnoreCase("am");
			String[] parts = match.group(3).split(":");
			int hours = Integer.parseInt(parts[0]);
			int minutes = (parts.length == 2 ? Integer.parseInt(parts[1]) : 0);

			if(hours == 12)
				hours = 0;
			
			while(hours > 12)
			{
				hours -= 12;
				am = !am;
			}
			
			if(!am)
				hours += 12;
			
			int ticks = hours * 1000 + (minutes * 1000 / 60);
			ticks -= 6000;
			if(ticks < 0)
				ticks += 24000;
			while(ticks >= 24000)
				ticks -= 24000;
			trigger.mTime = ticks;
		}
		
		return trigger;
	}
	
	@Override
	public String toString()
	{
		String worldName = "any world";
		if(mWorld != null)
		{
			World world = Bukkit.getWorld(mWorld);
			if(world != null)
				worldName = world.getName();
			else
				worldName = mWorld.toString();
		}
		
		int ticks = mTime + 6000;
		if(ticks >= 24000)
			ticks -= 24000;
		
		int hours = ticks / 1000;
		int minutes = (ticks - (hours * 1000)) * 60 / 1000;
		
		return String.format("Time trigger at %2d:%2d in %s", hours, minutes, worldName);
	}
	
	public static List<String> tabComplete(CommandSender sender, String[] args)
	{
		return null;
	}
	
	public static void initializeType(TriggerItPlugin plugin)
	{
		mWatcher = new TimeWatcher(plugin);
	}
	
	private static class TimeWatcher implements Runnable
	{
		private TriggerItPlugin mPlugin;
		private BukkitTask mTask;
		
		private WeakHashMap<World, Long> mLastTimes = new WeakHashMap<World, Long>();
		
		public TimeWatcher(TriggerItPlugin plugin)
		{
			mPlugin = plugin;
		}
		
		public void start()
		{
			if(mTask == null)
				mTask = Bukkit.getScheduler().runTaskTimer(mPlugin, this, 20, 20);
		}
		
		public void stop()
		{
			if(mTask != null)
				mTask.cancel();
			mLastTimes.clear();
		}
		
		@Override
		public void run()
		{
			for(World world : Bukkit.getWorlds())
			{
				Long lastTime = mLastTimes.get(world);
				if(lastTime == null)
					lastTime = world.getTime();
				
				UUID worldId = world.getUID();
				long time = world.getTime();
				
				Map<String, Object> map = new ImmutableMap.Builder<String, Object>()
					.put("world", world)
					.build();
				
				for(TimeTrigger trigger : mTriggers)
				{
					if(trigger.mWorld == null || trigger.mWorld.equals(worldId))
					{
						if(lastTime < trigger.mTime && time >= trigger.mTime)
							trigger.trigger(map);
					}
				}
				
				mLastTimes.put(world, time);
			}
		}
	}
}
