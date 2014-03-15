package au.com.addstar.triggerit.actions;

import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import au.com.addstar.triggerit.Action;
import au.com.addstar.triggerit.RelativeLocation;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.targets.LocationTarget;
import au.com.addstar.triggerit.targets.Target;
import au.com.addstar.triggerit.targets.TargetCS;

public class TeleportAction implements Action
{
	private TargetCS mTarget;
	private Target<? extends Object> mDestination;
	
	@Override
	public void execute( Map<String, Object> arguments )
	{
		mTarget.setArgumentMap(arguments);
		mDestination.setArgumentMap(arguments);
		
		List<? extends Object> destinations = mDestination.getTargets();
		Object destination = (destinations.isEmpty() ? null : destinations.get(0));
		
		if(destination == null)
			return;
		
		for(CommandSender target : mTarget.getTargets())
		{
			Player player = (Player)target;
			if(destination instanceof Player)
				player.teleport((Player)destination, TeleportCause.COMMAND);
			else if(destination instanceof RelativeLocation)
			{
				((RelativeLocation) destination).setSource(player.getLocation());
				player.teleport((RelativeLocation)destination, TeleportCause.COMMAND);
			}
			else if(destination instanceof Location)
				player.teleport((Location)destination, TeleportCause.COMMAND);
		}
	}

	@Override
	public String resolveArgument( Object argument )
	{
		return String.valueOf(argument);
	}

	@Override
	public void load( ConfigurationSection section ) throws InvalidConfigurationException
	{
		mTarget = (TargetCS)Target.loadTarget(section.getConfigurationSection("target"));
		mDestination = Target.loadTarget(section.getConfigurationSection("dest"));
	}
	
	@Override
	public void save( ConfigurationSection section )
	{
		ConfigurationSection target = section.createSection("target");
		mTarget.save(target);
		ConfigurationSection destination = section.createSection("dest");
		mDestination.save(destination);
	}
	
	public static TeleportAction newAction(CommandSender sender, String[] args) throws BadArgumentException, IllegalStateException
	{
		if(args.length == 0)
			throw new IllegalStateException("<target> <destination>");
		
		TargetCS target;
		
		try
		{
			target = TargetCS.parseTargets(args[0], false);
		}
		catch(IllegalArgumentException e)
		{
			BadArgumentException ex = new BadArgumentException(0, e.getMessage());
			ex.addInfo(ChatColor.GOLD + "Possible targets: ");
			ex.addInfo(ChatColor.GRAY + "<player> - A specific player");
			ex.addInfo(ChatColor.GRAY + "* - Everyone");
			ex.addInfo(ChatColor.GRAY + "#<perm> - Players with <perm>");
			ex.addInfo(ChatColor.GRAY + "!#<perm> - Players without <perm>");
			ex.addInfo(ChatColor.GRAY + "@<argument> - Value of <argument>");
			
			throw ex;
		}
		
		Target<? extends Object> destination = null;
		
		if(args.length == 2)
		{
			try
			{
				destination = Target.parseSingleTarget(args[1], false);
			}
			catch(IllegalArgumentException e)
			{
				BadArgumentException ex = new BadArgumentException(1, e.getMessage());
				ex.addInfo(ChatColor.GOLD + "Possible destinations: ");
				ex.addInfo(ChatColor.GRAY + "<player> - teleport target to <player>");
				ex.addInfo(ChatColor.GRAY + "@<argument> - teleport target to value of <argument>");
				ex.addInfo(ChatColor.GRAY + "<x> <y> <z> - teleport target to location. Can use relative coords");
				ex.addInfo(ChatColor.GRAY + "<x> <y> <z> <yaw> <pitch> - teleport target to location. Can use relative coords");
				throw ex;
			}
		}
		else if(args.length == 4)
		{
			RelativeLocation loc = RelativeLocation.parseLocation(args, 1);
			destination = new LocationTarget(loc);
		}
		else if(args.length == 6)
		{
			RelativeLocation loc = RelativeLocation.parseLocationFull(args, 1);
			destination = new LocationTarget(loc);
		}
		else
			throw new IllegalStateException("<target> <destination>");
		
		TeleportAction action = new TeleportAction();
		action.mTarget = target;
		action.mDestination = destination;
		
		return action;
	}
	
	public static List<String> tabComplete(CommandSender sender, String[] args)
	{
		return null;
	}
}
