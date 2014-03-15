package au.com.addstar.triggerit.commands.triggers;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.ChatPaginator;
import org.bukkit.util.ChatPaginator.ChatPage;

import au.com.addstar.triggerit.Trigger;
import au.com.addstar.triggerit.TriggerItPlugin;
import au.com.addstar.triggerit.TriggerManager;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.commands.CommandSenderType;
import au.com.addstar.triggerit.commands.ICommand;

public class ListTriggersCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "list";
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
		return label + " [page]";
	}

	@Override
	public String getDescription()
	{
		return "Lists all the triggers that exist";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if(args.length > 2)
			return false;
		
		int pageNo = 1;
		if(args.length == 1)
		{
			try
			{
				pageNo = Integer.parseInt(args[0]);
				if(pageNo <= 0)
					throw new BadArgumentException(0, "Page number must be greater or equal to 1");
			}
			catch(NumberFormatException e)
			{
				throw new BadArgumentException(0, "Page number must be a number greater or equal to 1");
			}
		}
		
		TriggerManager manager = TriggerItPlugin.getInstance().getTriggerManager();

		StringBuilder builder = new StringBuilder();
		Collection<Trigger> triggers = manager.getTriggers();
		for(Trigger trigger : triggers)
			builder.append(ChatColor.translateAlternateColorCodes('&', String.format("&e%s &7[%s]\n", trigger.getName(), manager.getTypeName(trigger))));
		
		ChatPage page = ChatPaginator.paginate(builder.toString(), pageNo);
		
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&e%d &6loaded triggers Page &c%d &6of &c%d", triggers.size(), page.getPageNumber(), page.getTotalPages())));
		sender.sendMessage(page.getLines());
		
		if(page.getPageNumber() != page.getTotalPages())
			sender.sendMessage(ChatColor.GOLD + "Use " + ChatColor.YELLOW + parent + label + " " + (page.getPageNumber()+1) + ChatColor.GOLD + " to get to the next page");
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
