package au.com.addstar.triggerit;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.triggerit.actions.CommandAction;
import au.com.addstar.triggerit.actions.MessageAction;
import au.com.addstar.triggerit.actions.SoundAction;
import au.com.addstar.triggerit.actions.TeleportAction;
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
		
		Bukkit.getScheduler().runTask(this, new Runnable()
		{
			@Override
			public void run()
			{
				for(World world : Bukkit.getWorlds())
					mTriggers.loadAll(world.getUID());
				
				mTriggers.loadAll(null);
				
				Bukkit.getPluginManager().registerEvents(mTriggers, TriggerItPlugin.this);
			}
		});
	}
	
	@Override
	public void onDisable()
	{
		if(mTriggers != null)
			mTriggers.unloadAll();
	}
	
	private void registerTriggers()
	{
		mTriggers.registerTriggerType("Block", BlockTrigger.class);
		mTriggers.registerTriggerType("Custom", CustomTrigger.class);
		mTriggers.registerTriggerType("Redstone", RedstoneTrigger.class);
		mTriggers.registerTriggerType("Time", TimeTrigger.class);
		
		if(Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
			mTriggers.registerTriggerType("Region", RegionTrigger.class);
	}
	
	private void registerActions()
	{
		mActions.registerActionType("Command", CommandAction.class);
		mActions.registerActionType("Message", MessageAction.class);
		mActions.registerActionType("SoundEffect", SoundAction.class);
		mActions.registerActionType("Teleport", TeleportAction.class);
	}
	
	private void registerCommands()
	{
		TriggerCommand trigger = new TriggerCommand();
		trigger.registerAs(getCommand("trigger"));
	}
}
