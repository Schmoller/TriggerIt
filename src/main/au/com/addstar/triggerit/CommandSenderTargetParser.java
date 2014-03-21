package au.com.addstar.triggerit;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.triggerit.targets.AllTarget;
import au.com.addstar.triggerit.targets.CSParametricTarget;
import au.com.addstar.triggerit.targets.ConsoleTarget;
import au.com.addstar.triggerit.targets.NullTarget;
import au.com.addstar.triggerit.targets.PermissionTarget;
import au.com.addstar.triggerit.targets.PlayerTarget;
import au.com.addstar.triggerit.targets.Target;

public class CommandSenderTargetParser implements ITargetParser<CommandSender>
{
	@Override
	public Class<CommandSender> getBaseType()
	{
		return CommandSender.class;
	}
	
	@Override
	public Target<? extends CommandSender> parseSingleTarget( String targetString, Set<? extends Class<? extends CommandSender>> specifics ) throws IllegalArgumentException
	{
		if(targetString.equals("~"))
		{
			if(Target.isTypeAllowed(ConsoleCommandSender.class, specifics))
				return new ConsoleTarget(specifics);
		}
		if(targetString.equals("-"))
		{
			if(Target.isTypeAllowed(ConsoleCommandSender.class, specifics))
				return new NullTarget(specifics);
		}
		if(targetString.startsWith("@"))
		{
			return new CSParametricTarget(targetString.substring(1), specifics);
		}
		if(Target.isTypeAllowed(Player.class, specifics))
		{
			OfflinePlayer player = Bukkit.getOfflinePlayer(targetString);
			if(!player.hasPlayedBefore())
				throw new IllegalArgumentException("Unknown player " + targetString);
			
			return new PlayerTarget(player, specifics);
		}
		
		return null;
	}
	
	@Override
	public Target<? extends CommandSender> parseTargets( String targetString, Set<? extends Class<? extends CommandSender>> specifics) throws IllegalArgumentException
	{
		if(targetString.equals("~"))
		{
			if(Target.isTypeAllowed(ConsoleCommandSender.class, specifics))
				return new ConsoleTarget(specifics);
		}
		if(targetString.equals("-"))
		{
			if(Target.isTypeAllowed(ConsoleCommandSender.class, specifics))
				return new NullTarget(specifics);
		}
		if(targetString.startsWith("#"))
		{
			if(Target.isTypeAllowed(CommandSender.class, specifics))
				return new PermissionTarget(targetString.substring(1), false, specifics);
		}
		if(targetString.startsWith("!#"))
		{
			if(Target.isTypeAllowed(CommandSender.class, specifics))
				return new PermissionTarget(targetString.substring(2), true, specifics);
		}
		if(targetString.startsWith("@"))
		{
			return new CSParametricTarget(targetString.substring(1), specifics);
		}
		if(targetString.equals("*"))
			return new AllTarget(specifics);
		
		
		if(Target.isTypeAllowed(Player.class, specifics))
		{
			OfflinePlayer player = Bukkit.getOfflinePlayer(targetString);
			if(!player.hasPlayedBefore())
				throw new IllegalArgumentException("Unknown player " + targetString);
			
			return new PlayerTarget(player, specifics);
		}
		
		return null;
	}

}
