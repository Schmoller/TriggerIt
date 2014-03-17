package au.com.addstar.triggerit;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BasicTextifier implements ITextifier
{
	public static final BasicTextifier instance = new BasicTextifier();
	
	@SuppressWarnings( "rawtypes" )
	@Override
	public String asString( Object obj )
	{
		if(obj instanceof Player)
			return ((Player)obj).getName();
		else if(obj instanceof Block)
			return String.format("%d %d %d", ((Block) obj).getX(), ((Block) obj).getY(), ((Block) obj).getZ());
		else if(obj instanceof Location)
			return String.format("%.1f %.1f %.1f", ((Location) obj).getX(), ((Location) obj).getY(), ((Location) obj).getZ());
		else if(obj instanceof World)
			return ((World) obj).getName();
		else if(obj instanceof Collection)
		{
			StringBuilder out = new StringBuilder();
			
			boolean first = true;
			for(Object o2 : (Collection)obj)
			{
				if(!first)
					out.append(",");
				first = false;
				out.append(asString(o2));
			}
			
			return out.toString();
		}
		return obj.toString();
	}

}
