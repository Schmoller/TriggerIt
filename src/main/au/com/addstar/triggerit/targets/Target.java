package au.com.addstar.triggerit.targets;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Target<T>
{
	protected Set<? extends Class<? extends T>> specifics;
	
	public Target(Set<? extends Class<? extends T>> specifics)
	{
		this.specifics = specifics;
	}
	
	/**
	 * Checks if a value is allowed by the specifics set.
	 * To be allowed one of the following conditions must be met:
	 * The specifics set is empty,
	 * The values class is in the specifics set,
	 * The values class is a sub class of one in the specifics set
	 */
	protected boolean isValueAllowed(T value)
	{
		if(value == null)
			return false;
		
		if(specifics.isEmpty())
			return true;
		
		for(Class<? extends Object> clazz : specifics)
		{
			if(clazz.isInstance(value))
				return true;
		}
		
		return false;
	}
	
	protected boolean isTypeAllowed(Class<? extends T> value)
	{
		return isTypeAllowed(value, specifics);
	}
	
	public static <T> boolean isTypeAllowed(Class<? extends T> value, Set<? extends Class<? extends Object>> specifics)
	{
		if(specifics.isEmpty())
			return true;
		
		for(Class<? extends Object> clazz : specifics)
		{
			if(clazz.isAssignableFrom(value))
				return true;
		}
		
		return false;
	}
	
	public abstract List<? extends T> getTargets();
	
	public void setArgumentMap(Map<String, Object> arguments) {}
	
	public abstract String describe();
	
	public abstract String toString();
}
