package au.com.addstar.triggerit;

import java.util.Set;

import au.com.addstar.triggerit.targets.Target;

public interface ITargetParser<T>
{
	public Class<T> getBaseType();
	
	public Target<? extends T> parseTargets(String targetString, Set<? extends Class<? extends T>> specifics) throws IllegalArgumentException;
	public Target<? extends T> parseSingleTarget(String targetString, Set<? extends Class<? extends T>> specifics) throws IllegalArgumentException;
}
