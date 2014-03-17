package au.com.addstar.triggerit.targets;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

public class ParametricTarget extends Target<Object>
{
	private String mArgument;
	private Map<String, Object> mArguments;
	
	public ParametricTarget(String argument)
	{
		mArgument = argument;
	}
	
	protected ParametricTarget() {}
	
	@Override
	public void setArgumentMap(Map<String, Object> arguments)
	{
		mArguments = arguments;
	}
	
	
	@Override
	public List<? extends Object> getTargets()
	{
		Validate.notNull(mArguments, "Argument map not set for ParametricTarget, use setArgumentMap()");
		
		return Arrays.asList(mArguments.get(mArgument));
	}
	
	@Override
	protected void load( ConfigurationSection section ) throws InvalidConfigurationException
	{
		mArgument = section.getString("argument");
	}
	
	@Override
	public void save( ConfigurationSection section )
	{
		super.save(section);
		section.set("argument", mArgument);
	}

	@Override
	public String describe()
	{
		return "Value of argument '" + mArgument + "'";
	}
}
