package org.bombusim.lime.data;

public class Message {
	public final static int TYPE_MESSAGE_IN = 0;
	public final static int TYPE_MESSAGE_OUT = 1;
	public final static int TYPE_MESSAGE_PRESENCE = 2;
	public int type;
	public String fromJid;	//full jid
	public String nick;
	public String messageBody;
	public String subj;
	private long timestamp;
	
	public Message (int type, String from, String messageBody) {
		this.type = type;
		this.fromJid = from;
		this.messageBody = messageBody;
		
		timestamp = System.currentTimeMillis();
		
		this.expanded = true;
	}
	//view
	public boolean expanded;
}
