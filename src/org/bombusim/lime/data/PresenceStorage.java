/*
 * Copyright (c) 2005-2011, Eugene Stahov (evgs@bombus-im.org), 
 * http://bombus-im.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

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
