package org.bombusim.lime.data;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.bombusim.lime.Lime;
import org.bombusim.lime.logger.LimeLog;
import org.bombusim.lime.service.XmppServiceBinding;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.handlers.IqVcard;

import android.content.Context;

public class VcardResolver {

	private final static int VCARD_TIMEOUT_S = 30; 
	private final static int VCARD_TIMER_S = 3; 
	
	private ArrayList<Contact> queue;
	
	private Contact pending;
	private long timeout;
	
	private Context context;
	
	public VcardResolver(Context context) {
		resetQueue();
		
		this.context = context;
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
	
	//TODO: parallel vcard fetching for different domains
	
	public synchronized void queryTop() {
		
		long current = System.currentTimeMillis();
		if (pending != null) {
			if (current < timeout) {
				setQueryTimer(); //next polling event
				return; 
			}
			queue.remove(pending);
			//pending.setAvatar(null, null);
		}

		//TODO: move resolver into RosterActivity's context
		//XmppServiceBinding sb = new XmppServiceBinding(context);
		XmppServiceBinding sb = Lime.getInstance().sb;
		
		//sb.doBindService();
		
		try {
			sb.getXmppStream(pending.getRosterJid()).cancelBlockListenerByClass(IqVcard.class);
		} catch (Exception e) {}
		
		timeout = current + VCARD_TIMEOUT_S * 1000;
		
		
		if (!queue.isEmpty()) {
		
			pending = queue.get(queue.size()-1);
			
			
			try {
				XmppStream s = sb.getXmppStream(pending.getRosterJid());
				if (s==null) {
					pending = null;
				} else { 
				
					LimeLog.i("VcardResolver", "Query "+pending.getJid(), null);
					new IqVcard().vcardRequest( pending.getJid(), s	);
					
					setQueryTimer(); 
				}
				
			} catch (Exception e) {
				pending = null;
			}
		}
		
		//sb.doUnbindService();
		
	}

	Timer timer;
	
	private void setQueryTimer() {
		if (timer != null) timer.cancel();
		
		timer = new Timer();
		
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				queryTop();
			}
		}, VCARD_TIMER_S * 1000);
	}
	
}
