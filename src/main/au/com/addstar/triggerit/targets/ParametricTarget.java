package au.com.addstar.triggerit.targets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

public class ParametricTarget extends Target<Object>
{
	private String mArgument;
	private Map<String, Object> mArguments;
	
	public ParametricTarget(String argument, Set<Class<? extends Object>> specifics)
	{
		super(specifics);
		mArgument = argument;
	}
	
	@Override
	public void setArgumentMap(Map<String, Object> arguments)
	{
		mArguments = arguments;
	}
	
	
	@Override
	public List<? extends Object> getTargets()
	{
		Validate.notNull(mArguments, "Argument map not set for ParametricTarget, use setArgumentMap()");
		
		Object obj = mArguments.get(mArgument);
		
		if(obj instanceof Collection<?>)
		{
			ArrayList<Object> result = new ArrayList<Object>(((Collection<?>)obj).size());
			for(Object o : (Collection<?>)obj)
			{
				if(isValueAllowed(o))
					result.add(o);
			}
			
			return result;
		}
		else if(isValueAllowed(obj))
			return Arrays.asList(obj);
		else
			return Collections.emptyList();
	}
	
	@Override
	public String describe()
	{
		return "Value of argument '" + mArgument + "'";
	}
	
	@Override
	public String toString()
	{
		return "@" + mArgument;
	}
}
