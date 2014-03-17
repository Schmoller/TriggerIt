package au.com.addstar.triggerit.conditions;

public class ConditionParseException extends Exception
{
	private static final long serialVersionUID = -3655980584419548120L;
	
	private boolean mSevere;
	private int mToken;

	public ConditionParseException(int token, boolean severe, String message)
	{
		super(message);
		mSevere = severe;
		mToken = token;
	}
	
	public boolean isSevere()
	{
		return mSevere;
	}
	
	public int getToken()
	{
		return mToken;
	}
	
	public void setToken(int token)
	{
		mToken = token;
	}
	
	@Override
	public String toString()
	{
		return String.format("ConditionParseException(token:%d, severe:%s, message:%s)", mToken, mSevere, getMessage()); 
	}
}
