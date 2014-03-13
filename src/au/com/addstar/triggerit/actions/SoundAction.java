package au.com.addstar.triggerit.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.triggerit.Action;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.targets.ParametricTarget;
import au.com.addstar.triggerit.targets.Target;

public class SoundAction implements Action
{
	private static ArrayList<String> mSoundNames = new ArrayList<String>();
	
	static
	{
		for(Sound sound : Sound.values())
			mSoundNames.add(sound.name());
	}
	
	private Sound mSound;
	private Target<? extends Object> mTarget;
	
	@Override
	public void execute( Map<String, Object> arguments )
	{
		if(mTarget instanceof ParametricTarget)
			((ParametricTarget)mTarget).setArgumentMap(arguments);
		
		List<? extends Object> targets = mTarget.getTargets();
		for(Object obj : targets)
		{
			if(obj instanceof Player)
				((Player)obj).playSound(((Player)obj).getLocation(), mSound, 1.0f, 1.0f);
			else if(obj instanceof Location)
			{
				Location loc = (Location)obj;
				loc.getWorld().playSound(loc, mSound, 1.0f, 1.0f);
			}
			else if(obj instanceof Block)
			{
				Block block = (Block)obj;
				block.getWorld().playSound(block.getLocation(), mSound, 1.0f, 1.0f);
			}
		}
	}

	@Override
	public String resolveArgument( Object argument )
	{
		if(argument instanceof Player)
			return ((Player)argument).getName();
		else if(argument instanceof Block)
			return String.format("%d %d %d", ((Block) argument).getX(), ((Block) argument).getY(), ((Block) argument).getZ());
		else if(argument instanceof Location)
			return String.format("%d %d %d", ((Location) argument).getX(), ((Location) argument).getY(), ((Location) argument).getZ());
		
		return argument.toString();
	}
	
	public static SoundAction newAction(CommandSender sender, String[] args) throws IllegalArgumentException, IllegalStateException, BadArgumentException
	{
		if(args.length != 2)
			throw new IllegalStateException("<target> <sound>");
		
		SoundAction action = new SoundAction();
		try
		{
			action.mTarget = Target.parseTargets(args[0], false);
		}
		catch(IllegalArgumentException e)
		{
			throw new BadArgumentException(0, e.getMessage());
		}
		
		for(Sound sound : Sound.values())
		{
			if(sound.name().equalsIgnoreCase(args[1]))
			{
				action.mSound = sound;
				break;
			}
		}
		if(action.mSound == null)
			throw new BadArgumentException(1, "Unknown sound " + args[1]);
		
		return action;
	}
	
	public static List<String> tabComplete(CommandSender sender, String[] args)
	{
		if(args.length == 2)
			return Utilities.matchString(args[1], mSoundNames);
		
		return null;
	}

}