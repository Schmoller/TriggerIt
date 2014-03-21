package au.com.addstar.triggerit.minigames.actions;

import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.MultiplayerTimer;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;

import au.com.addstar.triggerit.Action;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.commands.BadArgumentException;

public class StartMinigameAction implements Action
{
	private String mMinigame;
	
	@Override
	public void execute( Map<String, Object> arguments )
	{
		Minigame minigame = Minigames.plugin.mdata.getMinigame(mMinigame);
		
		if(minigame == null)
			return;
		
		switch(minigame.getType())
		{
		case TREASURE_HUNT:
			if(minigame.getThTimer() == null)
			{
				Minigames.plugin.mdata.startGlobalMinigame(minigame.getName());
				minigame.setEnabled(true);
				minigame.saveMinigame();
			}
			break;
		case SINGLEPLAYER:
			// Cant start this
			break;
		default:
			if(minigame.getMpTimer() == null)
			{
				minigame.setMpTimer(new MultiplayerTimer(minigame));
				minigame.getMpTimer().setPlayerWaitTime(0);
				minigame.getMpTimer().startTimer();
			}
			else if(minigame.getMpTimer().getPlayerWaitTimeLeft() > 0)
			{
				minigame.getMpTimer().setPlayerWaitTime(0);
			}
			break;
		}
	}

	@Override
	public void save( ConfigurationSection section )
	{
		section.set("minigame", mMinigame);
	}

	@Override
	public void load( ConfigurationSection section ) throws InvalidConfigurationException
	{
		mMinigame = section.getString("minigame");
	}

	@Override
	public String[] describe()
	{
		return new String[] {
			ChatColor.GRAY + "Minigame: " + ChatColor.YELLOW + mMinigame	
		};
	}
	
	@Override
	public String toString()
	{
		return "Start the minigame " + mMinigame;
	}
	
	public static Action newAction(CommandSender sender, String[] args) throws IllegalStateException, BadArgumentException
	{
		if(args.length != 1)
			throw new IllegalStateException("<minigame>");
		
		Minigame minigame = Minigames.plugin.mdata.getMinigame(args[0]);
		if(minigame == null)
			throw new BadArgumentException(0, "Unknown minigame");
		
		StartMinigameAction action = new StartMinigameAction();
		action.mMinigame = minigame.getName();
		
		return action;
	}
	
	public static List<String> tabComplete(CommandSender sender, String[] args)
	{
		if(args.length == 1)
			return Utilities.matchString(args[0], Minigames.plugin.mdata.getAllMinigames().keySet());
		
		return null;
	}
}
