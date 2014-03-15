package au.com.addstar.triggerit.targets;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

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
	
	protected LocationTarget() {}
	
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
	public void save( ConfigurationSection section )
	{
		super.save(section);
		
		if(mWorld != null)
			section.set("world", mWorld.toString());
		section.set("location", mLocation);
	}
	
	@Override
	protected void load( ConfigurationSection section ) throws InvalidConfigurationException
	{
		if(section.isString("world"))
			mWorld = UUID.fromString(section.getString("world"));
		else
			mWorld = null;
		mLocation = (RelativeLocation)section.get("location");
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
}
