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
	
	private boolean onMobile;
	
	public void setOnMobile(boolean onMobile) {
		this.onMobile = onMobile;
	}
	
	public VcardResolver(Context context) {
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
	
	//TODO: parallel vcard fetching for different domains
	
	public synchronized void queryTop() {
		
		if (onMobile) {
			if (! Lime.getInstance().prefs.loadAvatarsOverMobileConnections ) 
				return;
		}
		
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
