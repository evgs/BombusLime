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

import org.bombusim.lime.service.XmppService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class AutoRunOnBootCompleted extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		boolean autorun = PreferenceManager.getDefaultSharedPreferences(context)
				            .getBoolean("AUTO_SERVICE_STARTUP", false); 
		if (autorun)
			context.startService(new Intent("onBoot", null, context, XmppService.class));
	}
	
}
