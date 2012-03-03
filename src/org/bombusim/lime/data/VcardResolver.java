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
import org.bombusim.xmpp.XmppJid;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.handlers.IqVcard;

import android.content.Context;

public class VcardResolver {

	private ArrayList<Contact> queue;
	
	private Contact pending;

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
		
		if (pending != null) return; 

		//TODO: move resolver into RosterActivity's context
		//XmppServiceBinding sb = new XmppServiceBinding(context);
		XmppServiceBinding sb = Lime.getInstance().sb;
		
		//sb.doBindService();
		
		try {
			sb.getXmppStream(pending.getRosterJid()).cancelBlockListenerByClass(IqVcard.class);
		} catch (Exception e) {}
		
		
		if (!queue.isEmpty()) {
		
			pending = queue.get(queue.size()-1);
			
			
			try {
				XmppStream s = sb.getXmppStream(pending.getRosterJid());
				if (s==null) {
					pending = null;
				} else { 
				
				    String from = pending.getJid();
				    
					queryVCard(from, s);
				}
				
			} catch (Exception e) {
			    e.printStackTrace();
				pending = null;
			}
		}
		
		//sb.doUnbindService();
		
	}

    private void queryVCard(String from, final XmppStream s) {
        LimeLog.i("VcardResolver", "Query "+from, null);
        
        IqVcard vq = new IqVcard();
        
        vq.setVcardListener(new IqVcard.VCardListener() {
            
            @Override
            public void onVcardArrived(String from, Vcard result) {
                if (result != null) {
                    Lime.getInstance().getRoster().notifyVcard(result);
                    s.sendBroadcast(Roster.UPDATE_CONTACT, from);
                }
                
                removeFromQueue(from);
                queryTop();
            }
        });
        
        vq.vcardRequest( from, s	);
    }

    protected void removeFromQueue(String from) {
        queue.remove(pending);
        pending = null;
    }
}
