package au.com.addstar.triggerit.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import au.com.addstar.triggerit.Action;
import au.com.addstar.triggerit.BasicTextifier;
import au.com.addstar.triggerit.Utilities;
import au.com.addstar.triggerit.commands.BadArgumentException;
import au.com.addstar.triggerit.targets.ParametricTarget;
import au.com.addstar.triggerit.targets.Target;
import au.com.addstar.triggerit.targets.TargetCS;

public class CommandAction implements Action
{
	private String mCommand;
	private TargetCS mExecutor;
	
	public CommandAction()
	{
	}
	
	private static Pattern mPattern = Pattern.compile("@\\[([a-zA-Z0-9_]+)\\]");
	
	@Override
	public void execute( Map<String, Object> arguments )
	{
		mExecutor.setArgumentMap(arguments);
		
		String command = Utilities.replaceArguments(mCommand, arguments, BasicTextifier.instance);
		Matcher m = mPattern.matcher(command);
		
		ArrayList<List<? extends Object>> targets = new ArrayList<List<? extends Object>>();
		ArrayList<Integer> targetIndex = new ArrayList<Integer>();
		ArrayList<Integer> targetStarts = new ArrayList<Integer>();
		ArrayList<Integer> targetEnds = new ArrayList<Integer>();
		
		while(m.find())
		{
			String arg = m.group(1);
			Object val = arguments.get(arg);
			if(val instanceof Collection<?>)
			{
				ParametricTarget target = new ParametricTarget(arg);
				target.setArgumentMap(arguments);
				List<? extends Object> results = target.getTargets();
				if(!results.isEmpty())
				{
					targets.add(results);
					targetIndex.add(0);
					
					targetStarts.add(m.start());
					targetEnds.add(m.end());
				}
			}
		}
		
		if(targets.size() > 0)
		{
			List<? extends CommandSender> executors = mExecutor.getTargets();
			
			outer: while(true)
			{
				StringBuilder cmd = new StringBuilder();
				int lastPos = 0;
				for(int i = 0; i < targets.size(); ++i)
				{
					int start = targetStarts.get(i);
					int end = targetEnds.get(i);
					int index = targetIndex.get(i);
					
					cmd.append(command.substring(lastPos, start));
					lastPos = end;
					
					Object value = targets.get(i).get(index);
					cmd.append(BasicTextifier.instance.asString(value));
				}
				
				cmd.append(command.substring(lastPos));
				
				for(CommandSender sender : executors)
					Bukkit.dispatchCommand(sender, cmd.toString());
				
				// Increment arguments
				int i = 0;
				while(true)
				{
					int index = targetIndex.get(i);
					++index;
					targetIndex.set(i, index);
					if(index >= targets.get(i).size())
					{
						targetIndex.set(i, 0);
						++i;
						if(i >= targets.size())
							break outer;
					}
					else
						break;
				}
			}
		}
		else
		{
			for(CommandSender sender : mExecutor.getTargets())
				Bukkit.dispatchCommand(sender, command);
		}
	}
	
	@Override
	public void load( ConfigurationSection section ) throws InvalidConfigurationException
	{
		mCommand = section.getString("command");
		mExecutor = (TargetCS)Target.loadTarget(section.getConfigurationSection("target"));
	}
	
	@Override
	public void save( ConfigurationSection section )
	{
		section.set("command", mCommand);
		ConfigurationSection target = section.createSection("target");
		mExecutor.save(target);
	}
	
	@Override
	public String[] describe()
	{
		return new String[] {
			ChatColor.GOLD + "Command action:",
			ChatColor.GRAY + " Command: " + ChatColor.YELLOW + mCommand,
			ChatColor.GRAY + " Target: " + ChatColor.YELLOW + mExecutor.describe()
		};
	}
	
	public static CommandAction newAction(CommandSender sender, String[] args) throws IllegalArgumentException, IllegalStateException, BadArgumentException
	{
		if(args.length < 2)
			throw new IllegalStateException("<sender> <command>");
		
		CommandAction action = new CommandAction();
		try
		{
			action.mExecutor = TargetCS.parseTargets(args[0], true);
		}
		catch(IllegalArgumentException e)
		{
			throw new BadArgumentException(0, e.getMessage());
		}
		
		action.mCommand = "";
		for(int i = 1; i < args.length; ++i)
		{
			if(!action.mCommand.isEmpty())
				action.mCommand += " ";
			action.mCommand += args[i];
		}
		
		return action;
	}
	
	public static List<String> tabComplete(CommandSender sender, String[] args)
	{
		return null;
	}

}
