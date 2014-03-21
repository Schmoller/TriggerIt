package au.com.addstar.triggerit;

import com.google.common.collect.ImmutableMap.Builder;

public interface ArgumentHandler
{
	/**
	 * Build a list of all arguments available for the specific value
	 * @param value The value to provide arguments for
	 * @param builder The builder to create the map
	 */
	public void buildArguments(Object value, Builder<String, Object> builder);
	
	/**
	 * Returns a string version of the value or null if this handler doesnt handle it
	 */
	public String asString(Object value);
}
