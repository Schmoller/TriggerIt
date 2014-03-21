package au.com.addstar.triggerit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import au.com.addstar.triggerit.targets.Target;

import com.google.common.collect.HashMultimap;
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

	private static HashMultimap<Class<?>, ITargetParser<?>> mTargetParsers = HashMultimap.create();
	private static HashMultimap<Class<?>, Class<?>> mTargetLinks = HashMultimap.create();
	private static HashMap<Class<?>, List<Class<?>>> mTargetParseOrder = null;
	
	public static <T> void registerTargetParser(ITargetParser<T> parser)
	{
		mTargetParsers.put(parser.getBaseType(), parser);
		
		for(Class<?> clazz : mTargetParsers.keySet())
		{
			if(clazz.isAssignableFrom(parser.getBaseType()))
				mTargetLinks.put(clazz, parser.getBaseType());
		}
		
		if(mTargetParseOrder != null)
			mTargetParseOrder = null;
	}
	
	private static void rebuildTargetParseOrder()
	{
		mTargetParseOrder = new HashMap<Class<?>, List<Class<?>>>();
		
		for(Class<?> clazz : mTargetLinks.keySet())
		{
			ArrayList<Class<?>> order = new ArrayList<Class<?>>(mTargetLinks.get(clazz));
			
			Collections.sort(order, new Comparator<Class<?>>()
			{
				@Override
				public int compare( Class<?> o1, Class<?> o2 )
				{
					if(o1.isAssignableFrom(o2))
						return -1;
					if(o2.isAssignableFrom(o1))
						return 1;
					
					return 0;
				}
			});
			
			mTargetParseOrder.put(clazz, order);
			System.out.println("Parse order for " + clazz.getName() + ": " + order.toString());
		}
	}
	
	public static <T> Target<? extends T> parseTargets(String targetString, Class<T> targetBase) throws IllegalArgumentException
	{
		Set<Class<? extends T>> empty = Collections.emptySet();
		return parseTargets(targetString, targetBase, empty);
	}
	
	/**
	 * Parse targets from the targetString that are an instance of one of the classes specified in the specifics set
	 * @param targetString The string to parse from
	 * @param targetBase The class that the target returns
	 * @param specifics A set of classes that resolved targets (at runtime) must be an instance of to be returned
	 * @return The target object
	 * @throws IllegalArgumentException thrown if there was an error in the parse string
	 */
	@SuppressWarnings( "unchecked" )
	public static <T> Target<? extends T> parseTargets(String targetString, Class<T> targetBase, Set<? extends Class<? extends T>> specifics) throws IllegalArgumentException
	{
		if(mTargetParseOrder == null)
			rebuildTargetParseOrder();
		
		Validate.isTrue(mTargetParsers.containsKey(targetBase), "There is no registered parser for the type " + targetBase.getName());
		
		Set<ITargetParser<?>> parsers = mTargetParsers.get(targetBase);
		
		for(ITargetParser<?> parser : parsers)
		{
			Target<? extends T> target = ((ITargetParser<T>)parser).parseTargets(targetString, specifics);
			if(target != null)
				return target;
		}
		
		for(Class<?> clazz : mTargetParseOrder.get(targetBase))
		{
			parsers = mTargetParsers.get(clazz);
			
			for(ITargetParser<?> parser : parsers)
			{
				Target<? extends T> target = ((ITargetParser<T>)parser).parseTargets(targetString, specifics);
				if(target != null)
					return target;
			}
		}
		
		throw new IllegalArgumentException("Unable to parse target");
	}
	
	public static <T> Target<? extends T> parseSingleTarget(String targetString, Class<T> targetBase) throws IllegalArgumentException
	{
		Set<Class<? extends T>> empty = Collections.emptySet();
		return parseSingleTarget(targetString, targetBase, empty);
	}
	
	@SuppressWarnings( "unchecked" )
	public static <T> Target<? extends T> parseSingleTarget(String targetString, Class<T> targetBase, Set<? extends Class<? extends T>> specifics) throws IllegalArgumentException
	{
		if(mTargetParseOrder == null)
			rebuildTargetParseOrder();
		
		Validate.isTrue(mTargetParsers.containsKey(targetBase), "There is no registered parser for the type " + targetBase.getName());
		
		Set<ITargetParser<?>> parsers = mTargetParsers.get(targetBase);
		
		for(ITargetParser<?> parser : parsers)
		{
			Target<? extends T> target = ((ITargetParser<T>)parser).parseSingleTarget(targetString, specifics);
			if(target != null)
				return target;
		}
		
		for(Class<?> clazz : mTargetParseOrder.get(targetBase))
		{
			parsers = mTargetParsers.get(clazz);
			
			for(ITargetParser<?> parser : parsers)
			{
				Target<? extends T> target = ((ITargetParser<T>)parser).parseSingleTarget(targetString, specifics);
				if(target != null)
					return target;
			}
		}
		
		throw new IllegalArgumentException("Unable to parse target");
	}
}
