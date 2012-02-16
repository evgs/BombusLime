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
