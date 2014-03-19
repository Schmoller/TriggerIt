package au.com.addstar.triggerit;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class NullCommandSender implements ConsoleCommandSender
{
	public static NullCommandSender instance = new NullCommandSender();
	
	@Override
	public String getName()
	{
		return "Null";
	}

	@Override
	public Server getServer()
	{
		return Bukkit.getServer();
	}

	@Override
	public void sendMessage( String message ) {}

	@Override
	public void sendMessage( String[] message ) {}

	@Override
	public PermissionAttachment addAttachment( Plugin plugin )
	{
		return new PermissionAttachment(plugin, this); // For compatibility reasons
	}

	@Override
	public PermissionAttachment addAttachment( Plugin plugin, int ticks )
	{
		return new PermissionAttachment(plugin, this); // For compatibility reasons
	}

	@Override
	public PermissionAttachment addAttachment( Plugin plugin, String arg1, boolean arg2 )
	{
		return new PermissionAttachment(plugin, this); // For compatibility reasons
	}

	@Override
	public PermissionAttachment addAttachment( Plugin plugin, String arg1, boolean arg2, int ticks )
	{
		return new PermissionAttachment(plugin, this); // For compatibility reasons
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions()
	{
		return Bukkit.getConsoleSender().getEffectivePermissions();
	}

	@Override
	public boolean hasPermission( String perm ) { return true; }

	@Override
	public boolean hasPermission( Permission perm ) { return true; }

	@Override
	public boolean isPermissionSet( String perm ) { return true; }

	@Override
	public boolean isPermissionSet( Permission permission ) { return true; }

	@Override
	public void recalculatePermissions() {}

	@Override
	public void removeAttachment( PermissionAttachment attachment ) {}

	@Override
	public boolean isOp() { return true; }

	@Override
	public void setOp( boolean op ) {}

	@Override
	public void abandonConversation( Conversation conversation ) {}

	@Override
	public void abandonConversation( Conversation conversation, ConversationAbandonedEvent event ) {}

	@Override
	public void acceptConversationInput( String input ) {}

	@Override
	public boolean beginConversation( Conversation conversation ) { return false; }

	@Override
	public boolean isConversing() { return false; }

	@Override
	public void sendRawMessage( String message ) {}
}
