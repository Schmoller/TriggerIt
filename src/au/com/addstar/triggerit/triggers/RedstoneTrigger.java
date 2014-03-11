package au.com.addstar.triggerit.triggers;

import au.com.addstar.triggerit.Trigger;

public class RedstoneTrigger implements Trigger
{
	@Override
	public boolean isValid()
	{
		return false;
	}

	@Override
	public boolean isEnabled()
	{
		return false;
	}

	@Override
	public void setEnabled( boolean enabled )
	{
	}

}
