package au.com.addstar.triggerit.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.WorldSpecific;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.flags.Flag;
import au.com.addstar.triggerit.flags.FlagIO;

public class WorldTrigger extends Trigger implements WorldSpecific
{
	public enum WorldTriggerType
	{
		Enter,
		Leave,
		Transition
	}
	
	private static ArrayList<String> mTypeNames;
	
	static
	{
		mTypeNames = new ArrayList<String>();
		
		for(WorldTriggerType type : WorldTriggerType.values())
			mTypeNames.add(type.name());
	}
	
	private static HashMultimap<UUID, WorldTrigger> mActiveTriggers = HashMultimap.create();
	
	private UUID mWorld;
	private WorldTriggerTypeFlag mType = new WorldTriggerTypeFlag();

	public WorldTrigger() 
	{
		addFlag("type", mType);
	}
	
	private WorldTrigger(String name)
	{
		super(name);
		
		addFlag("type", mType);
	}
	
	@Override
	public boolean isValid()
	{
		return true;
	}
	
	@Override
	public void onLoad()
	{
		mActiveTriggers.put(mWorld, this);
	}
	
	@Override
	public void onUnload() 
	{
		mActiveTriggers.remove(mWorld, this);
	}
	
	@Override
	public UUID getWorld()
	{
		return mWorld;
	}
	
	public WorldTriggerType getType()
	{
		return mType.getValue();
	}
	
	@Override
	protected void load( ConfigurationSection section )
	{
		if(section.isString("world"))
			mWorld = UUID.fromString(section.getString("world"));
		else
			mWorld = null;
	}
	
	@Override
	protected void save( ConfigurationSection section )
	{
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
			ChatColor.GRAY + "World: " + ChatColor.YELLOW + worldName,
			ChatColor.GRAY + "Trigger Type: " + ChatColor.YELLOW + mType.getValueString()
		};
	}
	
	public static WorldTrigger newTrigger(CommandSender sender, String name, String[] args) throws IllegalArgumentException, IllegalStateException, BadArgumentException
	{
		if(args.length != 1 && args.length != 2)
			throw new IllegalStateException("<triggertype> [world]");
		
		World world = null;
		
		if(args.length == 2)
		{
			world = Bukkit.getWorld(args[1]);
			if(world == null)
				throw new BadArgumentException(1, "Unknown world");
		}
		
		WorldTriggerType type = null;
		for (WorldTriggerType t : WorldTriggerType.values())
		{
			if(t.name().equalsIgnoreCase(args[0]))
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
			for(WorldTriggerType t : WorldTriggerType.values())
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
			
			throw new BadArgumentException(0, "Unknown trigger type").addInfo(lines);
		}
		
		WorldTrigger trigger = new WorldTrigger(name);
		trigger.mWorld = (world != null ? world.getUID() : null);
		trigger.mType.setValue(type);
		
		sender.sendMessage(ChatColor.GREEN + "Successfully created a World trigger for " + type.name() + " in " + (world != null ? world.getName() : "any world"));
		
		return trigger;
	}
	
	public static List<String> tabComplete(CommandSender sender, String[] args)
	{
		if(args.length == 1)
			return Utilities.matchString(args[0], mTypeNames);
		else if(args.length == 2)
		{
			ArrayList<String> names = new ArrayList<String>();
			for(World world : Bukkit.getWorlds())
				names.add(world.getName());
			return Utilities.matchString(args[1], names);
		}
		
		return null;
	}
	
	public static void initializeType(TriggerItPlugin plugin)
	{
		Bukkit.getPluginManager().registerEvents(new WorldTriggerListener(), plugin);
		FlagIO.addKnownType("WorldTriggerType", WorldTriggerTypeFlag.class);
	}
	
	private static class WorldTriggerListener implements Listener
	{
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerChangeWorld(PlayerChangedWorldEvent event)
		{
			Set<WorldTrigger> triggers = mActiveTriggers.get(event.getPlayer().getWorld().getUID());
			
			Map<String, Object> map = new ImmutableMap.Builder<String, Object>()
				.put("player", event.getPlayer())
				.put("location", event.getPlayer().getLocation())
				.put("world", event.getPlayer().getWorld())
				.put("from", event.getFrom())
				.put("to", event.getPlayer().getWorld())
				.build();
			
			for(WorldTrigger trigger : triggers)
			{
				if(trigger.getType() == WorldTriggerType.Enter || trigger.getType() == WorldTriggerType.Transition)
					trigger.trigger(map);
			}
			
			triggers = mActiveTriggers.get(event.getFrom().getUID());
			
			for(WorldTrigger trigger : triggers)
			{
				if(trigger.getType() == WorldTriggerType.Leave || trigger.getType() == WorldTriggerType.Transition)
					trigger.trigger(map);
			}
			
			triggers = mActiveTriggers.get(null);
			
			for(WorldTrigger trigger : triggers)
				trigger.trigger(map);
		}
	}
	
	private static class WorldTriggerTypeFlag extends Flag<WorldTriggerType>
	{
		@Override
		public WorldTriggerType parse( Player sender, String[] args ) throws IllegalArgumentException, BadArgumentException
		{
			if(args.length != 1)
				throw new IllegalArgumentException("<type>");
			
			WorldTriggerType type = null;
			
			for(WorldTriggerType t : WorldTriggerType.values())
			{
				if(t.name().equalsIgnoreCase(args[0]))
				{
					type = t;
					break;
				}
			}
			
			if(type == null)
				throw new BadArgumentException(0, "Unknown world trigger type");
			
			return type;
		}

		@Override
		public List<String> tabComplete( Player sender, String[] args )
		{
			if(args.length == 1)
				return Utilities.matchString(args[0], mTypeNames);
			
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
			value = WorldTriggerType.valueOf(section.getString("value"));
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
