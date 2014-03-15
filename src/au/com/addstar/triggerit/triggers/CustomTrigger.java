package au.com.addstar.triggerit.triggers;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;

public class CustomTrigger extends Trigger
{
	public CustomTrigger() {}
	
	private CustomTrigger(String name) 
	{
		super(name);
	}
	
	@Override
	public boolean isValid()
	{
		return true;
	}
	
	@Override
	protected void load( ConfigurationSection section )
	{
	}
	
	@Override
	protected void save( ConfigurationSection section )
	{
	}
	
	public static CustomTrigger newTrigger(CommandSender sender, String name, String[] args) throws IllegalStateException
	{
		if(args.length != 0)
			throw new IllegalStateException("");
		
		return new CustomTrigger(name);
	}
	
	public static List<String> tabComplete(CommandSender sender, String[] args)
	{
		return null;
	}
	
	public static void initializeType(TriggerItPlugin plugin) {}
}
