
package au.com.addstar.triggerit.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
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
import au.com.addstar.triggerit.flags.TimeFlag;

public class TimeTrigger extends Trigger implements WorldSpecific
{
	private static List<TimeTrigger> mTriggers = new ArrayList<TimeTrigger>();
	private static TimeWatcher mWatcher;
	
	private TimeFlag mTime = new TimeFlag();
	private UUID mWorld;
	
	public TimeTrigger() 
	{
		addFlag("time", mTime);
	}
	
	private TimeTrigger(String name)
	{
		super(name);
		addFlag("time", mTime);
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
		if(section.isString("world"))
			mWorld = UUID.fromString(section.getString("world"));
	}
	
	@Override
	protected void save( ConfigurationSection section )
	{
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
		
		return new String[] {
			ChatColor.GRAY + "Time: " + ChatColor.YELLOW + mTime.getValueString(),
			ChatColor.GRAY + "World: " + ChatColor.YELLOW + worldName,
		};
	}
	
	
	
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
		
		trigger.mTime.setValue(trigger.mTime.parse(null, args));
		
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
		
		return String.format("Time trigger at %s in %s", mTime.getValueString(), worldName);
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
						if(lastTime < trigger.mTime.getValue() && time >= trigger.mTime.getValue())
							trigger.trigger(map);
					}
				}
				
				mLastTimes.put(world, time);
			}
		}
	}
}
