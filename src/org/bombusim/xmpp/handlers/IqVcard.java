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

package org.bombusim.xmpp.handlers;

import java.io.IOException;

import org.bombusim.lime.Lime;
import org.bombusim.lime.data.Roster;
import org.bombusim.lime.data.Vcard;
import org.bombusim.xmpp.XmppJid;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppObjectListener;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.exception.XmppException;
import org.bombusim.xmpp.handlers.IqVcard.VCardListener;
import org.bombusim.xmpp.stanza.Iq;

import android.os.Handler;

public class IqVcard extends XmppObjectListener {

    private String jid;
    
    public interface VCardListener {
        /**
         *  Notifies vcard arrived. Resulting VCard may be null if error encountered
         * @param from contact's Jid vcard pulled from
         * @param result pulled Vcard. may be null if no VCard available
         */
        public void onVcardArrived(String from, Vcard result);
    }

    private VCardListener mVcardListener;
    
    public void setVcardListener(VCardListener listener) {
        mVcardListener = listener;
    }
    
    private static final int VCARD_TIMEOUT = 30;

    private Thread timeoutThread = new Thread() {
        @Override
        public void run() {
            
            try {
                sleep(VCARD_TIMEOUT *1000);
            } catch (InterruptedException e) {
                return; // no need to timeout callback
            }
            
            if (!isInterrupted() && mVcardListener != null) {
                mVcardListener.onVcardArrived(jid, null);
            }
        }
    };

	@Override
	public int blockArrived(XmppObject data, XmppStream stream)
			throws IOException, XmppException {
		//TODO: handle VCard iq errors
		if (!(data instanceof Iq)) return BLOCK_REJECTED;
		
		if (data.getTypeAttribute().equals("error")) return BLOCK_REJECTED;
		
		if (!data.getAttribute("from").equals(jid) ) return BLOCK_REJECTED;
		
		XmppObject vcard = data.findNamespace("vCard", "vcard-temp");
		
		if (vcard == null) return BLOCK_REJECTED;
		
		String from = data.getAttribute("from");
		
		Vcard result = new Vcard(from, vcard);
		
		if (mVcardListener != null) {
		    timeoutThread.interrupt();
		    mVcardListener.onVcardArrived(jid, result);
		}
		
		return NO_MORE_BLOCKS;
	}

	@Override
	public String capsXmlns() { return null; }
	
	
	public void vcardRequest(String jid, XmppStream stream) {
		Iq request = new Iq(jid, Iq.TYPE_GET, "getVc");
		request.addChildNs("vCard", "vcard-temp");
		
		this.jid = jid;
		
		stream.addBlockListener(this);
		stream.send(request);
		
		timeoutThread.start();
	}

}
