package au.com.addstar.triggerit;

import java.util.Map;

public interface ArgumentProvider
{
	public Map<String, Object> provide(Object value);
}
