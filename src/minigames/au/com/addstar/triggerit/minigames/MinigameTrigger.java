package au.com.addstar.triggerit.minigames;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.events.EndMinigameEvent;
import com.pauldavdesign.mineauz.minigames.events.EndTeamMinigameEvent;
import com.pauldavdesign.mineauz.minigames.events.JoinMinigameEvent;
import com.pauldavdesign.mineauz.minigames.events.QuitMinigameEvent;
import com.pauldavdesign.mineauz.minigames.events.RevertCheckpointEvent;
import com.pauldavdesign.mineauz.minigames.events.SpectateMinigameEvent;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;

import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.commands.BadArgumentException;

public class MinigameTrigger extends Trigger
{
	public enum TriggerType
	{
		Join,
		Quit,
		Win,
		TeamWin,
		Spectate,
		Revert
	}
	
	private static ArrayList<String> mTypeNames = new ArrayList<String>();
	
	static
	{
		for(TriggerType type : TriggerType.values())
			mTypeNames.add(type.name());
	}

	private static HashMultimap<String, MinigameTrigger> mTriggers = HashMultimap.create();
	
	private String mMinigame;
	private TriggerType mType;
	
	public MinigameTrigger() {}
	
	private MinigameTrigger(String name)
	{
		super(name);
	}
	
	
	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	protected void save( ConfigurationSection section )
	{
		if(mMinigame != null)
			section.set("minigame", mMinigame);
		
		section.set("mgtype", mType.name());
	}

	@Override
	protected void load( ConfigurationSection section ) throws InvalidConfigurationException
	{
		if(section.isString("minigame"))
			mMinigame = section.getString("minigame").toLowerCase();
		else
			mMinigame = null;
		
		mType = TriggerType.valueOf(section.getString("mgtype"));
	}

	@Override
	protected String[] describeTrigger()
	{
		return new String[] {
			ChatColor.GRAY + "Minigame: " + ChatColor.YELLOW + (mMinigame == null ? "Any Minigame" : mMinigame),
			ChatColor.GRAY + "Trigger Type: " + ChatColor.YELLOW + mType.name()
		};
	}
	
	@Override
	public void onLoad()
	{
		mTriggers.put(mMinigame, this);
	}
	
	@Override
	public void onUnload()
	{
		mTriggers.remove(mMinigame, this);
	}
	
	public static MinigameTrigger newTrigger(CommandSender sender, String name, String[] args) throws BadArgumentException, IllegalStateException
	{
		if(args.length != 2)
			throw new IllegalStateException("(<minigame>|*) <type>");
		
		Minigame minigame = Minigames.plugin.mdata.getMinigame(args[0]);
		if(minigame == null && !args[0].equals("*"))
			throw new BadArgumentException(0, "Unknown minigame");
		
		TriggerType type = null;
		
		for(TriggerType t : TriggerType.values())
		{
			if(t.name().equalsIgnoreCase(args[1]))
			{
				type = t;
				break;
			}
		}
		
		if(type == null)
			throw new BadArgumentException(1, "Unknown minigame trigger type");
		
		MinigameTrigger trigger = new MinigameTrigger(name);
		trigger.mMinigame = (minigame == null ? null : minigame.getName().toLowerCase());
		trigger.mType = type;
		
		sender.sendMessage(ChatColor.GREEN + "Created a new Minigame Trigger on " + type.name() + " for " + (minigame == null ? "any minigame" : minigame.getName()));
		
		return trigger;
	}
	
	public static List<String> tabComplete(CommandSender sender, String[] args)
	{
		if(args.length == 1)
			return Utilities.matchString(args[0], Minigames.plugin.mdata.getAllMinigames().keySet());
		else if(args.length == 2)
			return Utilities.matchString(args[1], mTypeNames);
		return null;
	}
	
	public static void initializeType(TriggerItPlugin plugin)
	{
		Bukkit.getPluginManager().registerEvents(new MinigameListener(), plugin);
	}
	
	private static class MinigameListener implements Listener
	{
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		private void onMinigameJoin(JoinMinigameEvent event)
		{
			Set<MinigameTrigger> triggers = mTriggers.get(event.getMinigame().getName().toLowerCase());
			
			Map<String, Object> map = new ImmutableMap.Builder<String, Object>()
				.put("player", event.getPlayer())
				.put("location", event.getPlayer().getLocation())
				.put("world", event.getPlayer().getWorld())
				.put("minigame", event.getMinigame().getName())
				.build();
			
			for(MinigameTrigger trigger : triggers)
			{
				if(trigger.mType == TriggerType.Join)
					trigger.trigger(map);
			}
			
			triggers = mTriggers.get(null);
			
			for(MinigameTrigger trigger : triggers)
			{
				if(trigger.mType == TriggerType.Join)
					trigger.trigger(map);
			}
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		private void onMinigameWin(EndMinigameEvent event)
		{
			Set<MinigameTrigger> triggers = mTriggers.get(event.getMinigame().getName().toLowerCase());
			
			ArrayList<Player> winners = new ArrayList<Player>(event.getWinners().size());
			for(MinigamePlayer player : event.getWinners())
				winners.add(player.getPlayer());
			
			ArrayList<Player> losers = new ArrayList<Player>(event.getLosers().size());
			for(MinigamePlayer player : event.getLosers())
				losers.add(player.getPlayer());
			
			Map<String, Object> map = new ImmutableMap.Builder<String, Object>()
				.put("winners", winners)
				.put("losers", losers)
				.put("minigame", event.getMinigame().getName())
				.build();
			
			for(MinigameTrigger trigger : triggers)
			{
				if(trigger.mType == TriggerType.Win)
					trigger.trigger(map);
			}
			
			triggers = mTriggers.get(null);
			
			for(MinigameTrigger trigger : triggers)
			{
				if(trigger.mType == TriggerType.Win)
					trigger.trigger(map);
			}
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		private void onMinigameWin(EndTeamMinigameEvent event)
		{
			Set<MinigameTrigger> triggers = mTriggers.get(event.getMinigame().getName().toLowerCase());
			
			ArrayList<Player> winners = new ArrayList<Player>(event.getWinnningPlayers().size());
			for(MinigamePlayer player : event.getWinnningPlayers())
				winners.add(player.getPlayer());
			
			ArrayList<Player> losers = new ArrayList<Player>(event.getLosingPlayers().size());
			for(MinigamePlayer player : event.getLosingPlayers())
				losers.add(player.getPlayer());
			
			Map<String, Object> map = new ImmutableMap.Builder<String, Object>()
				.put("team", (event.getWinningTeamInt() == 0 ? "Red" : "Blue"))
				.put("winners", winners)
				.put("losers", losers)
				.put("minigame", event.getMinigame().getName())
				.build();
			
			for(MinigameTrigger trigger : triggers)
			{
				if(trigger.mType == TriggerType.TeamWin)
					trigger.trigger(map);
			}
			
			triggers = mTriggers.get(null);
			
			for(MinigameTrigger trigger : triggers)
			{
				if(trigger.mType == TriggerType.TeamWin)
					trigger.trigger(map);
			}
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		private void onMinigameQuit(QuitMinigameEvent event)
		{
			Set<MinigameTrigger> triggers = mTriggers.get(event.getMinigame().getName().toLowerCase());
			
			Map<String, Object> map = new ImmutableMap.Builder<String, Object>()
				.put("player", event.getPlayer())
				.put("location", event.getPlayer().getLocation())
				.put("world", event.getPlayer().getWorld())
				.put("minigame", event.getMinigame().getName())
				.build();
			
			for(MinigameTrigger trigger : triggers)
			{
				if(trigger.mType == TriggerType.Quit)
					trigger.trigger(map);
			}
			
			triggers = mTriggers.get(null);
			
			for(MinigameTrigger trigger : triggers)
			{
				if(trigger.mType == TriggerType.Quit)
					trigger.trigger(map);
			}
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		private void onMinigameRevert(RevertCheckpointEvent event)
		{
			MinigamePlayer player = event.getMinigamePlayer();
			if(!player.isInMinigame())
				return;
			
			Set<MinigameTrigger> triggers = mTriggers.get(player.getMinigame().getName().toLowerCase());
			
			Map<String, Object> map = new ImmutableMap.Builder<String, Object>()
				.put("player", event.getPlayer())
				.put("location", event.getPlayer().getLocation())
				.put("world", event.getPlayer().getWorld())
				.put("minigame", player.getMinigame().getName())
				.build();
			
			for(MinigameTrigger trigger : triggers)
			{
				if(trigger.mType == TriggerType.Revert)
					trigger.trigger(map);
			}
			
			triggers = mTriggers.get(null);
			
			for(MinigameTrigger trigger : triggers)
			{
				if(trigger.mType == TriggerType.Revert)
					trigger.trigger(map);
			}
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		private void onMinigameSpectate(SpectateMinigameEvent event)
		{
			MinigamePlayer player = event.getMinigamePlayer();
			if(!player.isInMinigame())
				return;
			
			Set<MinigameTrigger> triggers = mTriggers.get(player.getMinigame().getName().toLowerCase());
			
			Map<String, Object> map = new ImmutableMap.Builder<String, Object>()
				.put("player", event.getPlayer())
				.put("location", event.getPlayer().getLocation())
				.put("world", event.getPlayer().getWorld())
				.put("minigame", event.getMinigame().getName())
				.build();
			
			for(MinigameTrigger trigger : triggers)
			{
				if(trigger.mType == TriggerType.Spectate)
					trigger.trigger(map);
			}
			
			triggers = mTriggers.get(null);
			
			for(MinigameTrigger trigger : triggers)
			{
				if(trigger.mType == TriggerType.Spectate)
					trigger.trigger(map);
			}
		}
	}

}
