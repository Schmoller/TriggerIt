package au.com.addstar.triggerit.targets;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

public class PlayerTarget extends TargetCS
{
	private OfflinePlayer mTarget;
	
	public PlayerTarget(OfflinePlayer player)
	{
		mTarget = player;
	}
	
	protected PlayerTarget() {}
	
	@Override
	public List<? extends CommandSender> getTargets()
	{
		if(mTarget.isOnline())
			return Arrays.asList(mTarget.getPlayer());
		else
			return Collections.emptyList();
	}
	
	@Override
	public void save( ConfigurationSection section )
	{
		super.save(section);
		section.set("player", mTarget.getName());
	}
	
	@Override
	protected void load( ConfigurationSection section ) throws InvalidConfigurationException
	{
		mTarget = Bukkit.getOfflinePlayer(section.getString("player"));
	}

}
