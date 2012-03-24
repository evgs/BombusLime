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


package org.bombusim.lime;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.preference.PreferenceManager;

public class Preferences {
	public boolean autoServiceStartup;
	public boolean adbXmlLog;
	public String ringtoneMessage;
	public boolean vibraNotifyMessage;
	public boolean ledNotifyMessage;
	
	public int keepAlivePeriodMinutes;
	public boolean loadAvatarsOverMobileConnections;
	
	public boolean hideOfflines;
    public boolean wifiLock;
	
	public Preferences(Context applicationContext) {
		loadPreferences(applicationContext);
	}

	public void loadPreferences(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		loadAvatarsOverMobileConnections = prefs.getBoolean("VCARD_OVER_GPRS", true);
		
		autoServiceStartup = prefs.getBoolean("AUTO_SERVICE_STARTUP", false);
		adbXmlLog = prefs.getBoolean("ADB_XML_LOG", true);
		
		ringtoneMessage    = prefs.getString("RINGTONE_MESSAGE",
				RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString()
		);
		
		vibraNotifyMessage = prefs.getBoolean("NOTIFY_VIBRA", true);
		ledNotifyMessage   = prefs.getBoolean("NOTIFY_LED",   true);
		
        wifiLock = prefs.getBoolean("WIFI_LOCK", false);
		
		String ka = prefs.getString("KEEP_ALIVE_PERIOD", "");
		
		//TODO: dialog layout with int restriction
		try {
			keepAlivePeriodMinutes = Integer.parseInt(ka);
		} catch (Exception e) {
			keepAlivePeriodMinutes = 10;
		}
		
		//TODO: load from roster preferences
		prefs = context.getSharedPreferences("rosterPrefs", Context.MODE_PRIVATE);
		hideOfflines = prefs.getBoolean("HIDE_OFFLINES", false);
	}
	
}
