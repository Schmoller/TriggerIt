package au.com.addstar.triggerit.commands.actions;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.command.CommandSender;

import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.commands.CommandDispatcher;
import au.com.addstar.triggerit.commands.CommandSenderType;
import au.com.addstar.triggerit.commands.ICommand;

public class ActionCommand extends CommandDispatcher implements ICommand
{
	public ActionCommand()
	{
		super("Allows you to add, remove, and change actions on triggers");
		registerCommand(new AddActionCommand());
		registerCommand(new ClearActionsCommand());
		registerCommand(new RemoveActionCommand());
		registerCommand(new InsertActionCommand());
		registerCommand(new SetActionCommand());
	}

	@Override
	public String getName()
	{
		return "action";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return null;
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label;
	}

	@Override
	public String getDescription()
	{
		return "Allows you to add, remove, and change actions on triggers";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		return dispatchCommand(sender, parent, label, args);
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return tabComplete(sender, parent, label, args);
	}

}
