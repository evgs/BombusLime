package org.bombusim.lime.data;

import org.bombusim.xmpp.XmppAccount;
import org.bombusim.xmpp.stanza.Presence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PresenceStorage {
	private final static String STORAGE_NAME = "presence";
	
	private final static String STATUS = "status";
	private final static String MESSAGE = "message";
	private final static String PRIORITY = "priority";
	
	private SharedPreferences presence;
	
	public PresenceStorage(Context context) {
		presence = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE );
	}
	
	public int getStatus() { return presence.getInt(STATUS, Presence.PRESENCE_OFFLINE); }
	
	public String getMessage() {return presence.getString(MESSAGE, null);	}
			
	public int getPriority() { return presence.getInt(PRIORITY, XmppAccount.DEFAULT_PRIORITY_ACCOUNT); }
	
	
	public void setPresence( int status, String message, int priority ) { 
		presence.edit()
		  .putInt(STATUS, status)
		  .putString(MESSAGE, message)
		  .putInt(PRIORITY, priority)
		  .commit();
	}
    	
}
