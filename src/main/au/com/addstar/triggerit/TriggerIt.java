package au.com.addstar.triggerit;

import java.util.ArrayList;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public abstract class TriggerIt
{
	private static TriggerItPlugin mPlugin;
	
	public static void initialize(TriggerItPlugin plugin)
	{
		mPlugin = plugin;
	}
	
	private static ArrayList<ArgumentHandler> mArgHandlers = new ArrayList<ArgumentHandler>();
	
	public static void registerArgumentProvider(ArgumentHandler handler)
	{
		mArgHandlers.add(handler);
	}
	
	public static Map<String, Object> getArgumentsFor(Object obj)
	{
		ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
		for(ArgumentHandler handler : mArgHandlers)
			handler.buildArguments(obj, builder);
		
		return builder.build();
	}
	
	public static String getArgumentString(Object obj)
	{
		for(ArgumentHandler handler : mArgHandlers)
		{
			String value = handler.asString(obj);
			if(value != null)
				return value;
		}
		
		return (obj == null ? "null" : obj.toString());
	}
}
