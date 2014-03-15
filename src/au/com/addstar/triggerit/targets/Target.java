package au.com.addstar.triggerit.targets;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

public abstract class Target<T>
{
	public abstract List<? extends T> getTargets();
	
	public void setArgumentMap(Map<String, Object> arguments) {}
	
	public void save(ConfigurationSection section)
	{
		section.set("~~", getClass().getName());
	}
	
	protected abstract void load(ConfigurationSection section) throws InvalidConfigurationException;
	
	public static Target<? extends Object> parseSingleTarget(String targetString, boolean allowConsole) throws IllegalArgumentException
	{
		if(targetString.startsWith("@"))
			return new ParametricTarget(targetString.substring(1));
		else
			return TargetCS.parseSingleTarget(targetString, allowConsole);
	}
	
	public static Target<? extends Object> parseTargets(String targetString, boolean allowConsole) throws IllegalArgumentException
	{
		if(targetString.startsWith("@"))
			return new ParametricTarget(targetString.substring(1));
		else
			return TargetCS.parseTargets(targetString, allowConsole);
	}
	
	public static Target<? extends Object> loadTarget(ConfigurationSection section) throws InvalidConfigurationException
	{
		try
		{
			String className = section.getString("~~");
			@SuppressWarnings( "unchecked" )
			Class<? extends Target<? extends Object>> clazz = (Class<? extends Target<? extends Object>>)Class.forName(className).asSubclass(Target.class);
			
			Constructor<? extends Target<? extends Object>> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			
			Target<? extends Object> target = constructor.newInstance();
			
			target.load(section);
			
			return target;
		}
		catch(ClassNotFoundException e)
		{
			throw new InvalidConfigurationException("Could not find required Target type: " + e.getMessage());
		}
		catch(NoSuchMethodException e)
		{
			throw new InvalidConfigurationException("Target type does not provide a default constructor. Cannot load it");
		}
		catch ( InvocationTargetException e )
		{
			throw new InvalidConfigurationException("An error occured loading target type " + section.getString("~~"), e.getCause());
		}
		catch (Exception e)
		{
			throw new InvalidConfigurationException("An error occured loading target type " + section.getString("~~"), e);
		}
	}
}
