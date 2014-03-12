package au.com.addstar.triggerit.targets;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AllTarget extends TargetCS
{
	private boolean mConsole;
	
	public AllTarget(boolean console)
	{
		mConsole = console;
	}
	
	@Override
	public List<? extends CommandSender> getTargets()
	{
		Player[] players = Bukkit.getOnlinePlayers();
		ArrayList<CommandSender> senders = new ArrayList<CommandSender>(players.length + (mConsole ? 1 : 0));
		for(Player player : players)
			senders.add(player);
		
		if(mConsole)
			senders.add(Bukkit.getConsoleSender());
		
		return senders;
	}

}
