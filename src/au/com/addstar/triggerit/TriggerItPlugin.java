package au.com.addstar.triggerit;

import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.triggerit.actions.CommandAction;
import au.com.addstar.triggerit.commands.triggers.TriggerCommand;
import au.com.addstar.triggerit.triggers.*;

public class TriggerItPlugin extends JavaPlugin
{
	private TriggerManager mTriggers;
	private ActionManager mActions;
	private static TriggerItPlugin mInstance;
	
	public static TriggerItPlugin getInstance()
	{
		return mInstance;
	}
	
	public TriggerManager getTriggerManager()
	{
		return mTriggers;
	}
	
	public ActionManager getActionManager()
	{
		return mActions;
	}
	
	@Override
	public void onEnable()
	{
		mInstance = this;
		mTriggers = new TriggerManager();
		mActions = new ActionManager();
		
		registerTriggers();
		registerActions();
		registerCommands();
		
		mTriggers.initializeAll(this);
	}
	
	private void registerTriggers()
	{
		//mTriggers.registerTriggerType("Region", RegionTrigger.class);
		mTriggers.registerTriggerType("Block", BlockTrigger.class);
//		mTriggers.registerTriggerType("ChatCommand", ChatCommandTrigger.class);
//		mTriggers.registerTriggerType("Login", LoginTrigger.class);
		mTriggers.registerTriggerType("Redstone", RedstoneTrigger.class);
	}
	
	private void registerActions()
	{
		mActions.registerActionType("Command", CommandAction.class);
	}
	
	private void registerCommands()
	{
		TriggerCommand trigger = new TriggerCommand();
		trigger.registerAs(getCommand("trigger"));
	}
}
