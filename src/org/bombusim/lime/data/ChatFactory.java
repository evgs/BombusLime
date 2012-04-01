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

package org.bombusim.lime.data;

import java.util.ArrayList;

import org.bombusim.lime.Lime;

public class ChatFactory {
	private ArrayList<Chat> chats;

	public ChatFactory() {
		//TODO: restore opened chats state
		chats = new ArrayList<Chat>();
	}
	
	public void closeAllKeepActive() {
		for (Chat chat : chats) {
			chat.resetActiveState(true);
		}
		
		chats.clear();
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
		
		//TODO: raise unread counter
		
		//PROFIT!
		return c;
	}
	
	public void resetActiveState (Chat c) {
		chats.remove(c);
		c.resetActiveState(false);
	}

    public ArrayList<Chat> getChats() {
        return (ArrayList<Chat>) chats.clone();
    }
}
