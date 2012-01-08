package org.bombusim.lime.activity;

import org.bombusim.lime.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class LimePrefs extends PreferenceActivity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}

}
