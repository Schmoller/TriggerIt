package au.com.addstar.triggerit.triggers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockVector;

import com.google.common.collect.HashMultimap;

import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;

public class BlockTrigger extends Trigger
{
	public enum TriggerType
	{
		Remove,
		Place,
		LeftClick,
		RightClick,
		Physical,
		BlockUpdate
	}
	
	// Order: World, X, Z, Y
	private static HashMap<UUID, HashMap<Integer, HashMap<Integer, HashMultimap<Integer, BlockTrigger>>>> mBlockTriggers;
	
	public static Set<BlockTrigger> getTriggersAt(Block block)
	{
		return getTriggersAt(block.getWorld(), block.getX(), block.getY(), block.getZ());
	}
	
	public static Set<BlockTrigger> getTriggersAt(World world, int x, int y, int z)
	{
		HashMap<Integer, HashMap<Integer, HashMultimap<Integer, BlockTrigger>>> worldMap = mBlockTriggers.get(world.getUID());
		
		if(worldMap == null)
			return Collections.emptySet();
		
		HashMap<Integer, HashMultimap<Integer, BlockTrigger>> xMap = worldMap.get(x);
		
		if(xMap == null)
			return Collections.emptySet();
		
		HashMultimap<Integer, BlockTrigger> zMap = xMap.get(z);
		
		if(zMap == null)
			return Collections.emptySet();
		
		return zMap.get(y);
	}
	
	private static void removeTriggerAt(World world, int x, int y, int z, BlockTrigger trigger)
	{
		HashMap<Integer, HashMap<Integer, HashMultimap<Integer, BlockTrigger>>> worldMap = mBlockTriggers.get(world.getUID());
		
		if(worldMap == null)
			return;
		
		HashMap<Integer, HashMultimap<Integer, BlockTrigger>> xMap = worldMap.get(x);
		
		if(xMap == null)
			return;
		
		HashMultimap<Integer, BlockTrigger> zMap = xMap.get(z);
		
		if(zMap == null)
			return;
		
		zMap.remove(y, trigger);
		
		if(zMap.isEmpty())
			xMap.remove(z);
		
		if(xMap.isEmpty())
			worldMap.remove(x);
		
		if(worldMap.isEmpty())
			mBlockTriggers.remove(world.getUID());
	}
	
	private static void addTriggerAt(World world, int x, int y, int z, BlockTrigger trigger)
	{
		HashMap<Integer, HashMap<Integer, HashMultimap<Integer, BlockTrigger>>> worldMap = mBlockTriggers.get(world.getUID());
		
		if(worldMap == null)
		{
			worldMap = new HashMap<Integer, HashMap<Integer,HashMultimap<Integer,BlockTrigger>>>();
			mBlockTriggers.put(world.getUID(), worldMap);
		}
		
		HashMap<Integer, HashMultimap<Integer, BlockTrigger>> xMap = worldMap.get(x);
		
		if(xMap == null)
		{
			xMap = new HashMap<Integer, HashMultimap<Integer,BlockTrigger>>();
			worldMap.put(x, xMap);
		}
		
		HashMultimap<Integer, BlockTrigger> zMap = xMap.get(z);
		
		if(zMap == null)
		{
			zMap = HashMultimap.create();
			xMap.put(z, zMap);
		}
		
		zMap.put(y, trigger);
	}
	
	private TriggerType mType;
	private BlockVector mLocation;
	private UUID mWorld;
	
	public Location getLocation()
	{
		World world = Bukkit.getWorld(mWorld);
		if(world == null)
			return null;
		
		return mLocation.toLocation(world);
	}
	
	public TriggerType getType()
	{
		return mType;
	}
	
	@Override
	public boolean isValid()
	{
		return false;
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
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onBlockPlace(BlockPlaceEvent event)
		{
			Set<BlockTrigger> triggers = getTriggersAt(event.getBlock());
			
			for(BlockTrigger trigger : triggers)
			{
				if(trigger.getType() == TriggerType.Place)
					trigger.trigger(event.getPlayer());
			}
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onBlockBreak(BlockBreakEvent event)
		{
			Set<BlockTrigger> triggers = getTriggersAt(event.getBlock());
			
			for(BlockTrigger trigger : triggers)
			{
				if(trigger.getType() == TriggerType.Remove)
					trigger.trigger(event.getPlayer());
			}
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onBlockUpdate(BlockPhysicsEvent event)
		{
			Set<BlockTrigger> triggers = getTriggersAt(event.getBlock());
			
			for(BlockTrigger trigger : triggers)
			{
				if(trigger.getType() == TriggerType.BlockUpdate)
					trigger.trigger(null);
			}
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onBlockClick(PlayerInteractEvent event)
		{
			if(event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.PHYSICAL)
				return;
			
			Set<BlockTrigger> triggers = getTriggersAt(event.getClickedBlock());
			
			for(BlockTrigger trigger : triggers)
			{
				if(trigger.getType() == TriggerType.LeftClick && event.getAction() == Action.LEFT_CLICK_BLOCK)
					trigger.trigger(event.getPlayer());
				else if(trigger.getType() == TriggerType.RightClick && event.getAction() == Action.RIGHT_CLICK_BLOCK)
					trigger.trigger(event.getPlayer());
				else if(trigger.getType() == TriggerType.Physical && event.getAction() == Action.PHYSICAL)
					trigger.trigger(event.getPlayer());
			}
		}
	}
}
