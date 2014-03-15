package au.com.addstar.triggerit;

import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

public interface Action
{
	public void execute(Map<String, Object> arguments);
	
	public void save(ConfigurationSection section);
	public void load(ConfigurationSection section) throws InvalidConfigurationException;
	
	public String[] describe();
}
