package au.com.addstar.triggerit.actions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.google.common.collect.ImmutableSet;

import au.com.addstar.triggerit.Action;
import au.com.addstar.triggerit.RelativeLocation;
import au.com.addstar.triggerit.TriggerIt;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.targets.LocationTarget;
import au.com.addstar.triggerit.targets.Target;

public class TeleportAction implements Action
{
	private Target<? extends CommandSender> mTarget;
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
	public void load( ConfigurationSection section ) throws InvalidConfigurationException
	{
		mTarget = TriggerIt.parseTargets(section.getString("target"), CommandSender.class, ImmutableSet.of(Player.class));
		mDestination = TriggerIt.parseSingleTarget(section.getString("dest"), Object.class, ImmutableSet.of(Player.class, Location.class));
	}
	
	@Override
	public void save( ConfigurationSection section )
	{
		section.set("target", mTarget.toString());
		section.set("dest", mDestination.toString());
	}
	
	@Override
	public String[] describe()
	{
		return new String[] {
			ChatColor.GOLD + "Teleport action:",
			ChatColor.GRAY + " Target: " + ChatColor.YELLOW + mTarget.describe(),
			ChatColor.GRAY + " Destination: " + ChatColor.YELLOW + mDestination.describe()
		};
	}
	
	@Override
	public String toString()
	{
		return String.format("Teleport %s to %s", mTarget.describe(), mDestination.describe());
	}
	
	public static TeleportAction newAction(CommandSender sender, String[] args) throws BadArgumentException, IllegalStateException
	{
		if(args.length == 0)
			throw new IllegalStateException("<target> <destination>");
		
		Target<? extends CommandSender> target;
		
		try
		{
			target = TriggerIt.parseTargets(args[0], CommandSender.class, ImmutableSet.of(Player.class));
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
				destination = TriggerIt.parseSingleTarget(args[1], Object.class, ImmutableSet.of(Player.class, Location.class));
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
			Set<? extends Class<? extends RelativeLocation>> specifics = Collections.emptySet();
			destination = new LocationTarget(loc, specifics);
		}
		else if(args.length == 6)
		{
			Set<? extends Class<? extends RelativeLocation>> specifics = Collections.emptySet();
			RelativeLocation loc = RelativeLocation.parseLocationFull(args, 1);
			destination = new LocationTarget(loc, specifics);
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
