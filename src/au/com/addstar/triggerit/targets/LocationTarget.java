package au.com.addstar.triggerit.targets;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import au.com.addstar.triggerit.RelativeLocation;

public class LocationTarget extends Target<RelativeLocation>
{
	private RelativeLocation mLocation;
	private UUID mWorld;
	
	public LocationTarget(RelativeLocation location)
	{
		mLocation = location;
		if(location.getWorld() != null)
			mWorld = location.getWorld().getUID();
	}
	
	@Override
	public void setArgumentMap( Map<String, Object> arguments )
	{
		if(mWorld == null)
		{
			if(arguments.containsKey("world")) 
				mWorld = ((World)arguments.get("world")).getUID();
			else if(arguments.containsKey("location"))
				mWorld = ((Location)arguments.get("location")).getWorld().getUID();
		}
	}
	
	@Override
	public List<? extends RelativeLocation> getTargets()
	{
		if(mWorld != null)
			mLocation.setWorld(Bukkit.getWorld(mWorld));
		
		return Arrays.asList(mLocation);
	}
}
