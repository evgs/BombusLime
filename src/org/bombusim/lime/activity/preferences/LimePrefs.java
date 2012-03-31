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
         

package org.bombusim.lime.activity.preferences;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.service.XmppServiceBinding;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class LimePrefs extends PreferenceActivity{
    
	public static final String PREFS_CHANGED = "org.bimbusim.lime.PREFS_UPDATE";

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        addPreferencesFromResource(R.xml.roster_prefs);
        addPreferencesFromResource(R.xml.prefs);
	}

	@Override
	protected void onResume() {
	    super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Lime.getInstance().loadPreferences();

		sendBroadcast(new Intent(LimePrefs.PREFS_CHANGED));
	}
}
