package org.bombusim.lime.data;

import java.util.ArrayList;

public class Chat {
	public static final String UPDATE_CHAT = "org.bombusim.lime.data.UPDATE_CHAT";
	
	Contact visavis;
	
	private ArrayList<Message> messages;
	
	public Chat(Contact contact) {
		visavis = contact;
		//TODO: load recent messages
		messages = new ArrayList<Message>();
	}
	
	public Contact getVisavis() { return visavis; }
	
	public int getChatSize() { return messages.size(); }
	public Message getMessage(int index) { return messages.get(index); }
	
	public void addMessage(Message msg) {
		messages.add(msg);
	}
	
	public int getUnreadCount() {
		//TODO: return unread messages count
		return 0;
	}
	public void markRead(int index){
		//TODO: mark unread
	}
}
