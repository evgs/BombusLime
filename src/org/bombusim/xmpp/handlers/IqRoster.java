/*
 * IqRoster.java
 * Created on 26.12.2011
 *
 * Copyright (c) 2005-2011, Eugene Stahov (evgs@bombus-im.org), 
 * http://bombus-im.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * You can also redistribute and/or modify this program under the
 * terms of the Psi License, specified in the accompanied COPYING
 * file, as published by the Psi Project; either dated January 1st,
 * 2005, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */


package org.bombusim.xmpp.handlers;

import java.io.IOException;
import java.util.ArrayList;

import org.bombusim.lime.Lime;
import org.bombusim.lime.data.Contact;
import org.bombusim.lime.data.Roster;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppObjectListener;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.stanza.Iq;

public class IqRoster implements XmppObjectListener{

	private String id="LimeR" ;
	
	public void queryRoster(XmppStream stream) {
		XmppObject q=new Iq(null, Iq.TYPE_GET, id);
		q.addChildNs("query", "jabber:iq:roster");
		stream.send(q);
	}
	
	@Override
	public int blockArrived(XmppObject data, XmppStream stream)
			throws IOException {
		try {
			XmppObject iq=(Iq)data;
			//TODO: roster push
			if (iq.getTypeAttribute().equals("result")) {
				XmppObject query=iq.findNamespace("query", "jabber:iq:roster");
				
				//TODO: verify from equals my jid
				ArrayList<XmppObject> items=query.getChildBlocks();
				
				ArrayList<Contact> r=new ArrayList<Contact>();
				
				for (int i=0; i<items.size(); i++) {
					XmppObject item=items.get(i);
					Contact c=new Contact( item.getAttribute("jid"), item.getAttribute("name") );
					c.setSubscription( item.getAttribute("subscription") );
					r.add(c);
				}
				
				Lime.getInstance().getRoster().replaceRoster(r);
				
				stream.sendBroadcast(Roster.UPDATE_ROSTER);
				
				return BLOCK_PROCESSED;
			}
		} catch (Exception e) { /* normal case */ }
		return BLOCK_REJECTED;
	}
	
	@Override
	public String capsXmlns() {	return null; }
	
}
