package au.com.addstar.triggerit;

import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.triggerit.triggers.*;

public class TriggerItPlugin extends JavaPlugin
{
	private TriggerManager mTriggers;
	private static TriggerItPlugin mInstance;
	
	public static TriggerItPlugin getInstance()
	{
		return mInstance;
	}
	
	public TriggerManager getTriggerManager()
	{
		return mTriggers;
	}
	
	@Override
	public void onEnable()
	{
		mInstance = this;
		mTriggers = new TriggerManager();
		
		registerTriggers();
	}
	
	private void registerTriggers()
	{
		mTriggers.registerTriggerType("Region", RegionTrigger.class);
		mTriggers.registerTriggerType("Block", BlockTrigger.class);
		mTriggers.registerTriggerType("ChatCommand", ChatCommandTrigger.class);
		mTriggers.registerTriggerType("Login", LoginTrigger.class);
		mTriggers.registerTriggerType("Redstone", RedstoneTrigger.class);
	}
}
