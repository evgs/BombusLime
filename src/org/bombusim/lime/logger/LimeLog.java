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
}
