package au.com.addstar.triggerit.targets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class PermissionTarget extends Target<CommandSender>
{
	private String mPermission;
	private boolean mInvert;
	
	public PermissionTarget(String perm, boolean invert, Set<? extends Class<? extends CommandSender>> specifics)
	{
		super(specifics);
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
				if(sub instanceof CommandSender && isValueAllowed((CommandSender)sub))
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
				if(!player.hasPermission(mPermission) && isValueAllowed(player))
					senders.add(player);
			}
			
			return senders;
		}
	}

	@Override
	public String describe()
	{
		if(mInvert)
			return "Players without permisison '" + mPermission + "'";
		else
			return "Players with permisison '" + mPermission + "'";
	}
	
	@Override
	public String toString()
	{
		if(mInvert)
			return "!#" + mPermission;
		else
			return "#" + mPermission;
	}
}
