package au.com.addstar.triggerit;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BasicTextifier implements ITextifier
{
	public static final BasicTextifier instance = new BasicTextifier();
	
	@Override
	public String asString( Object obj )
	{
		if(obj instanceof Player)
			return ((Player)obj).getName();
		else if(obj instanceof Block)
			return String.format("%d %d %d", ((Block) obj).getX(), ((Block) obj).getY(), ((Block) obj).getZ());
		else if(obj instanceof Location)
			return String.format("%d %d %d", ((Location) obj).getX(), ((Location) obj).getY(), ((Location) obj).getZ());
		else if(obj instanceof World)
			return ((World) obj).getName();
		
		return obj.toString();
	}

}
