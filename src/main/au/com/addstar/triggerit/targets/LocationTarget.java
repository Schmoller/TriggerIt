package au.com.addstar.triggerit.targets;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import au.com.addstar.triggerit.RelativeLocation;

public class LocationTarget extends Target<Object>
{
	private RelativeLocation mLocation;
	
	public LocationTarget(RelativeLocation location, Set<? extends Class<? extends Object>> specifics)
	{
		super(specifics);
		
		mLocation = location;
	}
	
	@Override
	public void setArgumentMap( Map<String, Object> arguments )
	{
	}
	
	@Override
	public List<? extends RelativeLocation> getTargets()
	{
		return Arrays.asList(mLocation);
	}
	
	@Override
	public String describe()
	{
		mLocation.setSource(null);
		if(mLocation.getWorld() == null)
			return String.format("Location: %s %s %s %s %s", (mLocation.isRelX() ? "~" : "") + mLocation.getX(), (mLocation.isRelY() ? "~" : "") + mLocation.getY(), (mLocation.isRelZ() ? "~" : "") + mLocation.getZ(), (mLocation.isRelYaw() ? "~" : "") + mLocation.getYaw(), (mLocation.isRelPitch() ? "~" : "") + mLocation.getPitch());
		else
			return String.format("Location: %s %s %s %s %s %s", mLocation.getWorld().getName(), (mLocation.isRelX() ? "~" : "") + mLocation.getX(), (mLocation.isRelY() ? "~" : "") + mLocation.getY(), (mLocation.isRelZ() ? "~" : "") + mLocation.getZ(), (mLocation.isRelYaw() ? "~" : "") + mLocation.getYaw(), (mLocation.isRelPitch() ? "~" : "") + mLocation.getPitch());
	}
	
	@Override
	public String toString()
	{
		mLocation.setSource(null);
		if(mLocation.getWorld() == null)
			return String.format("%s %s %s %s %s", (mLocation.isRelX() ? "~" : "") + mLocation.getX(), (mLocation.isRelY() ? "~" : "") + mLocation.getY(), (mLocation.isRelZ() ? "~" : "") + mLocation.getZ(), (mLocation.isRelYaw() ? "~" : "") + mLocation.getYaw(), (mLocation.isRelPitch() ? "~" : "") + mLocation.getPitch());
		else
			return String.format("%s %s %s %s %s %s", (mLocation.isRelX() ? "~" : "") + mLocation.getX(), (mLocation.isRelY() ? "~" : "") + mLocation.getY(), (mLocation.isRelZ() ? "~" : "") + mLocation.getZ(), (mLocation.isRelYaw() ? "~" : "") + mLocation.getYaw(), (mLocation.isRelPitch() ? "~" : "") + mLocation.getPitch(), mLocation.getWorld().getName());
	}
}
