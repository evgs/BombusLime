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
	
	private ChatHistoryDbAdapter readDbAdapter;
	
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
		//TODO: unread
		ChatHistoryDbAdapter db = new ChatHistoryDbAdapter(Lime.getInstance().getApplicationContext(), visavis.getRosterJid());
		db.open(false);
		db.putMessage(msg, -1);
		db.close();
	}
	
	public void markRead(long id){
		//TODO: mark unread
	}

	public void removeFromHistory(long id) {
		ChatHistoryDbAdapter db = new ChatHistoryDbAdapter(Lime.getInstance().getApplicationContext(), visavis.getRosterJid());
		db.open(false);
		db.removeMessage(id);
		db.close();
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
