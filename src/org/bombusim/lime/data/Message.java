package org.bombusim.lime.data;

import android.graphics.Color;

public class Message {
	public final static int TYPE_MESSAGE_IN = 0;
	public final static int TYPE_MESSAGE_OUT = 1;
	public final static int TYPE_MESSAGE_PRESENCE = 2;
	public int type;
	public String fromJid;	//full jid
	public String nick;
	public String messageBody;
	public String subj;
	public long timestamp;
	
	public Message (int type, String from, String messageBody) {
		this.type = type;
		this.fromJid = from;
		this.messageBody = messageBody;
		
		timestamp = System.currentTimeMillis();
		
		this.expanded = true;
	}
	//view
	public boolean expanded;

	private final static int IN_COLOR  = Color.GREEN;
	private final static int OUT_COLOR = Color.CYAN;
	private final static int PRESENCE_COLOR = Color.GRAY;

	public static int getColor(int type) {
		switch (type) {
		case TYPE_MESSAGE_IN: return IN_COLOR;
		case TYPE_MESSAGE_OUT: return OUT_COLOR;
		default: 
		}
		return PRESENCE_COLOR;
	}
}
