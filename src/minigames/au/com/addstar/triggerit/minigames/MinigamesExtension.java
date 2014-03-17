package au.com.addstar.triggerit.minigames;

import au.com.addstar.triggerit.TriggerItPlugin;

public class MinigamesExtension
{
	public static void load()
	{
		TriggerItPlugin.getInstance().getLogger().info("Adding triggers/actions for Minigames");
		TriggerItPlugin.getInstance().getTriggerManager().registerTriggerType("Minigame", MinigameTrigger.class);
	}
}
