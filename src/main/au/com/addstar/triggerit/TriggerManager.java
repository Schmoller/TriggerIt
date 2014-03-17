package au.com.addstar.triggerit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;

import au.com.addstar.triggerit.commands.BadArgumentException;

public class TriggerManager implements Listener
{
	private HashMap<String, TriggerDefinition> mDefinitions = new HashMap<String, TriggerDefinition>();
	private HashMap<Class<? extends Trigger>, String> mReverseDefinitions = new HashMap<Class<? extends Trigger>, String>();
	private ArrayList<String> mTypeNames = new ArrayList<String>();
	
	private File mFolder;
	
	public void registerTriggerType(String type, Class<? extends Trigger> typeClass) throws IllegalArgumentException
	{
		Validate.notNull(typeClass);
		Validate.notNull(type);
		
		if(mDefinitions.containsKey(type.toLowerCase()))
			throw new IllegalArgumentException("Duplicate type name found");
		
		mDefinitions.put(type.toLowerCase(), new TriggerDefinition(typeClass));
		mReverseDefinitions.put(typeClass, type);
		mTypeNames.add(type);
	}
	
	public void initializeAll(TriggerItPlugin plugin)
	{
		mFolder = plugin.getDataFolder();
		
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
	
	public String getTypeName(Trigger trigger)
	{
		return mReverseDefinitions.get(trigger.getClass());
	}
	
	private HashMultimap<UUID, Trigger> mAllTriggers = HashMultimap.create();
	private HashMap<String, Trigger> mNamedTriggers = new HashMap<String, Trigger>();

	public void addTrigger(Trigger trigger)
	{
		Validate.isTrue(!mNamedTriggers.containsKey(trigger.getName().toLowerCase()), "Duplicate trigger found");
		
		if(trigger.isValid())
		{
			internalAddTrigger(trigger);
			saveTrigger(trigger);
		}
	}
	
	private void internalAddTrigger(Trigger trigger)
	{
		if(trigger.isValid())
		{
			if(trigger instanceof WorldSpecific)
				mAllTriggers.put(((WorldSpecific)trigger).getWorld(), trigger);
			else
				mAllTriggers.put(null, trigger);
			mNamedTriggers.put(trigger.getName().toLowerCase(), trigger);
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
	
	public Trigger getTrigger(String name)
	{
		return mNamedTriggers.get(name.toLowerCase());
	}
	
	public Collection<Trigger> getTriggers()
	{
		return Collections.unmodifiableCollection(mAllTriggers.values());
	}
	
	public void saveTrigger(Trigger trigger)
	{
		Validate.isTrue(trigger.isValid());
		
		String typeName = mReverseDefinitions.get(trigger.getClass());
		Validate.notNull(typeName, "Trigger type is not registered");
		
		File file = null;
		
		if(trigger instanceof WorldSpecific)
		{
			UUID world = ((WorldSpecific)trigger).getWorld();
			
			if(world != null)
				file = new File(mFolder, world.toString());
		}
		if(file == null)
			file = new File(mFolder, "global");

		file.mkdirs();
		
		file = new File(file, trigger.getName() + ".yml");
		YamlConfiguration config = new YamlConfiguration();
		trigger.write(config);
		config.set("type", typeName);
		
		try
		{
			config.save(file);
		}
		catch(IOException e)
		{
			System.err.println("Failed to save trigger " + trigger.getName());
			e.printStackTrace();
		}
	}
	
	public void loadAll(UUID world)
	{
		Set<Trigger> triggers = mAllTriggers.get(world);
		for(Trigger t : triggers)
			t.onUnload();
		
		mAllTriggers.removeAll(world);
		
		File folder = null;
		
		if(world != null)
			folder = new File(mFolder, world.toString().replace(':', '-'));
		else
			folder = new File(mFolder, "global");
		
		if(!folder.exists())
			return;
		
		for(File file : folder.listFiles())
		{
			if(!file.getName().endsWith(".yml"))
				continue;
			
			Trigger trigger = loadTrigger(file);
			if(trigger != null)
				internalAddTrigger(trigger);
		}
	}
	
	private Trigger loadTrigger(File file)
	{
		if(!file.exists())
			return null;
		
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.load(file);
			String type = config.getString("type");
			
			TriggerDefinition def = getType(type);
			if(def == null)
				throw new InvalidConfigurationException("Cannot find trigger type " + type);
			
			Trigger trigger = def.newBlankTrigger();
			trigger.read(config);
			return trigger;
		}
		catch(InvalidConfigurationException e)
		{
			TriggerItPlugin.getInstance().getLogger().severe("Failed to load trigger " + file.getName() + ":");
			TriggerItPlugin.getInstance().getLogger().severe(e.getMessage());
			return null;
		}
		catch ( IOException e )
		{
			TriggerItPlugin.getInstance().getLogger().severe("Failed to load trigger " + file.getName() + ":");
			TriggerItPlugin.getInstance().getLogger().severe(e.getMessage());
			return null;
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	private void onWorldLoad(WorldLoadEvent event)
	{
		loadAll(event.getWorld().getUID());
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	private void onWorldUnload(WorldUnloadEvent event)
	{
		UUID world = event.getWorld().getUID();
		Set<Trigger> triggers = mAllTriggers.get(world);
		for(Trigger t : triggers)
			t.onUnload();
		
		mAllTriggers.removeAll(world);
	}
	
	public void unloadAll()
	{
		for(Trigger trigger : mAllTriggers.values())
			trigger.onUnload();
		
		mAllTriggers.clear();
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
				mNewTriggerMethod = triggerClass.getMethod("newTrigger", CommandSender.class, String.class, String[].class);
				mTabCompleteMethod = triggerClass.getMethod("tabComplete", CommandSender.class, String[].class);
				mInitializeMethod = triggerClass.getMethod("initializeType", TriggerItPlugin.class);
			}
			catch(NoSuchMethodException e)
			{
				throw new IllegalArgumentException("Missing a required method", e);
			}
		}
		
		
		public Trigger newTrigger(CommandSender sender, String name, String[] args) throws IllegalArgumentException, IllegalStateException, BadArgumentException
		{
			try
			{
				return (Trigger)mNewTriggerMethod.invoke(null, sender, name, args);
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
