package au.com.addstar.triggerit.commands.triggers;

import au.com.addstar.triggerit.commands.RootCommandDispatcher;

public class TriggerCommand extends RootCommandDispatcher
{
	public TriggerCommand()
	{
		super("Allows you to manage triggers");
		
		registerCommand(new NewTriggerCommand());
	}
}
