package au.com.addstar.triggerit;

/**
 * Represents a trigger object.<br/>
 * Each trigger should have the following 3 static methods:<br/>
 * <ul>
 * <li>public ? extends Trigger newTrigger(CommandSender sender, String[] args) throws IllegalArgumentException, IllegalStateException</li>
 * <li>public List<String> tabComplete(CommanSender sender, String[] args)</li>
 * <li>public void initializeType(TriggerItPlugin plugin)</li>
 * </ul>
 * 
 * newTrigger should create a new trigger using the input. If there is an error in the input, throw an IllegalArgumentException if 
 * one of the arguments is bad. Put the reason in the message. Throw an IllegalStateException if the whole command is wrong. 
 * The message of it should contain the proper usage.<br/>
 * 
 * tabComplete provides tab completion for the newTrigger method<br/>
 * 
 * initializeType should handle setting up needed listeners for the trigger
 */
public interface Trigger
{
	public boolean isValid();
	
	public boolean isEnabled();
	public void setEnabled(boolean enabled);
}
