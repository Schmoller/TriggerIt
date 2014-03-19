package au.com.addstar.triggerit.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableMap;

import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.flags.Flag;
import au.com.addstar.triggerit.flags.FlagIO;
import au.com.addstar.triggerit.flags.PlayerTargetFlag;
import au.com.addstar.triggerit.targets.AllTarget;
import au.com.addstar.triggerit.targets.CSParametricTarget;
import au.com.addstar.triggerit.targets.TargetCS;

public class PlayerTrigger extends Trigger
{
	public enum PlayerTriggerType
	{
		Death,
		Respawn,
		LevelChange,
		Click,
		LeftClick,
		RightClick,
		PlaceBlock,
		RemoveBlock,
		ChangeBlock,
		DropItem,
		XpChange,
		Eat,
		Join,
		Quit
	}
	
	private static ArrayList<String> mTypeNames;
	
	static
	{
		mTypeNames = new ArrayList<String>();
		
		for(PlayerTriggerType type : PlayerTriggerType.values())
			mTypeNames.add(type.name());
	}
	
	private static ArrayList<PlayerTrigger> mTriggers = new ArrayList<PlayerTrigger>();
	
	private PlayerTargetFlag mTarget = new PlayerTargetFlag();
	private PlayerTriggerTypeFlag mType = new PlayerTriggerTypeFlag();
	
	public PlayerTrigger() 
	{
		addFlag("target", mTarget);
		addFlag("type", mType);
	}
	
	private PlayerTrigger(String name)
	{
		super(name);
		
		addFlag("target", mTarget);
		addFlag("type", mType);
	}
	
	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	protected void save( ConfigurationSection section )
	{
	}

	@Override
	protected void load( ConfigurationSection section ) throws InvalidConfigurationException
	{
	}
	
	@Override
	public void onLoad()
	{
		mTriggers.add(this);
	}
	
	@Override
	public void onUnload()
	{
		mTriggers.remove(this);
	}
	

	@Override
	protected String[] describeTrigger()
	{
		return new String[] {
			ChatColor.GRAY + "Trigger Type: " + ChatColor.YELLOW + mType.getValueString(),
			ChatColor.GRAY + "Target: " + ChatColor.YELLOW + mTarget.getValueString()
		};
	}
	
	public static PlayerTrigger newTrigger(CommandSender sender, String name, String[] args) throws IllegalStateException, BadArgumentException
	{
		if(args.length != 1 && args.length != 2)
			throw new IllegalStateException("<triggertype> [target]");
		
		PlayerTriggerType type = null;
		for(PlayerTriggerType t : PlayerTriggerType.values())
		{
			if(t.name().equalsIgnoreCase(args[0]))
			{
				type = t;
				break;
			}
		}
		
		if(type == null)
			throw new BadArgumentException(0, "Unknown player trigger type");
		
		TargetCS target = null;
		
		if(args.length == 2)
		{
			try
			{
				target = TargetCS.parseTargets(args[1], false);
			}
			catch(IllegalArgumentException e)
			{
				throw new BadArgumentException(1, e.getMessage());
			}
			
			if(target instanceof CSParametricTarget)
				throw new BadArgumentException(1, "Cannot use argument based targets here");
		}
		else
			target = new AllTarget(false);
		
		PlayerTrigger trigger = new PlayerTrigger(name);
		trigger.mTarget.setValue(target);
		trigger.mType.setValue(type);
		
		sender.sendMessage(ChatColor.GREEN + "Created player trigger for " + type.name() + " limited to " + target.describe());
		
		return trigger;
	}
	
	public static List<String> tabComplete(CommandSender sender, String[] args)
	{
		if(args.length == 1)
			return Utilities.matchString(args[0], mTypeNames);
		
		return null;
	}
	
	public static void initializeType(TriggerItPlugin plugin)
	{
		Bukkit.getPluginManager().registerEvents(new PlayerTriggerListener(), plugin);
		FlagIO.addKnownType("PlayerTriggerType", PlayerTriggerTypeFlag.class);
	}

	private static class PlayerTriggerListener implements Listener
	{
		private void handleTrigger(Player player, Map<String, Object> args, PlayerTriggerType... types)
		{
			for(PlayerTrigger trigger : mTriggers)
			{
				PlayerTriggerType type = trigger.mType.getValue();
				
				boolean ok = false;
				for(PlayerTriggerType t : types)
				{
					if(type == t)
					{
						ok = true;
						break;
					}
				}
				
				if(!ok)
					continue;
				
				TargetCS target = trigger.mTarget.getValue();
				
				if(!(target instanceof AllTarget))
				{
					if(!target.getTargets().contains(player))
						continue;
				}
				
				trigger.trigger(args);
			}
		}
		
		private boolean areNoTriggers()
		{
			return mTriggers.isEmpty();
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerJoin(PlayerJoinEvent event)
		{
			if(areNoTriggers())
				return;
			
			handleTrigger(event.getPlayer(), new ImmutableMap.Builder<String, Object>()
				.put("player", event.getPlayer())
				.put("location", event.getPlayer().getLocation())
				.put("world", event.getPlayer().getWorld())
				.put("message", event.getJoinMessage())
				.build(),
				
				PlayerTriggerType.Join);
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerQuit(PlayerQuitEvent event)
		{
			if(areNoTriggers())
				return;
			
			handleTrigger(event.getPlayer(), new ImmutableMap.Builder<String, Object>()
				.put("player", event.getPlayer())
				.put("location", event.getPlayer().getLocation())
				.put("world", event.getPlayer().getWorld())
				.put("message", event.getQuitMessage())
				.build(),
				
				PlayerTriggerType.Quit);
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerDeath(PlayerDeathEvent event)
		{
			if(areNoTriggers())
				return;
			
			handleTrigger(event.getEntity(), new ImmutableMap.Builder<String, Object>()
				.put("player", event.getEntity())
				.put("location", event.getEntity().getLocation())
				.put("world", event.getEntity().getWorld())
				.put("message", event.getDeathMessage())
				.put("xp", event.getDroppedExp())
				.build(),
				
				PlayerTriggerType.Death);
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerRespawn(PlayerRespawnEvent event)
		{
			if(areNoTriggers())
				return;
			
			handleTrigger(event.getPlayer(), new ImmutableMap.Builder<String, Object>()
				.put("player", event.getPlayer())
				.put("location", event.getRespawnLocation())
				.put("world", event.getRespawnLocation().getWorld())
				.build(),
				
				PlayerTriggerType.Respawn);
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerLevelChange(PlayerLevelChangeEvent event)
		{
			if(areNoTriggers())
				return;
			
			handleTrigger(event.getPlayer(), new ImmutableMap.Builder<String, Object>()
				.put("player", event.getPlayer())
				.put("location", event.getPlayer().getLocation())
				.put("world", event.getPlayer().getWorld())
				.put("levelOld", event.getOldLevel())
				.put("level", event.getNewLevel())
				.build(),
				
				PlayerTriggerType.LevelChange);
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerExpChange(PlayerExpChangeEvent event)
		{
			if(areNoTriggers())
				return;
			
			handleTrigger(event.getPlayer(), new ImmutableMap.Builder<String, Object>()
				.put("player", event.getPlayer())
				.put("location", event.getPlayer().getLocation())
				.put("world", event.getPlayer().getWorld())
				.put("gained", event.getAmount())
				.build(),
				
				PlayerTriggerType.XpChange);
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerDropItem(PlayerDropItemEvent event)
		{
			if(areNoTriggers())
				return;
			
			handleTrigger(event.getPlayer(), new ImmutableMap.Builder<String, Object>()
				.put("player", event.getPlayer())
				.put("location", event.getPlayer().getLocation())
				.put("world", event.getPlayer().getWorld())
				.put("item", event.getItemDrop())
				.build(),
				
				PlayerTriggerType.DropItem);
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerEat(PlayerItemConsumeEvent event)
		{
			if(areNoTriggers())
				return;
			
			handleTrigger(event.getPlayer(), new ImmutableMap.Builder<String, Object>()
				.put("player", event.getPlayer())
				.put("location", event.getPlayer().getLocation())
				.put("world", event.getPlayer().getWorld())
				.put("item", event.getItem())
				.build(),
				
				PlayerTriggerType.Eat);
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerRemoveBlock(BlockBreakEvent event)
		{
			if(areNoTriggers())
				return;
			
			handleTrigger(event.getPlayer(), new ImmutableMap.Builder<String, Object>()
				.put("player", event.getPlayer())
				.put("location", event.getPlayer().getLocation())
				.put("world", event.getPlayer().getWorld())
				.put("block", event.getBlock())
				.build(),
				
				PlayerTriggerType.RemoveBlock,
				PlayerTriggerType.ChangeBlock);
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerPlaceBlock(BlockPlaceEvent event)
		{
			if(areNoTriggers())
				return;
			
			handleTrigger(event.getPlayer(), new ImmutableMap.Builder<String, Object>()
				.put("player", event.getPlayer())
				.put("location", event.getPlayer().getLocation())
				.put("world", event.getPlayer().getWorld())
				.put("block", event.getBlock())
				.build(),
				
				PlayerTriggerType.PlaceBlock,
				PlayerTriggerType.ChangeBlock);
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerInteract(PlayerInteractEvent event)
		{
			if(areNoTriggers())
				return;
			
			Block block = event.getClickedBlock();
			ItemStack item = event.getItem();
			
			ImmutableMap.Builder<String,Object> builder = ImmutableMap.builder();
			
			builder.put("player", event.getPlayer())
				.put("world", event.getPlayer().getWorld());
			
			if(block != null)
			{
				builder.put("block", block)
					.put("location", block.getLocation());
			}
			else
			{
				builder.put("location", event.getPlayer().getLocation());
			}
			
			if(item != null)
				builder.put("item", event.getItem());
			
			if(event.getAction() == Action.LEFT_CLICK_BLOCK)
				handleTrigger(event.getPlayer(), builder.build(), PlayerTriggerType.Click, PlayerTriggerType.LeftClick);
			else if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
				handleTrigger(event.getPlayer(), builder.build(), PlayerTriggerType.Click, PlayerTriggerType.RightClick);
		}
	}
	
	private static class PlayerTriggerTypeFlag extends Flag<PlayerTriggerType>
	{
		@Override
		public PlayerTriggerType parse( Player sender, String[] args ) throws IllegalArgumentException, BadArgumentException
		{
			if(args.length != 1)
				throw new IllegalArgumentException("<type>");
			
			PlayerTriggerType type = null;
			
			for(PlayerTriggerType t : PlayerTriggerType.values())
			{
				if(t.name().equalsIgnoreCase(args[0]))
				{
					type = t;
					break;
				}
			}
			
			if(type == null)
				throw new BadArgumentException(0, "Unknown player trigger type");
			
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
			value = PlayerTriggerType.valueOf(section.getString("value"));
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
