package org.bombusim.lime.activity.preferences;

import java.util.List;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class LimePrefsHC extends SherlockPreferenceActivity{
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        getSupportActionBar().setTitle(R.string.preferences);
        
        //TODO: handle "home"
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        super.onCreate(savedInstanceState);
    }
    
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
