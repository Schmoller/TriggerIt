package au.com.addstar.triggerit;

import java.util.Map;

public interface Action
{
	public void execute(Map<String, Object> arguments);
	
	public String resolveArgument(Object argument);
}
