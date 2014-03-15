package au.com.addstar.triggerit.triggers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.WorldSpecific;
import au.com.addstar.triggerit.commands.BadArgumentException;

public class RegionTrigger extends Trigger implements WorldSpecific
{
	public enum RegionTriggerType
	{
		Enter,
		Leave,
		Transition
	}
	
	private static ArrayList<String> mTypeNames;
	
	static
	{
		mTypeNames = new ArrayList<String>();
		
		for(RegionTriggerType type : RegionTriggerType.values())
			mTypeNames.add(type.name());
	}
	
	private static WorldGuardPlugin plugin;
	
	private static HashMap<UUID, HashMultimap<ProtectedRegion, RegionTrigger>> mActiveTriggers = new HashMap<UUID, HashMultimap<ProtectedRegion,RegionTrigger>>();
	
	public static Set<RegionTrigger> getApplicableTriggers(UUID world, ProtectedRegion region)
	{
		HashMultimap<ProtectedRegion, RegionTrigger> worldMap = mActiveTriggers.get(world);
		if(worldMap == null)
			return null;
		
		return worldMap.get(region);
	}
	
	public static boolean isTriggersInWorld(UUID world)
	{
		return mActiveTriggers.containsKey(world);
	}
	
	private static void addTrigger(UUID world, ProtectedRegion region, RegionTrigger trigger)
	{
		HashMultimap<ProtectedRegion, RegionTrigger> worldMap = mActiveTriggers.get(world);
		if(worldMap == null)
		{
			worldMap = HashMultimap.create();
			mActiveTriggers.put(world, worldMap);
		}
		
		worldMap.put(region, trigger);
	}
	
	private static void removeTrigger(UUID world, ProtectedRegion region, RegionTrigger trigger)
	{
		HashMultimap<ProtectedRegion, RegionTrigger> worldMap = mActiveTriggers.get(world);
		if(worldMap == null)
			return;
		
		worldMap.remove(region, trigger);
		
		if(worldMap.isEmpty())
			mActiveTriggers.remove(world);
	}
	
	
	private ProtectedRegion mRegion;
	private String mRegionId;
	private UUID mWorld;
	
	private RegionTriggerType mType;

	public RegionTrigger() {}
	
	private RegionTrigger(String name)
	{
		super(name);
	}
	
	@Override
	public boolean isValid()
	{
		return mRegion != null;
	}
	
	@Override
	public void onLoad()
	{
		addTrigger(mWorld, mRegion, this);
	}
	
	@Override
	public void onUnload() 
	{
		removeTrigger(mWorld, mRegion, this);
	}
	
	@Override
	public UUID getWorld()
	{
		return mWorld;
	}
	
	public RegionTriggerType getType()
	{
		return mType;
	}
	
	@Override
	public String toString()
	{
		World world = Bukkit.getWorld(mWorld);
		
		if(world != null)
			return "Region trigger for " + mRegionId + " in " + world.getName();
		else
			return "Region trigger for " + mRegionId + " in " + mWorld;
	}
	
	@Override
	protected void load( ConfigurationSection section )
	{
		mWorld = UUID.fromString(section.getString("world"));
		mRegionId = section.getString("region");
	}
	
	@Override
	protected void save( ConfigurationSection section )
	{
		section.set("world", mWorld.toString());
		section.set("region", mRegionId);
	}
	
	
	public static RegionTrigger newTrigger(CommandSender sender, String name, String[] args) throws IllegalArgumentException, IllegalStateException, BadArgumentException
	{
		if(!(sender instanceof Player))
		{
			if(args.length != 3)
				throw new IllegalStateException("<region> <world> <triggertype>");
		}
		else if(args.length != 2 && args.length != 3)
			throw new IllegalStateException("<region> [world] <triggertype>");
		
		World world = null;
		if(sender instanceof Player)
			world = ((Player)sender).getWorld();
		
		if(args.length == 3)
		{
			world = Bukkit.getWorld(args[1]);
			if(world == null)
				throw new BadArgumentException(1, "Unknown world");
		}
		
		ProtectedRegion region;
		RegionManager manager = plugin.getRegionManager(world);
		
		if(manager == null)
		{
			if(args.length == 3)
				throw new BadArgumentException(1, "WorldGuard is not operational in that world.");
			else
				throw new BadArgumentException(0, "WorldGuard is not operational in this world.");
		}
		
		region = manager.getRegionExact(args[0]);
		if(region == null)
		{
			region = manager.getRegion(args[0]);
			if(region != null)
				throw new BadArgumentException(0, "Unknown region. Did you mean " + ChatColor.YELLOW + region.getId());
			else
				throw new BadArgumentException(0, "Unknown region.");
		}
		
		RegionTriggerType type = null;
		for (RegionTriggerType t : RegionTriggerType.values())
		{
			if(t.name().equalsIgnoreCase(args[args.length-1]))
			{
				type = t;
				break;						
			}
		}
		
		if(type == null)
		{
			ArrayList<String> lines = new ArrayList<String>();
			lines.add(ChatColor.GOLD + "Available types: ");
			
			String line = "";
			boolean odd = true;
			for(RegionTriggerType t : RegionTriggerType.values())
			{
				if(!line.isEmpty())
					line += ChatColor.GRAY + ", ";
				
				if(odd)
					line += ChatColor.WHITE;
				else
					line += ChatColor.GRAY;
				
				line += t.name();
				odd = !odd;
			}
			lines.add(line);
			
			throw new BadArgumentException(args.length-1, "Unknown trigger type").addInfo(lines);
		}
		
		RegionTrigger trigger = new RegionTrigger(name);
		trigger.mRegion = region;
		trigger.mRegionId = region.getId();
		trigger.mWorld = world.getUID();
		trigger.mType = type;
		
		sender.sendMessage(ChatColor.GREEN + "Successfully created a Region trigger for " + region.getId() + " in " + world.getName());
		
		return trigger;
	}
	
	public static List<String> tabComplete(CommandSender sender, String[] args)
	{
		if(!(sender instanceof Player))
		{
			if(args.length == 3)
				return Utilities.matchString(args[2], mTypeNames);
		}
		else if(args.length == 2 || args.length == 3)
			return Utilities.matchString(args[args.length-1], mTypeNames);
		
		return null;
	}
	
	public static void initializeType(TriggerItPlugin plugin)
	{
		RegionTrigger.plugin = WorldGuardPlugin.getPlugin(WorldGuardPlugin.class);
		Bukkit.getPluginManager().registerEvents(new RegionTriggerListener(), plugin);
	}
	
	private static class RegionTriggerListener implements Listener
	{
		private WeakHashMap<Player, ApplicableRegionSet> mPlayerRegions = new WeakHashMap<Player, ApplicableRegionSet>();
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerTeleport(PlayerTeleportEvent event)
		{
			handleMove(event.getPlayer(), event.getFrom(), event.getTo());
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerMove(PlayerMoveEvent event)
		{
			if(event.getFrom().getWorld().equals(event.getTo().getWorld()))
			{
				if(event.getFrom().distanceSquared(event.getTo()) < 0.00005)
					return;
			}
			
			handleMove(event.getPlayer(), event.getFrom(), event.getTo());
		}
		
		private void onLeaveRegion(Player player, World world, ProtectedRegion region)
		{
			Set<RegionTrigger> triggers = getApplicableTriggers(world.getUID(), region);
			
			if(triggers == null)
				return;
			
			Map<String, Object> map = new ImmutableMap.Builder<String, Object>()
				.put("player", player)
				.put("world", world)
				.put("location", player.getLocation())
				.put("region", region.getId())
				.build();
			
			for(RegionTrigger trigger : triggers)
			{
				if(trigger.getType() == RegionTriggerType.Leave || trigger.getType() == RegionTriggerType.Transition)
					trigger.trigger(map);
			}
		}
		
		private void onEnterRegion(Player player, World world, ProtectedRegion region)
		{
			Set<RegionTrigger> triggers = getApplicableTriggers(world.getUID(), region);
			
			if(triggers == null)
				return;
			
			Map<String, Object> map = new ImmutableMap.Builder<String, Object>()
				.put("player", player)
				.put("world", world)
				.put("location", player.getLocation())
				.put("region", region.getId())
				.build();
			
			for(RegionTrigger trigger : triggers)
			{
				if(trigger.getType() == RegionTriggerType.Enter || trigger.getType() == RegionTriggerType.Transition)
					trigger.trigger(map);
			}
		}
		
		private void handleMove(Player player, Location from, Location to)
		{
			ApplicableRegionSet oldRegions = mPlayerRegions.get(player);
			
			if(!from.getWorld().equals(to.getWorld()))
			{
				for(ProtectedRegion region : oldRegions)
					onLeaveRegion(player, from.getWorld(), region);
				mPlayerRegions.remove(player);
				oldRegions = null;
			}
			
			if(!isTriggersInWorld(to.getWorld().getUID()))
				return;
			
			RegionManager manager = plugin.getRegionManager(to.getWorld());
			if(manager == null)
				return;

			ApplicableRegionSet newRegions = manager.getApplicableRegions(to);
			
			if(oldRegions == null)
			{
				for(ProtectedRegion region : newRegions)
					onEnterRegion(player, to.getWorld(), region);
			}
			else
			{
				ArrayList<ProtectedRegion> newList = new ArrayList<ProtectedRegion>(newRegions.size());
				for(ProtectedRegion region : newRegions)
					newList.add(region);
				
				ArrayList<ProtectedRegion> oldList = new ArrayList<ProtectedRegion>(oldRegions.size());
				for(ProtectedRegion region : oldRegions)
					oldList.add(region);
				
				ArrayList<ProtectedRegion> common = new ArrayList<ProtectedRegion>(newList);
				common.retainAll(oldList);
				
				oldList.removeAll(common);
				newList.removeAll(common);
				
				for(ProtectedRegion region : oldList)
					onLeaveRegion(player, from.getWorld(), region);
				
				for(ProtectedRegion region : newList)
					onEnterRegion(player, to.getWorld(), region);
			}
			mPlayerRegions.put(player, newRegions);
		}
	}
}
