package org.bombusim.lime;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.preference.PreferenceManager;
import android.provider.Settings;

public class Preferences {
	public boolean autoServiceStartup;
	public boolean adbXmlLog;
	public String ringtoneMessage;
	public boolean vibraNotifyMessage;
	public boolean ledNotifyMessage;
	
	public int keepAlivePeriodMinutes;
	
	public Preferences(Context applicationContext) {
		loadPreferences(applicationContext);
	}

	public void loadPreferences(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		autoServiceStartup = prefs.getBoolean("AUTO_SERVICE_STARTUP", false);
		adbXmlLog = prefs.getBoolean("ADB_XML_LOG", true);
		
		ringtoneMessage    = prefs.getString("RINGTONE_MESSAGE",
				RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString()
		);
		
		vibraNotifyMessage = prefs.getBoolean("NOTIFY_VIBRA", true);
		ledNotifyMessage   = prefs.getBoolean("NOTIFY_LED",   true);
		
		String ka = prefs.getString("KEEP_ALIVE_PERIOD", "");
		
		//TODO: dialog layout with int restriction
		try {
			keepAlivePeriodMinutes = Integer.parseInt(ka);
		} catch (Exception e) {
			keepAlivePeriodMinutes = 10;
		}
	}
	
}
