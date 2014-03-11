package au.com.addstar.triggerit;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.apache.commons.lang.Validate;


public class TriggerManager
{
	private HashMap<String, Constructor<? extends Trigger>> mTriggers = new HashMap<String, Constructor<? extends Trigger>>();
	
	public void registerTriggerType(String type, Class<? extends Trigger> typeClass)
	{
		Validate.notNull(typeClass);
		Validate.notNull(type);
		
		try
		{
			mTriggers.put(type, typeClass.getConstructor());
		}
		catch(NoSuchMethodException e)
		{
			throw new IllegalArgumentException("Cannot use " + typeClass.getName() + " as trigger class. Does not provide a default constructor!");
		}
	}
}
