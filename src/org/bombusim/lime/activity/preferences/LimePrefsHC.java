package org.bombusim.lime.activity.preferences;

import java.util.List;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;

import android.content.Intent;
import android.preference.PreferenceActivity;

public class LimePrefsHC extends PreferenceActivity{
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Lime.getInstance().loadPreferences();

        sendBroadcast(new Intent(LimePrefs.PREFS_CHANGED));
    }
}
