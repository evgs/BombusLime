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

import org.bombusim.lime.Lime;

import android.util.Log;

public class LimeLog {
	public static void i (String tag, String message, String details) {
		Log.i(tag, message);
		Lime.getInstance().getLog().addLogEvent(LoggerEvent.INFO, tag+": "+message, details);
	}
	public static void d (String tag, String message, String details) {
		Log.d(tag, message);
		Lime.getInstance().getLog().addLogEvent(LoggerEvent.DEBUG, tag+": "+message, details);
	}
	public static void w (String tag, String message, String details) {
		Log.d(tag, message);
		Lime.getInstance().getLog().addLogEvent(LoggerEvent.WARNING, tag+": "+message, details);
	}
	public static void e (String tag, String message, String details) {
		Log.e(tag, message);
		Lime.getInstance().getLog().addLogEvent(LoggerEvent.ERROR, tag+": "+message, details);
	}
	public static boolean getLocalXmlEnabled() {
		return Lime.getInstance().getLog().localXmlEnabled;
	}
	public static void setlocalXmlEnabled(boolean enabled) {
		Lime.getInstance().getLog().localXmlEnabled = enabled;
	}
}
