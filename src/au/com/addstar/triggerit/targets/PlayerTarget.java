package au.com.addstar.triggerit.targets;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class PlayerTarget extends TargetCS
{
	private OfflinePlayer mTarget;
	
	public PlayerTarget(OfflinePlayer player)
	{
		mTarget = player;
	}
	
	@Override
	public List<? extends CommandSender> getTargets()
	{
		if(mTarget.isOnline())
			return Arrays.asList(mTarget.getPlayer());
		else
			return Collections.emptyList();
	}

}