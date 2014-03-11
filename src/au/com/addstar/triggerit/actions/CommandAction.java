package au.com.addstar.triggerit.actions;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.triggerit.Action;
import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.Utilities;

public class CommandAction implements Action
{
	private String mCommand;
	private String mExecutor;
	
	public CommandAction()
	{
	}
	
	public CommandAction(String command, String executor)
	{
		mCommand = command;
		mExecutor = executor;
	}
	
	@Override
	public void execute( Map<String, Object> arguments )
	{
		CommandSender sender;
		if(mExecutor.equals("~"))
			sender = Bukkit.getConsoleSender();
		else
			sender = Bukkit.getPlayerExact(Utilities.replaceArguments(mExecutor, arguments, this));
		
		if(sender == null)
			TriggerItPlugin.getInstance().getLogger().warning("Failed to execute command action. Unknown player " + Utilities.replaceArguments(mExecutor, arguments, this));
		
		String command = Utilities.replaceArguments(mCommand, arguments, this);
		Bukkit.dispatchCommand(sender, command);
	}
	
	@Override
	public String resolveArgument( Object argument )
	{
		if(argument instanceof Player)
			return ((Player)argument).getName();
		else if(argument instanceof Block)
			return String.format("%d %d %d", ((Block) argument).getX(), ((Block) argument).getY(), ((Block) argument).getZ());
		else if(argument instanceof Location)
			return String.format("%d %d %d", ((Location) argument).getX(), ((Location) argument).getY(), ((Location) argument).getZ());
		
		return argument.toString();
	}

}
