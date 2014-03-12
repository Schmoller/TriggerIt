package au.com.addstar.triggerit.targets;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public abstract class TargetCS extends Target<CommandSender>
{
	public static TargetCS parseTargets(String targetString, boolean allowConsole) throws IllegalArgumentException
	{
		if(targetString.startsWith("~"))
		{
			if(!allowConsole)
				throw new IllegalArgumentException("Console targets are not allowed here.");
			
			return new ConsoleTarget();
		}
		else if(targetString.startsWith("#"))
			return new PermissionTarget(targetString.substring(1), false);
		else if(targetString.startsWith("!#"))
			return new PermissionTarget(targetString.substring(2), true);
		else if(targetString.startsWith("@"))
			return new CSParametricTarget(targetString.substring(1));
		else
		{
			OfflinePlayer player = Bukkit.getOfflinePlayer(targetString);
			if(!player.hasPlayedBefore())
				throw new IllegalArgumentException("Unknown player " + targetString);
			
			return new PlayerTarget(player);
		}
	}
}
