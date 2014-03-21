package au.com.addstar.triggerit;

import java.util.Set;

import org.bukkit.Location;

import au.com.addstar.triggerit.targets.LocationTarget;
import au.com.addstar.triggerit.targets.ParametricTarget;
import au.com.addstar.triggerit.targets.Target;

public class BasicTargetParser implements ITargetParser<Object>
{
	@Override
	public Class<Object> getBaseType()
	{
		return Object.class;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public Target<? extends Object> parseTargets( String targetString, Set<? extends Class<? extends Object>> specifics ) throws IllegalArgumentException
	{
		if(targetString.startsWith("@"))
			return new ParametricTarget(targetString.substring(1), (Set<Class<? extends Object>>) specifics);
		
		if(Target.isTypeAllowed(Location.class, specifics))
		{
			String[] parts = targetString.split(" ");
			
			if(parts.length == 3 || parts.length == 4)
			{
				RelativeLocation loc = RelativeLocation.parseLocation(parts, 0);
				return new LocationTarget(loc, specifics);
			}
			else if(parts.length == 5 || parts.length == 6)
			{
				RelativeLocation loc = RelativeLocation.parseLocationFull(parts, 0);
				return new LocationTarget(loc, specifics);
			}
		}
		return null;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public Target<? extends Object> parseSingleTarget( String targetString, Set<? extends Class<? extends Object>> specifics ) throws IllegalArgumentException
	{
		if(targetString.startsWith("@"))
			return new ParametricTarget(targetString.substring(1), (Set<Class<? extends Object>>) specifics);
		
		if(Target.isTypeAllowed(Location.class, specifics))
		{
			String[] parts = targetString.split(" ");
			
			if(parts.length == 3 || parts.length == 4)
			{
				RelativeLocation loc = RelativeLocation.parseLocation(parts, 0);
				return new LocationTarget(loc, specifics);
			}
			else if(parts.length == 5 || parts.length == 6)
			{
				RelativeLocation loc = RelativeLocation.parseLocationFull(parts, 0);
				return new LocationTarget(loc, specifics);
			}
		}
		return null;
	}

}
