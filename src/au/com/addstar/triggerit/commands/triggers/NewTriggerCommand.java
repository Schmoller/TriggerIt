package au.com.addstar.triggerit.commands.triggers;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.command.CommandSender;

import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.TriggerManager;
import au.com.addstar.triggerit.TriggerManager.TriggerDefinition;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.commands.CommandSenderType;
import au.com.addstar.triggerit.commands.ICommand;

public class NewTriggerCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "new";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "triggerit.command.trigger.new";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <type> <options>";
	}

	@Override
	public String getDescription()
	{
		return "Creates a new trigger of type <type>";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Console, CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if(args.length < 1)
			return false;
		
		TriggerManager manager = TriggerItPlugin.getInstance().getTriggerManager();
		TriggerDefinition def = manager.getType(args[0]);
		
		if(def == null)
			throw new BadArgumentException(0, "Unknown trigger type");
		
		try
		{
			Trigger trigger = def.newTrigger(sender, Arrays.copyOfRange(args, 1, args.length));
			manager.addTrigger(trigger);
		}
		catch(BadArgumentException e)
		{
			e.setArgument(e.getArgument()+1);
			throw e;
		}
		catch(IllegalStateException e)
		{
			throw new IllegalArgumentException("Usage " + parent + label + " <type> " + e.getMessage());
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		TriggerManager manager = TriggerItPlugin.getInstance().getTriggerManager();
		if(args.length == 1)
			return Utilities.matchString(args[0], manager.getTypeNames());
		else
		{
			TriggerDefinition def = manager.getType(args[0]);
			
			if(def != null)
				return def.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
		}
		return null;
	}

}
