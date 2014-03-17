package au.com.addstar.triggerit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import au.com.addstar.triggerit.ActionManager.ActionDefinition;
import au.com.addstar.triggerit.conditions.Condition;

/**
 * Represents a trigger object.<br/>
 * Each trigger should have the following 3 static methods:<br/>
 * <ul>
 * <li>public ? extends Trigger newTrigger(CommandSender sender, String name, String[] args) throws IllegalArgumentException, IllegalStateException, BadArgumentException</li>
 * <li>public List<String> tabComplete(CommanSender sender, String[] args)</li>
 * <li>public void initializeType(TriggerItPlugin plugin)</li>
 * </ul>
 * 
 * newTrigger should create a new trigger using the input. If there is an error in the input, throw a BadArgumentException 
 * if the error is a spefic argument, or an IllegalArgumentException if something in general is bad. For both, put the reason in the message. 
 * Throw an IllegalStateException if the format of the command is wrong (eg. argument count). The message of it should contain the proper usage.<br/>
 * 
 * tabComplete provides tab completion for the newTrigger method<br/>
 * 
 * initializeType should handle setting up needed listeners for the trigger
 */
public abstract class Trigger
{
	private boolean mIsEnabled = true;
	private ArrayList<Action> mActions = new ArrayList<Action>();
	private String mName;
	private Condition mCondition;
	
	public Trigger() {}
	
	protected Trigger(String name)
	{
		mName = name;
	}
	
	public abstract boolean isValid();
	
	public boolean isEnabled()
	{
		return mIsEnabled;
	}
	
	public void setEnabled(boolean enabled)
	{
		mIsEnabled = enabled;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public final void addAction(Action action)
	{
		mActions.add(action);
	}
	
	public final void clearActions()
	{
		mActions.clear();
	}
	
	public Condition getCondition()
	{
		return mCondition;
	}
	
	public void setCondition(Condition condition)
	{
		mCondition = condition;
	}
	
	/**
	 * Trigger this trigger with the supplied arguments
	 * 
	 * @param arguments The map of arguments used in this. Cannot be null, can be empty
	 */
	public final void trigger(Map<String, Object> arguments)
	{
		if(!mIsEnabled)
			return;
		
		if(mCondition != null && !mCondition.isTrue(arguments))
			return;
		
		System.out.println("Trigger " + toString() + " triggered");
		Validate.notNull(arguments);
		for(Action action : mActions)
		{
			action.execute(arguments);
		}
	}
	
	/**
	 * Called upon creation or loading
	 */
	public void onLoad() {}
	public void onUnload() {}
	
	public void write(ConfigurationSection section)
	{
		section.set("name", mName);
		section.set("enabled", mIsEnabled);
		ConfigurationSection settings = section.createSection("settings");
		save(settings);
		ConfigurationSection actions = section.createSection("actions");
		actions.set("count", mActions.size());
		
		ActionManager manager = TriggerItPlugin.getInstance().getActionManager();
		
		for(int i = 0; i < mActions.size(); ++i)
		{
			ConfigurationSection act = actions.createSection(String.valueOf(i));
			mActions.get(i).save(act);
			
			act.set("type", manager.getTypeFrom(mActions.get(i)));
		}
		
		if(mCondition != null)
			section.set("condition", mCondition.toString());
	}
	
	public void read(ConfigurationSection section) throws InvalidConfigurationException
	{
		mName = section.getString("name");
		mIsEnabled = section.getBoolean("enabled");
		load(section.getConfigurationSection("settings"));
		
		ConfigurationSection actions = section.getConfigurationSection("actions");
		int count = actions.getInt("count");
		
		ActionManager manager = TriggerItPlugin.getInstance().getActionManager();
		
		mActions.clear();
		for(int i = 0; i < count; ++i)
		{
			ConfigurationSection act = actions.getConfigurationSection(String.valueOf(i));
			String typeName = act.getString("type");
			
			ActionDefinition def = manager.getType(typeName);
			if(def == null)
				throw new InvalidConfigurationException("Unknown action type " + typeName);
			
			Action action = def.newBlankAction();
			
			action.load(act);
			mActions.add(action);
		}
		
		if(section.isString("condition"))
			mCondition = Condition.parse(section.getString("condition"));
	}
	
	protected abstract void save(ConfigurationSection section);
	protected abstract void load(ConfigurationSection section) throws InvalidConfigurationException;
	
	public List<String> describe()
	{
		ArrayList<String> lines = new ArrayList<String>();
		lines.add(String.format("&6Trigger Info: &f%s", mName));
		lines.add(String.format(" &7Type: &e%s", TriggerItPlugin.getInstance().getTriggerManager().getTypeName(this)));
		if(mCondition != null)
			lines.add(String.format(" &7Condition: &e%s", mCondition.toString()));
		String[] desc = describeTrigger();
		for(String line : desc)
			lines.add(String.format(" %s", line));
		
		lines.add(String.format("&6Actions: &f%d", mActions.size()));
		int index = 0;
		for(Action action : mActions)
		{
			lines.add(String.format(" &f%d&7:", index++));
			desc = action.describe();
			for(String line : desc)
				lines.add(String.format(" %s", line));
			lines.add("");
		}
		
		return lines;
	}
	
	protected abstract String[] describeTrigger();
}
