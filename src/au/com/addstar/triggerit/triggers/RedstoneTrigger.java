package au.com.addstar.triggerit.triggers;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockVector;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;

import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.commands.BadArgumentException;

public class RedstoneTrigger extends Trigger
{
	// Triggers waiting to be completed
	private static WeakHashMap<Player, RedstoneTrigger> mWaitingTriggers = new WeakHashMap<Player, RedstoneTrigger>();
	
	// Order: World, X, Z, Y
	private static HashMap<UUID, HashMap<Integer, HashMap<Integer, HashMultimap<Integer, RedstoneTrigger>>>> mBlockTriggers = new HashMap<UUID, HashMap<Integer,HashMap<Integer,HashMultimap<Integer,RedstoneTrigger>>>>();
	
	public static Set<RedstoneTrigger> getTriggersAt(Block block)
	{
		return getTriggersAt(block.getWorld(), block.getX(), block.getY(), block.getZ());
	}
	
	public static Set<RedstoneTrigger> getTriggersAt(World world, int x, int y, int z)
	{
		HashMap<Integer, HashMap<Integer, HashMultimap<Integer, RedstoneTrigger>>> worldMap = mBlockTriggers.get(world.getUID());
		
		if(worldMap == null)
			return Collections.emptySet();
		
		HashMap<Integer, HashMultimap<Integer, RedstoneTrigger>> xMap = worldMap.get(x);
		
		if(xMap == null)
			return Collections.emptySet();
		
		HashMultimap<Integer, RedstoneTrigger> zMap = xMap.get(z);
		
		if(zMap == null)
			return Collections.emptySet();
		
		return zMap.get(y);
	}
	
	private static void removeTriggerAt(UUID world, BlockVector location, RedstoneTrigger trigger)
	{
		HashMap<Integer, HashMap<Integer, HashMultimap<Integer, RedstoneTrigger>>> worldMap = mBlockTriggers.get(world);
		
		if(worldMap == null)
			return;
		
		HashMap<Integer, HashMultimap<Integer, RedstoneTrigger>> xMap = worldMap.get(location.getBlockX());
		
		if(xMap == null)
			return;
		
		HashMultimap<Integer, RedstoneTrigger> zMap = xMap.get(location.getBlockZ());
		
		if(zMap == null)
			return;
		
		zMap.remove(location.getBlockY(), trigger);
		
		if(zMap.isEmpty())
			xMap.remove(location.getBlockZ());
		
		if(xMap.isEmpty())
			worldMap.remove(location.getBlockX());
		
		if(worldMap.isEmpty())
			mBlockTriggers.remove(world);
	}
	
	private static void addTriggerAt(UUID world, BlockVector location, RedstoneTrigger trigger)
	{
		HashMap<Integer, HashMap<Integer, HashMultimap<Integer, RedstoneTrigger>>> worldMap = mBlockTriggers.get(world);
		
		if(worldMap == null)
		{
			worldMap = new HashMap<Integer, HashMap<Integer,HashMultimap<Integer,RedstoneTrigger>>>();
			mBlockTriggers.put(world, worldMap);
		}
		
		HashMap<Integer, HashMultimap<Integer, RedstoneTrigger>> xMap = worldMap.get(location.getBlockX());
		
		if(xMap == null)
		{
			xMap = new HashMap<Integer, HashMultimap<Integer,RedstoneTrigger>>();
			worldMap.put(location.getBlockX(), xMap);
		}
		
		HashMultimap<Integer, RedstoneTrigger> zMap = xMap.get(location.getBlockZ());
		
		if(zMap == null)
		{
			zMap = HashMultimap.create();
			xMap.put(location.getBlockZ(), zMap);
		}
		
		zMap.put(location.getBlockY(), trigger);
	}
	
	private boolean mOnHigh;
	private int mThreshold;
	
	private BlockVector mLocation;
	private UUID mWorld;
	
	public RedstoneTrigger() {}
	private RedstoneTrigger(String name)
	{
		super(name);
	}
	
	public Location getLocation()
	{
		World world = Bukkit.getWorld(mWorld);
		if(world == null)
			return null;
		
		return mLocation.toLocation(world);
	}
	
	public int getThreshold()
	{
		return mThreshold;
	}
	
	public void setThreshold(int threshold)
	{
		mThreshold = threshold;
	}
	
	public boolean isOnHigh()
	{
		return mOnHigh;
	}
	
	public void setIsOnHigh(boolean high)
	{
		mOnHigh = high;
	}
	
	private void setBlock(Block block)
	{
		mWorld = block.getWorld().getUID();
		mLocation = new BlockVector(block.getX(), block.getY(), block.getZ());
	}
	
	@Override
	public boolean isValid()
	{
		return mWorld != null && mLocation != null;
	}
	
	@Override
	public void onLoad()
	{
		addTriggerAt(mWorld, mLocation, this);
	}
	
	@Override
	public void onUnload()
	{
		removeTriggerAt(mWorld, mLocation, this);
	}
	
	@Override
	public String toString()
	{
		if(!isValid())
			return "Redstone trigger (incomplete)";

		World world = Bukkit.getWorld(mWorld);
		if(mOnHigh)
			return String.format("Redstone trigger @(%d,%d,%d,%s) on >= %d", mLocation.getBlockX(), mLocation.getBlockY(), mLocation.getBlockZ(), (world != null ? world.getName() : "Unloaded"), mThreshold); 
		else
			return String.format("Redstone trigger @(%d,%d,%d,%s) on <= %d", mLocation.getBlockX(), mLocation.getBlockY(), mLocation.getBlockZ(), (world != null ? world.getName() : "Unloaded"), mThreshold);
	}
	
	public static RedstoneTrigger newTrigger(CommandSender sender, String name, String[] args) throws IllegalArgumentException, IllegalStateException, BadArgumentException
	{
		if(!(sender instanceof Player))
			throw new IllegalArgumentException("Redstone is only usable by players.");
		
		if(args.length != 2)
			throw new IllegalStateException("<level> {high|low}");

		int level;
		
		try
		{
			level = Integer.parseInt(args[0]);
			if(level < 0 || level > 15)
				throw new BadArgumentException(0, "Level must be between 0 and 15 inclusive.");
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(0, "Expected number between 0 and 15 inclusive");
		}
		
		boolean onHigh = false;
		
		if(args[1].equalsIgnoreCase("high"))
			onHigh = true;
		else if(!args[1].equalsIgnoreCase("low"))
			throw new BadArgumentException(1, "Expected high or low");
		
		RedstoneTrigger trigger = new RedstoneTrigger(name);
		trigger.mOnHigh = onHigh;
		trigger.mThreshold = level;
		
		mWaitingTriggers.put((Player)sender, trigger);
		sender.sendMessage(ChatColor.GREEN + "Please click a block to complete the block trigger");
		
		return trigger;
	}
	
	public static List<String> tabComplete(CommandSender sender, String[] args)
	{
		if(args.length == 2)
			return Utilities.matchString(args[1], Arrays.asList("high","low"));
		
		return null;
	}
	
	public static void initializeType(TriggerItPlugin plugin)
	{
		Bukkit.getPluginManager().registerEvents(new RedstoneTriggerListener(), plugin);
	}
	
	private static class RedstoneTriggerListener implements Listener
	{
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onRedstone(BlockRedstoneEvent event)
		{
			Set<RedstoneTrigger> triggers = getTriggersAt(event.getBlock());
			
			for(RedstoneTrigger trigger : triggers)
			{
				if(!trigger.isEnabled())
					continue;
				
				if(trigger.isOnHigh())
				{
					if(event.getNewCurrent() >= trigger.getThreshold() && event.getOldCurrent() < trigger.getThreshold())
					{
						trigger.trigger(new ImmutableMap.Builder<String, Object>()
							.put("block", event.getBlock())
							.put("location", event.getBlock().getLocation())
							.put("level", event.getNewCurrent())
							.build());
					}
				}
				else
				{
					if(event.getNewCurrent() <= trigger.getThreshold() && event.getOldCurrent() > trigger.getThreshold())
					{
						trigger.trigger(new ImmutableMap.Builder<String, Object>()
							.put("block", event.getBlock())
							.put("location", event.getBlock().getLocation())
							.put("level", event.getNewCurrent())
							.build());
					}
				}
			}
		}
		
		@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=false)
		public void onBlockClickLowest(PlayerInteractEvent event)
		{
			if(event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;
			
			RedstoneTrigger trigger = mWaitingTriggers.remove(event.getPlayer());
			
			if(trigger != null)
			{
				Block block = event.getClickedBlock();
				trigger.setBlock(block);
				TriggerItPlugin.getInstance().getTriggerManager().completeTrigger(trigger);
				event.getPlayer().sendMessage(ChatColor.GREEN + "Finished creation of Redstone trigger at " + String.format("%d, %d, %d in %s", block.getX(), block.getY(), block.getZ(), block.getWorld().getName()));
				event.setCancelled(true);
			}
		}
	}
}
