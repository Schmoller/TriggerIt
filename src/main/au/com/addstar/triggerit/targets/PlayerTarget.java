package au.com.addstar.triggerit.targets;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class PlayerTarget extends Target<CommandSender>
{
	private OfflinePlayer mTarget;
	
	public PlayerTarget(OfflinePlayer player, Set<? extends Class<? extends CommandSender>> specifics)
	{
		super(specifics);
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
	
	@Override
	public String describe()
	{
		return "Player: " + mTarget.getName();
	}

	@Override
	public String toString()
	{
		return mTarget.getName();
	}
}
