package org.bombusim.lime;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {
	public boolean autoServiceStartup;
	public boolean adbXmlLog;
	
	public Preferences(Context applicationContext) {
		loadPreferences(applicationContext);
	}

	public void loadPreferences(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		autoServiceStartup = prefs.getBoolean("AUTO_SERVICE_STARTUP", false);
		adbXmlLog = prefs.getBoolean("ADB_XML_LOG", true);
	}
	
}
