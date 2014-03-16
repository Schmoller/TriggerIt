package au.com.addstar.triggerit.commands.triggers;

import au.com.addstar.triggerit.commands.RootCommandDispatcher;
import au.com.addstar.triggerit.commands.actions.ActionCommand;

public class TriggerCommand extends RootCommandDispatcher
{
	public TriggerCommand()
	{
		super("Allows you to manage triggers");
		
		registerCommand(new NewTriggerCommand());
		registerCommand(new ActionCommand());
		registerCommand(new RunTrigger());
		registerCommand(new ListTriggersCommand());
		registerCommand(new TriggerInfoCommand());
		registerCommand(new TriggerConditionCommand());
	}
}
