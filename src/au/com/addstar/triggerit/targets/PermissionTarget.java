package au.com.addstar.triggerit.targets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class PermissionTarget extends TargetCS
{
	private String mPermission;
	private boolean mInvert;
	
	public PermissionTarget(String perm, boolean invert)
	{
		mPermission = perm;
		mInvert = invert;
	}
	
	@Override
	public List<? extends CommandSender> getTargets()
	{
		if(!mInvert)
		{
			Set<Permissible> subs = Bukkit.getPluginManager().getPermissionSubscriptions(mPermission);
			ArrayList<CommandSender> senders = new ArrayList<CommandSender>(subs.size());
			
			for(Permissible sub : subs)
			{
				if(sub instanceof CommandSender)
					senders.add((CommandSender)sub);
			}
			
			return senders;
		}
		else
		{
			Player[] players = Bukkit.getOnlinePlayers();
			ArrayList<CommandSender> senders = new ArrayList<CommandSender>(players.length);
			for(Player player : players)
			{
				if(!player.hasPermission(mPermission))
					senders.add(player);
			}
			
			return senders;
		}
	}

}
