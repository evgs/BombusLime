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

package org.bombusim.lime.logger;

import java.util.ArrayList;

import org.bombusim.lime.Lime;

import android.util.Log;

public class LoggerData {
	public static final String UPDATE_LOG = "org.bombusim.lime.data.UPDATE_ROSTER";

	boolean localXmlEnabled;

	public LoggerData() {
		log = new ArrayList<LoggerEvent>();
	}
	
	private ArrayList<LoggerEvent> log;
	
	int verboseLevel = LoggerEvent.XML;

	public void clear() {
		synchronized (this) {
			log.clear();
		}
	}

	private void add(int eventType, String message, String details) {
		synchronized (this) {
			log.add(new LoggerEvent(eventType, message, details));
			Lime.getInstance().getApplicationContext().sendBroadcast(new android.content.Intent(LoggerData.UPDATE_LOG));
		}
	}
	
	public void addLogEvent(int eventType, String message, String details) {
		if (eventType >= verboseLevel) {
			add(eventType, message, details);
		}
		
		//TODO: broadcast update
	}
	
	public void addLogStreamingEvent(int eventType, String prefix, byte[] data, int length) {
		boolean adbXml = Lime.getInstance().prefs.adbXmlLog;

		if (!localXmlEnabled && !adbXml) return;
		
		StringBuilder sb = new StringBuilder(data.length);
		
		for (int i=0; i<length; i++) {
			sb.append((char) data[i]);
		}
		
		String ds=sb.toString();
		
		String prefixEx="["+prefix+((eventType==LoggerEvent.XMLIN)?"]<<" : "]>>");
		
		if (localXmlEnabled) 	add(eventType, prefixEx, ds);
		if (adbXml)     Log.d(prefixEx, ds);
	}
	
	public ArrayList<LoggerEvent> getLogRecords() { return log; }

}
