package au.com.addstar.triggerit.targets;

import java.util.List;
import java.util.Map;

public abstract class Target<T>
{
	public abstract List<? extends T> getTargets();
	
	public void setArgumentMap(Map<String, Object> arguments) {} 
	
	public static Target<? extends Object> parseTargets(String targetString, boolean allowConsole) throws IllegalArgumentException
	{
		if(targetString.startsWith("@"))
			return new ParametricTarget(targetString.substring(1));
		else
			return TargetCS.parseTargets(targetString, allowConsole);
	}
}
