package au.com.addstar.triggerit.minigames.actions;

import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.gametypes.MinigameType;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;

import au.com.addstar.triggerit.Action;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.targets.Target;
import au.com.addstar.triggerit.targets.TargetCS;

public class JoinMinigameAction implements Action
{
	private String mMinigame;
	private TargetCS mTarget;
	
	@Override
	public void execute( Map<String, Object> arguments )
	{
		Minigame minigame = Minigames.plugin.mdata.getMinigame(mMinigame);
		
		if(minigame == null)
			return;
		
		if(minigame.getType() != MinigameType.TREASURE_HUNT)
		{
			for(CommandSender sender : mTarget.getTargets())
			{
				MinigamePlayer player = Minigames.plugin.pdata.getMinigamePlayer((Player)sender);
				if(player == null || player.isInMinigame())
					continue;
				
				Minigames.plugin.pdata.joinMinigame(player, minigame, false, 0.0);
			}
		}
	}

	@Override
	public void save( ConfigurationSection section )
	{
		section.set("minigame", mMinigame);
		mTarget.save(section.createSection("target"));
	}

	@Override
	public void load( ConfigurationSection section ) throws InvalidConfigurationException
	{
		mMinigame = section.getString("minigame");
		mTarget = (TargetCS)Target.loadTarget(section.getConfigurationSection("target"));
	}

	@Override
	public String[] describe()
	{
		return new String[] {
			ChatColor.GRAY + "Minigame: " + ChatColor.YELLOW + mMinigame,
			ChatColor.GRAY + "Target: " + ChatColor.YELLOW + mTarget.describe()
		};
	}
	
	@Override
	public String toString()
	{
		return "Make " + mTarget.describe() + " join the minigame " + mMinigame;
	}
	
	public static Action newAction(CommandSender sender, String[] args) throws IllegalStateException, BadArgumentException
	{
		if(args.length != 2)
			throw new IllegalStateException("<minigame> <target>");
		
		Minigame minigame = Minigames.plugin.mdata.getMinigame(args[0]);
		if(minigame == null)
			throw new BadArgumentException(0, "Unknown minigame");
		
		try
		{
			TargetCS target = TargetCS.parseTargets(args[1], false);
		
			JoinMinigameAction action = new JoinMinigameAction();
			action.mMinigame = minigame.getName();
			action.mTarget = target;
			
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
