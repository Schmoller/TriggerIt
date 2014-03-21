package au.com.addstar.triggerit.targets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AllTarget extends Target<CommandSender>
{
	public AllTarget(Set<? extends Class<? extends CommandSender>> specifics)
	{
		super(specifics);
	}
	
	@Override
	public List<? extends CommandSender> getTargets()
	{
		boolean allowConsole = isValueAllowed(Bukkit.getConsoleSender());
		boolean allowPlayers = isTypeAllowed(Player.class);
		
		Player[] players = Bukkit.getOnlinePlayers();
		ArrayList<CommandSender> senders = new ArrayList<CommandSender>((allowPlayers ? players.length : 0) + (allowConsole ? 1 : 0));
		
		if(allowPlayers)
		{
			for(Player player : players)
				senders.add(player);
		}
		
		if(allowConsole)
			senders.add(Bukkit.getConsoleSender());
		
		return senders;
	}

	@Override
	public String describe()
	{
		return "Everybody";
	}
	
	@Override
	public String toString()
	{
		return "*";
	}
}
