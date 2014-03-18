package au.com.addstar.triggerit.triggers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;
import org.bukkit.material.PressurePlate;
import org.bukkit.material.TripwireHook;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;

import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.WorldSpecific;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.flags.Flag;
import au.com.addstar.triggerit.flags.FlagIO;

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
		BlockUpdate,
		Shot,
		Activate
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
	
	private TriggerTypeFlag mType = new TriggerTypeFlag();
	private BlockVector mLocation;
	private UUID mWorld;
	
	public BlockTrigger() 
	{
		addFlag("type", mType);
	}
	private BlockTrigger(String name)
	{
		super(name);
		addFlag("type", mType);
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
		return mType.getValue();
	}
	
	public void setType(TriggerType type)
	{
		mType.setValue(type);
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
		return String.format("Block trigger @(%d,%d,%d,%s) for %s", mLocation.getBlockX(), mLocation.getBlockY(), mLocation.getBlockZ(), (world != null ? world.getName() : "Unloaded"), mType.getValueString()); 
	}
	
	@Override
	protected void load( ConfigurationSection section )
	{
		mWorld = UUID.fromString(section.getString("world"));
		mLocation = (BlockVector)section.get("block");
	}
	
	@Override
	protected void save( ConfigurationSection section )
	{
		section.set("block", mLocation);
		section.set("world", mWorld.toString());
	}
	
	@Override
	protected String[] describeTrigger()
	{
		World world = Bukkit.getWorld(mWorld);
		
		return new String[] {
			ChatColor.GRAY + "Location: " + ChatColor.YELLOW + String.format("%d, %d, %d in %s", mLocation.getBlockX(), mLocation.getBlockY(), mLocation.getBlockZ(), (world != null ? world.getName() : mWorld.toString())),
			ChatColor.GRAY + "Trigger on: " + ChatColor.YELLOW + mType.getValueString()
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
		FlagIO.addKnownType("BlockTriggerType", TriggerTypeFlag.class);
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
				{
					trigger.trigger(new ImmutableMap.Builder<String, Object>()
						.put("player", event.getPlayer())
						.put("block", event.getBlock())
						.put("location", event.getBlock().getLocation())
						.put("world", event.getBlock().getWorld())
						.build());
				}
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
						.put("world", event.getBlock().getWorld())
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
						.put("world", event.getBlock().getWorld())
						.build());
				}
			}
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onBlockClick(PlayerInteractEvent event)
		{
			if(event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.PHYSICAL)
				return;
			
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.PHYSICAL)
				ActivateChecker.handleActivateCheck(event.getClickedBlock(), event.getPlayer(), false);
			
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
						.put("world", event.getClickedBlock().getWorld())
						.build());
				}
			}
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onBlockInteract(EntityInteractEvent event)
		{
			ActivateChecker.handleActivateCheck(event.getBlock(), event.getEntity(), false);
		}
		
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onProjectileHit(ProjectileHitEvent event)
		{
			Location loc = event.getEntity().getLocation();
			Vector dir = event.getEntity().getVelocity().normalize();
			
			Location newLoc = loc.clone();
			
			Block block = newLoc.getBlock();
			float f = 0;
			while(block.isEmpty() && f < 1.4f)
			{
				newLoc.add(dir.clone().multiply(f));
				block = newLoc.getBlock();
				
				f += 0.1;
			}

			if(block.isEmpty())
				return;
			
			if(event.getEntity().getShooter() instanceof Player)
				ActivateChecker.handleActivateCheck(block, (Player)event.getEntity().getShooter(), true);
			else
				ActivateChecker.handleActivateCheck(block, null, true);
			
			Set<BlockTrigger> triggers = getTriggersAt(block);
			
			for(BlockTrigger trigger : triggers)
			{
				if(!trigger.isEnabled())
					continue;
				
				if(trigger.getType() == TriggerType.Shot)
				{
					if(event.getEntity().getShooter() instanceof Player)
					{
						trigger.trigger(new ImmutableMap.Builder<String, Object>()
							.put("player", (Player)event.getEntity().getShooter())
							.put("block", block)
							.put("location", block.getLocation())
							.put("world", block.getWorld())
							.put("projectile", event.getEntity().getType().name())
							.build());
					}
					else
					{
						trigger.trigger(new ImmutableMap.Builder<String, Object>()
							.put("block", block)
							.put("location", block.getLocation())
							.put("world", block.getWorld())
							.put("projectile", event.getEntity().getType().name())
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
	
	private static class ActivateChecker implements Runnable
	{
		private static final Material[] activatable = new Material[] {Material.WOOD_BUTTON, Material.STONE_BUTTON, Material.LEVER, Material.TRIPWIRE_HOOK};
		private static final Material[] activatableShot = new Material[] {Material.WOOD_BUTTON, Material.WOOD_PLATE};
		
		public static boolean isActivatable(Material mat, boolean shot)
		{
			Material[] mats;
			if(shot)
				mats = activatableShot;
			else
				mats = activatable;
			
			for(Material m : mats)
			{
				if(m == mat)
					return true;
			}
			return false;
		}
		
		public static void handleActivateCheck(Block block, Entity entity, boolean shot)
		{
			ActivateChecker checker = null;
			
			if(block.getType() != Material.TRIPWIRE && !isActivatable(block.getType(), shot))
				return;
			
			MaterialData data = block.getState().getData();
			
			if(block.getType() == Material.TRIPWIRE)
			{
				Block hook1, hook2;
				
				// Check west-east direction
				hook1 = hook2 = block;
				for(int i = 1; i < 40; ++i)
				{
					if(hook1.getType() == Material.TRIPWIRE)
						hook1 = hook1.getRelative(BlockFace.WEST);
					if(hook2.getType() == Material.TRIPWIRE)
						hook2 = hook2.getRelative(BlockFace.EAST);
					
					if(hook1.getType() != Material.TRIPWIRE && hook2.getType() != Material.TRIPWIRE)
						break;
				}
				
				if(hook1.getType() != Material.TRIPWIRE_HOOK || hook2.getType() != Material.TRIPWIRE_HOOK)
				{
					// Check north-south direction
					hook1 = hook2 = block;
					for(int i = 1; i < 40; ++i)
					{
						if(hook1.getType() == Material.TRIPWIRE)
							hook1 = hook1.getRelative(BlockFace.NORTH);
						if(hook2.getType() == Material.TRIPWIRE)
							hook2 = hook2.getRelative(BlockFace.SOUTH);
						
						if(hook1.getType() != Material.TRIPWIRE && hook2.getType() != Material.TRIPWIRE)
							break;
					}
				}
				
				if(hook1.getType() != Material.TRIPWIRE_HOOK || hook2.getType() != Material.TRIPWIRE_HOOK)
					return;
				
				TripwireHook hook1D = (TripwireHook)hook1.getState().getData();
				TripwireHook hook2D = (TripwireHook)hook2.getState().getData();
				
				if(hook1D.isConnected() && hook2D.isConnected() && hook1D.getFacing() == hook2D.getFacing().getOppositeFace())
				{
					if(!hook1D.isActivated())
						checker = new ActivateChecker(hook1, hook2, entity);
				}
			}
			else if(data instanceof Button)
			{
				if(!((Button)data).isPowered())
					checker = new ActivateChecker(block, entity);
			}
			else if(data instanceof Lever)
			{
				if(!((Lever)data).isPowered())
					checker = new ActivateChecker(block, entity);
			}
			else if(data instanceof PressurePlate)
			{
				if(!((PressurePlate)data).isPressed())
					checker = new ActivateChecker(block, entity);
			}
			
			if(checker != null)
				Bukkit.getScheduler().runTask(TriggerItPlugin.getInstance(), checker);
		}
		
		private Block mBlock;
		private Entity mEntity;
		private MaterialData mData;
		
		private Block mSecondBlock;
		
		private ActivateChecker(Block block, Entity entity)
		{
			mBlock = block;
			mEntity = entity;
			mData = block.getState().getData();
		}
		
		private ActivateChecker(Block block, Block secondBlock, Entity entity)
		{
			mBlock = block;
			mSecondBlock = secondBlock;
			mEntity = entity;
			mData = block.getState().getData();
		}
		
		@Override
		public void run()
		{
			MaterialData newData = mBlock.getState().getData();
			if(mData.getItemType() != newData.getItemType())
				return;
			
			if(mData instanceof Button)
			{
				Button buttonOld = (Button)mData;
				Button buttonNew = (Button)newData;
				
				if(!buttonOld.isPowered() && buttonNew.isPowered())
					onBlockActivate(mBlock);
			}
			else if(mData instanceof Lever)
			{
				Lever leverOld = (Lever)mData;
				Lever leverNew = (Lever)newData;
				
				if(!leverOld.isPowered() && leverNew.isPowered())
					onBlockActivate(mBlock);
			}
			else if(mData instanceof PressurePlate)
			{
				PressurePlate plateOld = (PressurePlate)mData;
				PressurePlate plateNew = (PressurePlate)newData;
				
				if(!plateOld.isPressed() && plateNew.isPressed())
					onBlockActivate(mBlock);
			}
			else if(mData instanceof TripwireHook)
			{
				TripwireHook hookOld = (TripwireHook)mData;
				TripwireHook hookNew = (TripwireHook)newData;
				
				if(!hookOld.isActivated() && hookNew.isActivated())
				{
					onBlockActivate(mBlock);
					onBlockActivate(mSecondBlock);
				}
			}
		}
		
		private void onBlockActivate(Block block)
		{
			Set<BlockTrigger> triggers = getTriggersAt(block);
			
			ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
			builder.put("block", block)
				.put("location", block.getLocation())
				.put("world", block.getWorld());
			
			if(mEntity instanceof Player)
				builder.put("player", mEntity);
			else if(mEntity != null)
				builder.put("entity", mEntity);
			 
			Map<String, Object> args = builder.build();
			
			for(BlockTrigger trigger : triggers)
			{
				if(!trigger.isEnabled())
					continue;
				
				if(trigger.getType() == TriggerType.Activate)
					trigger.trigger(args);
			}
		}
	}
	
	public static class TriggerTypeFlag extends Flag<TriggerType>
	{
		@Override
		public TriggerType parse( Player sender, String[] args ) throws IllegalArgumentException, BadArgumentException
		{
			if(args.length != 1)
				throw new IllegalArgumentException("<type>");
			
			TriggerType type = mTriggerTypeMap.get(args[0].toLowerCase());
			if(type == null)
				throw new BadArgumentException(0, "Unknown block trigger type");
			
			return type;
		}

		@Override
		public List<String> tabComplete( Player sender, String[] args )
		{
			if(args.length == 1)
				return Utilities.matchString(args[0], mTriggerTypeNames);
			
			return null;
		}

		@Override
		public void save( ConfigurationSection section )
		{
			section.set("value", value.name());
		}

		@Override
		public void read( ConfigurationSection section ) throws InvalidConfigurationException
		{
			value = TriggerType.valueOf(section.getString("value"));
			if(value == null)
				throw new InvalidConfigurationException("Unknown trigger type " + section.getString("value"));
		}

		@Override
		public String getValueString()
		{
			return value.name();
		}
	}
}
