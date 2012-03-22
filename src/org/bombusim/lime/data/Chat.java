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

import org.bombusim.lime.Lime;

import android.database.Cursor;
import android.util.Log;

public class Chat {
	public static final String UPDATE_CHAT = "org.bombusim.lime.data.UPDATE_CHAT";

	private static final int HISTORY_DEFAULT_CONTEXT_SIZE = 20;
	private static final int HISTORY_UNLIMITED = -1;

	public static final String DELIVERED = "org.bombusim.lime.data.DELIVERED";
	
	Contact visavis;
	
	//XEP-0085 Chat state notifications
	private String chatState;
	
	public boolean acceptComposingEvents() { return chatState!=null; }
	public void setChatState(String state) {
		this.chatState = state;
	}
	
	public boolean isComposing() {
		if (chatState == null) return false;
		return (chatState.equals("composing")); 
	}
	
	private ChatHistoryDbAdapter readDbAdapter;

	//TODO: store this text in database
	private String suspendedText;
	
	public Chat(Contact contact) {
		visavis = contact;
		
		readDbAdapter = new ChatHistoryDbAdapter(Lime.getInstance().getApplicationContext(), visavis.getRosterJid());
		readDbAdapter.open(true);

		updateActiveChatDb(true);
	}
	
	public void resetActiveState(boolean keepActive) {
		//readDbAdapter.close(); done in finalizer
		updateActiveChatDb(false);
	}
	
	//TODO: redsesign cursor handling 
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        readDbAdapter.close();
    }
	
	
	private void updateActiveChatDb(boolean active) {
		if (visavis.hasActiveChats() == active) return;
		
		visavis.setActiveChats(active);
		
		RosterDbAdapter db = new RosterDbAdapter(Lime.getInstance().getApplicationContext());
		db.open();
		db.putContact(visavis, visavis.getId());
		db.close();
	}
	
	public Contact getVisavis() { return visavis; }
	
	public void addMessage(Message msg) {
		synchronized(visavis) {
			if (msg.unread) {
				visavis.setUnread( visavis.getUnread() +1 );
			}
			
			ChatHistoryDbAdapter db = new ChatHistoryDbAdapter(Lime.getInstance().getApplicationContext(), visavis.getRosterJid());
			db.open(false);
			db.putMessage(msg, -1);
			db.close();
		}
	}
	
	public void markRead(long id){
		ChatHistoryDbAdapter db = new ChatHistoryDbAdapter(Lime.getInstance().getApplicationContext(), visavis.getRosterJid());
		db.open(false);
		db.markUnread(id, false);
		db.close();
	}

	public void removeFromHistory(long id) {
		ChatHistoryDbAdapter db = new ChatHistoryDbAdapter(Lime.getInstance().getApplicationContext(), visavis.getRosterJid());
		db.open(false);
		db.removeMessage(id);
		db.close();
	}

	public String getSuspendedText() { 
		return suspendedText; 
	}

	public void saveSuspendedText(String suspendedText) { 
		this.suspendedText = suspendedText; 
	}
	
	public Cursor getCursor() {
		
		//benchmarking
		long st=System.currentTimeMillis();
		Cursor hist = readDbAdapter.getMessageCursor( visavis.getJid(), HISTORY_UNLIMITED );
		long delay = System.currentTimeMillis() - st;
		
		Log.d("SQLite", visavis.getJid() + ": delay=" + delay);
		
		return hist;
	}

	public static int countUnread(String jid, String rosterJid) {
		ChatHistoryDbAdapter rDbA = new ChatHistoryDbAdapter(Lime.getInstance().getApplicationContext(), rosterJid);
		
		rDbA.open(true);
		int unread = rDbA.countUnread(jid);
		rDbA.close();
		
		return unread;
	}
}
