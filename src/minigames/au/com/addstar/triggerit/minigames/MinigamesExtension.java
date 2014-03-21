package au.com.addstar.triggerit.minigames;

import au.com.addstar.triggerit.ActionManager;
import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.minigames.actions.EndMinigameAction;
import au.com.addstar.triggerit.minigames.actions.JoinMinigameAction;
import au.com.addstar.triggerit.minigames.actions.StartMinigameAction;

public class MinigamesExtension
{
	public static void load()
	{
		TriggerItPlugin.getInstance().getLogger().info("Adding triggers/actions for Minigames");
		TriggerItPlugin.getInstance().getTriggerManager().registerTriggerType("Minigame", MinigameTrigger.class);
		
		ActionManager actions = TriggerItPlugin.getInstance().getActionManager();
		
		actions.registerActionType("MgEnd", EndMinigameAction.class);
		actions.registerActionType("MgStart", StartMinigameAction.class);
		actions.registerActionType("MgJoin", JoinMinigameAction.class);
	}
}
