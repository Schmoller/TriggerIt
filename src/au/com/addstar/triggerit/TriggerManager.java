package au.com.addstar.triggerit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;

import au.com.addstar.triggerit.commands.BadArgumentException;


public class TriggerManager
{
	private HashMap<String, TriggerDefinition> mDefinitions = new HashMap<String, TriggerDefinition>();
	private ArrayList<String> mTypeNames = new ArrayList<String>();
	
	public void registerTriggerType(String type, Class<? extends Trigger> typeClass) throws IllegalArgumentException
	{
		Validate.notNull(typeClass);
		Validate.notNull(type);
		
		if(mDefinitions.containsKey(type.toLowerCase()))
			throw new IllegalArgumentException("Duplicate type name found");
		
		mDefinitions.put(type.toLowerCase(), new TriggerDefinition(typeClass));
		mTypeNames.add(type);
	}
	
	public void initializeAll(TriggerItPlugin plugin)
	{
		for(TriggerDefinition def : mDefinitions.values())
			def.initialize(plugin);
	}
	
	public List<String> getTypeNames()
	{
		return Collections.unmodifiableList(mTypeNames);
	}
	
	public TriggerDefinition getType(String type)
	{
		return mDefinitions.get(type.toLowerCase());
	}
	
	private HashMultimap<UUID, Trigger> mAllTriggers = HashMultimap.create();

	public void addTrigger(Trigger trigger)
	{
		if(trigger.isValid())
		{
			// TODO: Way to get world for those that have it
			mAllTriggers.put(null, trigger);
			trigger.onLoad();
		}
	}
	
	/**
	 * Some triggers need extra information for example the BlockTrigger needs a block clicked to finish it.
	 * This method is to be called once all needed information is filled out
	 * @param trigger The trigger to complete
	 */
	public void completeTrigger(Trigger trigger)
	{
		Validate.isTrue(trigger.isValid(), "Trigger is not valid!");
		addTrigger(trigger);
	}
	
	public static class TriggerDefinition
	{
		private Constructor<? extends Trigger> mBlankConstructor;
		private Method mNewTriggerMethod;
		private Method mTabCompleteMethod;
		private Method mInitializeMethod;
		
		public TriggerDefinition(Class<? extends Trigger> triggerClass) throws IllegalArgumentException
		{
			try
			{
				mBlankConstructor = triggerClass.getConstructor();
				mNewTriggerMethod = triggerClass.getMethod("newTrigger", CommandSender.class, String[].class);
				mTabCompleteMethod = triggerClass.getMethod("tabComplete", CommandSender.class, String[].class);
				mInitializeMethod = triggerClass.getMethod("initializeType", TriggerItPlugin.class);
			}
			catch(NoSuchMethodException e)
			{
				throw new IllegalArgumentException("Missing a required method", e);
			}
		}
		
		
		public Trigger newTrigger(CommandSender sender, String[] args) throws IllegalArgumentException, IllegalStateException, BadArgumentException
		{
			try
			{
				return (Trigger)mNewTriggerMethod.invoke(null, sender, args);
			}
			catch ( IllegalAccessException e )
			{
				throw new RuntimeException(e);
			}
			catch ( InvocationTargetException e )
			{
				Throwables.propagateIfPossible(e.getCause());
				throw new RuntimeException(e.getCause());
			}
		}
		
		public Trigger newBlankTrigger()
		{
			try
			{
				return mBlankConstructor.newInstance();
			}
			catch ( Exception e )
			{
				throw new RuntimeException(e);
			}
		}
		
		@SuppressWarnings( "unchecked" )
		public List<String> tabComplete(CommandSender sender, String[] args)
		{
			try
			{
				return (List<String>)mTabCompleteMethod.invoke(null, sender, args);
			}
			catch ( IllegalAccessException e )
			{
				throw new RuntimeException(e);
			}
			catch ( InvocationTargetException e )
			{
				Throwables.propagateIfPossible(e.getCause());
				throw new RuntimeException(e.getCause());
			}
		}
		
		public void initialize(TriggerItPlugin plugin)
		{
			try
			{
				mInitializeMethod.invoke(null, plugin);
			}
			catch ( IllegalAccessException e )
			{
				throw new RuntimeException(e);
			}
			catch ( InvocationTargetException e )
			{
				Throwables.propagateIfPossible(e.getCause());
				throw new RuntimeException(e.getCause());
			}
		}
	}
}
