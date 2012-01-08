package org.bombusim.lime.logger;

import android.graphics.Color;

public class LoggerEvent {
	public String title;
	public String message;
	public int eventType;
	public long timestamp;
	
	public boolean expanded = false;
	
	public LoggerEvent(int eventType, String title, String message) {
		this.eventType = eventType;
		this.title = title;
		this.message = message;
		timestamp = System.currentTimeMillis();
	}
	public LoggerEvent(int eventType, String title) {
		this (eventType, title, null);
	}
	
	
	public final static int XML  = 0;
	public final static int XMLIN  = 0;
	public final static int XMLOUT = 1;
	public final static int DEBUG = 10;
	public final static int INFO = 20;
	public final static int WARNING = 30;
	public final static int ERROR = 90;
	
	private final static int XMLIN_COLOR  = Color.GREEN;
	private final static int XMLOUT_COLOR = Color.CYAN;
	private final static int DEBUG_COLOR = Color.LTGRAY;
	private final static int INFO_COLOR = Color.WHITE;
	private final static int WARNING_COLOR = Color.MAGENTA;
	private final static int ERROR_COLOR = Color.RED;
	
	public int getEventColor() {
		switch (eventType) {
		case XMLIN: return XMLIN_COLOR;
		case XMLOUT: return XMLOUT_COLOR;
		case DEBUG: return DEBUG_COLOR;
		case WARNING: return WARNING_COLOR;
		case ERROR: return ERROR_COLOR;
		case INFO:
		default: return INFO_COLOR;
		}
	}
}
