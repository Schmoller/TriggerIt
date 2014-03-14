package au.com.addstar.triggerit;

import org.bukkit.Location;
import org.bukkit.World;

import au.com.addstar.triggerit.commands.BadArgumentException;

public class RelativeLocation extends Location
{
	private boolean mRelX;
	private boolean mRelY;
	private boolean mRelZ;
	
	private boolean mRelYaw;
	private boolean mRelPitch;
	
	private Location mRelSource;
	
	public RelativeLocation( World world, double x, double y, double z )
	{
		super(world, x, y, z);
	}

	public RelativeLocation( World world, double x, double y, double z, float yaw, float pitch )
	{
		super(world, x, y, z, yaw, pitch);
	}
	
	public RelativeLocation( World world, double x, boolean relX, double y, boolean relY, double z, boolean relZ, float yaw, boolean relYaw, float pitch, boolean relPitch )
	{
		super(world, x, y, z, yaw, pitch);
		mRelX = relX;
		mRelY = relY;
		mRelZ = relZ;
		mRelYaw = relYaw;
		mRelPitch = relPitch;
	}
	
	public RelativeLocation( Location location )
	{
		super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}
	
	public void setSource(Location location)
	{
		mRelSource = location;
	}
	
	public Location getSource()
	{
		return mRelSource;
	}
	
	@Override
	public double getX()
	{
		if(mRelX && mRelSource != null)
			return super.getX() + mRelSource.getX();
		return super.getX();
	}
	
	@Override
	public double getY()
	{
		if(mRelY && mRelSource != null)
			return super.getY() + mRelSource.getY();
		return super.getY();
	}
	
	@Override
	public double getZ()
	{
		if(mRelZ && mRelSource != null)
			return super.getZ() + mRelSource.getZ();
		return super.getZ();
	}
	
	@Override
	public float getYaw()
	{
		if(mRelYaw && mRelSource != null)
			return super.getYaw() + mRelSource.getYaw();
		return super.getYaw();
	}
	
	@Override
	public float getPitch()
	{
		if(mRelPitch && mRelSource != null)
			return super.getPitch() + mRelSource.getPitch();
		return super.getPitch();
	}
	
	@Override
	public int getBlockX()
	{
		return locToBlock(getX());
	}
	
	@Override
	public int getBlockY()
	{
		return locToBlock(getY());
	}
	
	@Override
	public int getBlockZ()
	{
		return locToBlock(getZ());
	}
	
	public boolean isRelX()
	{
		return mRelX;
	}
	
	public boolean isRelY()
	{
		return mRelY;
	}
	
	public boolean isRelZ()
	{
		return mRelZ;
	}
	
	public boolean isRelYaw()
	{
		return mRelYaw;
	}
	
	public boolean isRelPitch()
	{
		return mRelPitch;
	}
	
	public void setRelX(boolean relative)
	{
		mRelX = relative;
	}
	
	public void setRelY(boolean relative)
	{
		mRelY = relative;
	}
	
	public void setRelZ(boolean relative)
	{
		mRelZ = relative;
	}
	
	public void setRelYaw(boolean relative)
	{
		mRelYaw = relative;
	}
	
	public void setRelPitch(boolean relative)
	{
		mRelPitch = relative;
	}
	
	
	@Override
	public RelativeLocation clone()
	{
		return new RelativeLocation(getWorld(), super.getX(), mRelX, super.getY(), mRelY, super.getZ(), mRelZ, super.getYaw(), mRelYaw, super.getPitch(), mRelPitch);
	}
	
	@Override
	public boolean equals( Object obj )
	{
		if(!(obj instanceof RelativeLocation))
			return false;
		
		RelativeLocation loc = (RelativeLocation)obj;
		
		return super.equals(obj) && mRelX == loc.mRelX && mRelY == loc.mRelY && mRelZ == loc.mRelZ && mRelPitch == loc.mRelPitch && mRelYaw == loc.mRelYaw;
	}
	
	@Override
	public String toString()
	{
		return String.format("RelativeLocation{world=%s,x=%s,y=%s,z=%s,pitch=%s,yaw=%s}", getWorld(), (mRelX ? "~" : "") + super.getX(), (mRelY ? "~" : "") + super.getY(), (mRelZ ? "~" : "") + super.getZ(), (mRelPitch ? "~" : "") + super.getPitch(), (mRelYaw ? "~" : "") + super.getYaw());
	}
	
	public static RelativeLocation parseLocation(String[] args, int offset) throws BadArgumentException
	{
		RelativeLocation loc = new RelativeLocation(null, 0, 0, 0);
		loc.setRelYaw(true);
		loc.setRelPitch(true);

		try
		{
			if(args[offset].startsWith("~"))
			{
				if(args[offset].length() != 1)
					loc.setX(Double.parseDouble(args[offset].substring(1)));
				loc.setRelX(true);
			}
			else
				loc.setX(Double.parseDouble(args[offset]));
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(offset, "X value expected");
		}
		++offset;
		try
		{
			if(args[offset].startsWith("~"))
			{
				if(args[offset].length() != 1)
					loc.setY(Double.parseDouble(args[offset].substring(1)));
				loc.setRelY(true);
			}
			else
				loc.setY(Double.parseDouble(args[offset]));
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(offset, "Y value expected");
		}
		++offset;
		try
		{
			if(args[offset].startsWith("~"))
			{
				if(args[offset].length() != 1)
					loc.setZ(Double.parseDouble(args[offset].substring(1)));
				loc.setRelZ(true);
			}
			else
				loc.setZ(Double.parseDouble(args[offset]));
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(offset, "Z value expected");
		}
		
		return loc;
	}
	
	public static RelativeLocation parseLocationFull(String[] args, int offset) throws BadArgumentException
	{
		RelativeLocation loc = new RelativeLocation(null, 0, 0, 0, 0, 0);

		try
		{
			if(args[offset].startsWith("~"))
			{
				if(args[offset].length() != 1)
					loc.setX(Double.parseDouble(args[offset].substring(1)));
				loc.setRelX(true);
			}
			else
				loc.setX(Double.parseDouble(args[offset]));
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(offset, "X value expected");
		}
		++offset;
		try
		{
			if(args[offset].startsWith("~"))
			{
				if(args[offset].length() != 1)
					loc.setY(Double.parseDouble(args[offset].substring(1)));
				loc.setRelY(true);
			}
			else
				loc.setY(Double.parseDouble(args[offset]));
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(offset, "Y value expected");
		}
		++offset;
		try
		{
			if(args[offset].startsWith("~"))
			{
				if(args[offset].length() != 1)
					loc.setZ(Double.parseDouble(args[offset].substring(1)));
				loc.setRelZ(true);
			}
			else
				loc.setZ(Double.parseDouble(args[offset]));
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(offset, "Z value expected");
		}
		++offset;
		try
		{
			if(args[offset].startsWith("~"))
			{
				if(args[offset].length() != 1)
					loc.setYaw(Float.parseFloat(args[offset].substring(1)));
				loc.setRelYaw(true);
			}
			else
				loc.setYaw(Float.parseFloat(args[offset]));
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(offset, "Yaw value expected");
		}
		++offset;
		try
		{
			if(args[offset].startsWith("~"))
			{
				if(args[offset].length() != 1)
					loc.setPitch(Float.parseFloat(args[offset].substring(1)));
				loc.setRelPitch(true);
			}
			else
				loc.setPitch(Float.parseFloat(args[offset]));
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(offset, "Pitch value expected");
		}
		
		return loc;
	}
	
	@Override
	public World getWorld()
	{
		if(super.getWorld() == null && mRelSource != null)
			return mRelSource.getWorld();
		
		return super.getWorld();
	}
}
