package au.com.addstar.triggerit;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang.Validate;

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
	
	/**
	 * Trigger this trigger with the supplied arguments
	 * 
	 * @param arguments The map of arguments used in this. Cannot be null, can be empty
	 */
	public final void trigger(Map<String, Object> arguments)
	{
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
	
}
