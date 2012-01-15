package org.bombusim.lime.data;

import java.util.ArrayList;

import org.bombusim.lime.Lime;

public class ChatFactory {
	private ArrayList<Chat> chats;

	public ChatFactory() {
		//TODO: restore opened chats state
		chats = new ArrayList<Chat>();
	}
	
	private static ChatFactory getChatFactory() {
		return Lime.getInstance().getChatFactory();
	}
	
	public Chat getChat(String visavisJid, String myJid) {
		Contact visavis = null;
		//1. search already opened chat
		for (int index = 0; index < chats.size(); index++) {
			Chat c = chats.get(index);
			visavis = c.visavis;
			if (visavis.getJid().equals(visavisJid) && visavis.getRosterJid().equals(myJid)) 
				return c;
		}
		//2. searching visavis in roster
		visavis = Lime.getInstance().getRoster().findContact(visavisJid, myJid);
		
		//3. initial policy: drop message if contact is not in our rosters
		if (visavis == null) return null;
		
		//4. creating chat for existing contact
		Chat c = new Chat(visavis);
		
		synchronized (chats) {
			chats.add(c);
		}
		
		//PROFIT!
		return c;
	}
}
