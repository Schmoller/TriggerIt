package au.com.addstar.triggerit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

import au.com.addstar.triggerit.commands.BadArgumentException;

import com.google.common.base.Throwables;

public class ActionManager
{
	private HashMap<String, ActionDefinition> mDefinitions = new HashMap<String, ActionDefinition>();
	private HashMap<Class<? extends Action>, String> mReverseDefinitions = new HashMap<Class<? extends Action>, String>();
	private ArrayList<String> mTypeNames = new ArrayList<String>();
	
	public void registerActionType(String type, Class<? extends Action> typeClass) throws IllegalArgumentException
	{
		Validate.notNull(typeClass);
		Validate.notNull(type);
		
		if(mDefinitions.containsKey(type.toLowerCase()))
			throw new IllegalArgumentException("Duplicate type name found");
		
		mDefinitions.put(type.toLowerCase(), new ActionDefinition(typeClass));
		mReverseDefinitions.put(typeClass, type.toLowerCase());
		mTypeNames.add(type);
	}
	
	public List<String> getTypeNames()
	{
		return Collections.unmodifiableList(mTypeNames);
	}
	
	public ActionDefinition getType(String type)
	{
		return mDefinitions.get(type.toLowerCase());
	}
	
	public String getTypeFrom(Action action)
	{
		String type = mReverseDefinitions.get(action.getClass());
		if(type == null)
			throw new IllegalArgumentException("Action class " + action.getClass().getName() + " has not been registered!");
		return type;
	}
	
	public static class ActionDefinition
	{
		private Constructor<? extends Action> mBlankConstructor;
		private Method mNewActionMethod;
		private Method mTabCompleteMethod;
		
		public ActionDefinition(Class<? extends Action> actionClass) throws IllegalArgumentException
		{
			try
			{
				mBlankConstructor = actionClass.getConstructor();
				mNewActionMethod = actionClass.getMethod("newAction", CommandSender.class, String[].class);
				mTabCompleteMethod = actionClass.getMethod("tabComplete", CommandSender.class, String[].class);
			}
			catch(NoSuchMethodException e)
			{
				throw new IllegalArgumentException("Missing a required method", e);
			}
		}
		
		
		public Action newAction(CommandSender sender, String[] args) throws IllegalArgumentException, IllegalStateException, BadArgumentException
		{
			try
			{
				return (Action)mNewActionMethod.invoke(null, sender, args);
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
		
		public Action newBlankAction()
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
	}
}
