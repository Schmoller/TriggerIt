package au.com.addstar.triggerit.minigames.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableSet;
import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.gametypes.MinigameType;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;

import au.com.addstar.triggerit.Action;
import au.com.addstar.triggerit.TriggerIt;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.targets.Target;

public class EndMinigameAction implements Action
{
	private String mMinigame;
	private Target<? extends CommandSender> mWinners;
	
	@Override
	public void execute( Map<String, Object> arguments )
	{
		Minigame minigame = Minigames.plugin.mdata.getMinigame(mMinigame);
		
		if(minigame == null)
			return;
		
		if(minigame.getType() != MinigameType.TREASURE_HUNT)
		{
			// TODO: Handle teams somehow
			
			ArrayList<MinigamePlayer> winners = new ArrayList<MinigamePlayer>();
			ArrayList<MinigamePlayer> losers = new ArrayList<MinigamePlayer>();
			
			for(CommandSender sender : mWinners.getTargets())
			{
				MinigamePlayer player = Minigames.plugin.pdata.getMinigamePlayer((Player)sender);
				if(player == null)
					continue;
				
				if(player.getMinigame() == minigame)
					winners.add(player);
			}
			
			for(MinigamePlayer player : minigame.getPlayers())
			{
				if(!winners.contains(player))
					losers.add(player);
			}
			
			Minigames.plugin.pdata.endMinigame(minigame, winners, losers);
		}
	}

	@Override
	public void save( ConfigurationSection section )
	{
		section.set("minigame", mMinigame);
		section.set("winners", mWinners.toString());
	}

	@Override
	public void load( ConfigurationSection section ) throws InvalidConfigurationException
	{
		mMinigame = section.getString("minigame");
		mWinners = TriggerIt.parseTargets(section.getString("winners"), CommandSender.class, ImmutableSet.of(Player.class));
	}

	@Override
	public String[] describe()
	{
		return new String[] {
			ChatColor.GRAY + "Minigame: " + ChatColor.YELLOW + mMinigame,
			ChatColor.GRAY + "Winners: " + ChatColor.YELLOW + mWinners.describe()
		};
	}
	
	@Override
	public String toString()
	{
		return "End the minigame " + mMinigame + " with " + mWinners.describe() + " as the winners.";
	}
	
	public static Action newAction(CommandSender sender, String[] args) throws IllegalStateException, BadArgumentException
	{
		if(args.length != 2)
			throw new IllegalStateException("<minigame> <winners>");
		
		Minigame minigame = Minigames.plugin.mdata.getMinigame(args[0]);
		if(minigame == null)
			throw new BadArgumentException(0, "Unknown minigame");
		
		try
		{
			Target<? extends CommandSender> winners = TriggerIt.parseTargets(args[1], CommandSender.class, ImmutableSet.of(Player.class));
		
			EndMinigameAction action = new EndMinigameAction();
			action.mMinigame = minigame.getName();
			action.mWinners = winners;
			
			return action;
		}
		catch(IllegalArgumentException e)
		{
			throw new BadArgumentException(1, e.getMessage());
		}
	}
	
	public static List<String> tabComplete(CommandSender sender, String[] args)
	{
		if(args.length == 1)
			return Utilities.matchString(args[0], Minigames.plugin.mdata.getAllMinigames().keySet());
		
		return null;
	}
}
