package org.bombusim.lime.data;

import java.util.ArrayList;

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

	}
	
	public void closeChat() {
		readDbAdapter.close();
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
