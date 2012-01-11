package org.bombusim.lime.data;

import java.util.ArrayList;

import org.bombusim.lime.Lime;
import org.bombusim.lime.logger.LimeLog;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.handlers.IqVcard;

public class VcardResolver {

	private final static int VCARD_TIMEOUT_S = 30; 
	
	private ArrayList<Contact> queue;
	
	private Contact pending;
	private long timeout;
	
	public VcardResolver() {
		resetQueue();
	}
	
	public void resetQueue() {
		queue = new ArrayList<Contact>();
		pending = null;
	}
	
	public void restartQueue() {
		pending = null;
	}
	
	public void vcardNotify(Contact c) {
		queue.remove(c);
		if (pending == c)
		pending = null;
		queryTop();
	}
	
	public void queryVcard(Contact c) {
		//move request to top
		queue.remove(c);
		queue.add(c);
		LimeLog.i("VcardResolver", "Queued "+c.getJid(), null);

		queryTop();
	}
	
	public void queryTop() {
		if (!Lime.getInstance().online) return;
		
		long current = System.currentTimeMillis();
		if (pending != null) {
			if (current < timeout) return; 
			queue.remove(pending);
		}

		try {
			Lime.getInstance().getXmppStream(pending.getRosterJid()).cancelBlockListenerByClass(IqVcard.class);
		} catch (Exception e) {}
		
		timeout = current + VCARD_TIMEOUT_S * 1000;
		
		int index = queue.size()-1;
		
		if (index < 0) return;
		
		pending = queue.get(index);
		
		LimeLog.i("VcardResolver", "Query "+pending.getJid(), null);
		
		try {
			new IqVcard().vcardRequest(
					pending.getJid(),
					Lime.getInstance().getXmppStream(pending.getRosterJid())
				);
		} catch (Exception e) {}
		
		
	}
}
