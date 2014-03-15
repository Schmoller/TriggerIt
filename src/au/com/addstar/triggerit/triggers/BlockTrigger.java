package au.com.addstar.triggerit.triggers;

import java.util.ArrayList;
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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
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
import com.google.common.collect.ImmutableMap;

import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.WorldSpecific;
import au.com.addstar.triggerit.commands.BadArgumentException;

public class BlockTrigger extends Trigger implements WorldSpecific
{
	public enum TriggerType
	{
		Remove,
		Place,
		Change,
		LeftClick,
		RightClick,
		Click,
		Physical,
		BlockUpdate
	}
	
	private static ArrayList<String> mTriggerTypeNames;
	private static HashMap<String, TriggerType> mTriggerTypeMap;
	
	// Triggers waiting to be completed
	private static WeakHashMap<Player, BlockTrigger> mWaitingTriggers = new WeakHashMap<Player, BlockTrigger>();
	
	static
	{
		mTriggerTypeNames = new ArrayList<String>();
		mTriggerTypeMap = new HashMap<String, TriggerType>();
		
		for(TriggerType type : TriggerType.values())
		{
			mTriggerTypeNames.add(type.name());
			mTriggerTypeMap.put(type.name().toLowerCase(), type);
		}
	}
	
	// Order: World, X, Z, Y
	private static HashMap<UUID, HashMap<Integer, HashMap<Integer, HashMultimap<Integer, BlockTrigger>>>> mBlockTriggers = new HashMap<UUID, HashMap<Integer,HashMap<Integer,HashMultimap<Integer,BlockTrigger>>>>();
	
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
	
	private static void removeTriggerAt(UUID world, BlockVector location, BlockTrigger trigger)
	{
		HashMap<Integer, HashMap<Integer, HashMultimap<Integer, BlockTrigger>>> worldMap = mBlockTriggers.get(world);
		
		if(worldMap == null)
			return;
		
		HashMap<Integer, HashMultimap<Integer, BlockTrigger>> xMap = worldMap.get(location.getBlockX());
		
		if(xMap == null)
			return;
		
		HashMultimap<Integer, BlockTrigger> zMap = xMap.get(location.getBlockZ());
		
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
	
	private static void addTriggerAt(UUID world, BlockVector location, BlockTrigger trigger)
	{
		HashMap<Integer, HashMap<Integer, HashMultimap<Integer, BlockTrigger>>> worldMap = mBlockTriggers.get(world);
		
		if(worldMap == null)
		{
			worldMap = new HashMap<Integer, HashMap<Integer,HashMultimap<Integer,BlockTrigger>>>();
			mBlockTriggers.put(world, worldMap);
		}
		
		HashMap<Integer, HashMultimap<Integer, BlockTrigger>> xMap = worldMap.get(location.getBlockX());
		
		if(xMap == null)
		{
			xMap = new HashMap<Integer, HashMultimap<Integer,BlockTrigger>>();
			worldMap.put(location.getBlockX(), xMap);
		}
		
		HashMultimap<Integer, BlockTrigger> zMap = xMap.get(location.getBlockZ());
		
		if(zMap == null)
		{
			zMap = HashMultimap.create();
			xMap.put(location.getBlockZ(), zMap);
		}
		
		zMap.put(location.getBlockY(), trigger);
	}
	
	private TriggerType mType;
	private BlockVector mLocation;
	private UUID mWorld;
	
	public BlockTrigger() {}
	private BlockTrigger(String name)
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
	
	public TriggerType getType()
	{
		return mType;
	}
	
	public void setType(TriggerType type)
	{
		mType = type;
	}
	
	private void setBlock(Block block)
	{
		mWorld = block.getWorld().getUID();
		mLocation = new BlockVector(block.getX(), block.getY(), block.getZ());
	}
	
	@Override
	public UUID getWorld()
	{
		return mWorld;
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
			return "Block trigger (incomplete)";

		World world = Bukkit.getWorld(mWorld);
		return String.format("Block trigger @(%d,%d,%d,%s) for %s", mLocation.getBlockX(), mLocation.getBlockY(), mLocation.getBlockZ(), (world != null ? world.getName() : "Unloaded"), mType.name()); 
	}
	
	@Override
	protected void load( ConfigurationSection section )
	{
		mType = TriggerType.valueOf(section.getString("type"));
		mWorld = UUID.fromString(section.getString("world"));
		mLocation = (BlockVector)section.get("block");
	}
	
	@Override
	protected void save( ConfigurationSection section )
	{
		section.set("block", mLocation);
		section.set("world", mWorld.toString());
		section.set("type", mType.name());
	}
	
	@Override
	protected String[] describeTrigger()
	{
		World world = Bukkit.getWorld(mWorld);
		
		return new String[] {
			ChatColor.GRAY + "Location: " + ChatColor.YELLOW + String.format("%d, %d, %d in %s", mLocation.getBlockX(), mLocation.getBlockY(), mLocation.getBlockZ(), (world != null ? world.getName() : mWorld.toString())),
			ChatColor.GRAY + "Trigger on: " + ChatColor.YELLOW + mType.name()
		};
	}
	
	public static BlockTrigger newTrigger(CommandSender sender, String name, String[] args) throws IllegalArgumentException, IllegalStateException, BadArgumentException
	{
		if(!(sender instanceof Player))
			throw new IllegalArgumentException("Block is only usable by players.");
		
		if(args.length != 1)
			throw new IllegalStateException("<type>");

		TriggerType type = mTriggerTypeMap.get(args[0].toLowerCase());
		if(type == null)
			throw new BadArgumentException(0, "Unknown block trigger type");
		
		BlockTrigger trigger = new BlockTrigger(name);
		trigger.setType(type);
		
		mWaitingTriggers.put((Player)sender, trigger);
		sender.sendMessage(ChatColor.GREEN + "Please click a block to complete the block trigger");
		
		return trigger;
	}
	
	public static List<String> tabComplete(CommandSender sender, String[] args)
	{
		if(args.length == 1)
			return Utilities.matchString(args[0], mTriggerTypeNames);
		
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
				if(!trigger.isEnabled())
					continue;
				
				if(trigger.getType() == TriggerType.Place || trigger.getType() == TriggerType.Change)
					trigger.trigger(new ImmutableMap.Builder<String, Object>().put("player", event.getPlayer()).build());
			}
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onBlockBreak(BlockBreakEvent event)
		{
			Set<BlockTrigger> triggers = getTriggersAt(event.getBlock());
			
			for(BlockTrigger trigger : triggers)
			{
				if(!trigger.isEnabled())
					continue;
				
				if(trigger.getType() == TriggerType.Remove || trigger.getType() == TriggerType.Change)
				{
					trigger.trigger(new ImmutableMap.Builder<String, Object>()
						.put("player", event.getPlayer())
						.put("block", event.getBlock())
						.put("location", event.getBlock().getLocation())
						.build());
				}
			}
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onBlockUpdate(BlockPhysicsEvent event)
		{
			Set<BlockTrigger> triggers = getTriggersAt(event.getBlock());
			
			for(BlockTrigger trigger : triggers)
			{
				if(!trigger.isEnabled())
					continue;
				
				if(trigger.getType() == TriggerType.BlockUpdate)
				{
					trigger.trigger(new ImmutableMap.Builder<String, Object>()
						.put("block", event.getBlock())
						.put("location", event.getBlock().getLocation())
						.build());
				}
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
				if(!trigger.isEnabled())
					continue;
				
				if(((trigger.getType() == TriggerType.LeftClick || trigger.getType() == TriggerType.Click) && event.getAction() == Action.LEFT_CLICK_BLOCK) ||
				   ((trigger.getType() == TriggerType.RightClick || trigger.getType() == TriggerType.Click) && event.getAction() == Action.RIGHT_CLICK_BLOCK) ||
				   (trigger.getType() == TriggerType.Physical && event.getAction() == Action.PHYSICAL))
				{
					trigger.trigger(new ImmutableMap.Builder<String, Object>()
						.put("player", event.getPlayer())
						.put("block", event.getClickedBlock())
						.put("location", event.getClickedBlock().getLocation())
						.build());
				}
			}
		}
		
		@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=false)
		public void onBlockClickLowest(PlayerInteractEvent event)
		{
			if(event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;
			
			BlockTrigger trigger = mWaitingTriggers.remove(event.getPlayer());
			
			if(trigger != null)
			{
				Block block = event.getClickedBlock();
				trigger.setBlock(block);
				TriggerItPlugin.getInstance().getTriggerManager().completeTrigger(trigger);
				event.getPlayer().sendMessage(ChatColor.GREEN + "Finished creation of Block trigger at " + String.format("%d, %d, %d in %s", block.getX(), block.getY(), block.getZ(), block.getWorld().getName()));
				event.setCancelled(true);
			}
		}
	}
}
