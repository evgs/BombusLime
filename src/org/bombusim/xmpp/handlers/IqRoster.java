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

	private static final String XMLNS_JABBER_IQ_ROSTER = "jabber:iq:roster";
	private static final String REMOVE = "remove";
	private static final String SUBSCRIPTION = "subscription";
	private String id="LimeR" ;
	
	public void queryRoster(XmppStream stream) {
		XmppObject q=new Iq(null, Iq.TYPE_GET, id);
		q.addChildNs("query", XMLNS_JABBER_IQ_ROSTER);
		stream.send(q);
	}
	
	@Override
	public int blockArrived(XmppObject data, XmppStream stream)
			throws IOException {
		try {
			XmppObject iq=(Iq)data;
			
			XmppObject query=iq.findNamespace("query", XMLNS_JABBER_IQ_ROSTER);
			if (query == null) return BLOCK_REJECTED;

			String from = data.getAttribute("from");
			
			// http://tools.ietf.org/html/rfc6121#section-2.1.6
			//    2.  A receiving client MUST ignore the stanza unless it has no 'from'
		    //   attribute (i.e., implicitly from the bare JID of the user's
		    //   account) or it has a 'from' attribute whose value matches the
		    //   user's bare JID <user@domainpart>.
			
			if (from != null) { 
				if (!from.equals(stream.jid)) return BLOCK_REJECTED;
			} else {
				from = stream.jid;
			}

			String type = iq.getTypeAttribute();
			
			String id = iq.getAttribute("id");

			//roster result or roster push
			if (type.equals("result") || type.equals("set")) {
				ArrayList<Contact> r=new ArrayList<Contact>();
				
				ArrayList<XmppObject> items=query.getChildBlocks();
				
				for (XmppObject item : items) {
					Contact c=new Contact( item.getAttribute("jid"), item.getAttribute("name") );
					c.setSubscription( item.getAttribute(SUBSCRIPTION) );
					c.setRJid(from);
					
					// adding group names
					ArrayList<XmppObject> groups = item.getChildBlocks();
					if (groups !=null) {
						for (XmppObject group : groups) {
							if (group.getTagName().equals("group")) c.addGroup(group.getText());
						}
					}
					
					r.add(c);
				}

				boolean set = type.equals("set");
				Lime.getInstance().getRoster().replaceRoster(r, from, !set);
				
				if (set) 				
					stream.send(new Iq(null,Iq.TYPE_RESULT, id));

				
				stream.sendBroadcast(Roster.UPDATE_ROSTER);
				
				return BLOCK_PROCESSED;
			}
		} catch (Exception e) { /* normal case */ }
		return BLOCK_REJECTED;
	}
	
	@Override
	public String capsXmlns() {	return null; }

	public static void deleteContact(String jid, XmppStream stream) {
		XmppObject q = new Iq(null, Iq.TYPE_SET, "rm"+jid);
		
		XmppObject item = q.addChildNs("query", XMLNS_JABBER_IQ_ROSTER)
			.addChild("item", null);
		
		item.setAttribute("jid", jid);
		item.setAttribute(SUBSCRIPTION, REMOVE);
		
		stream.send(q);
	}
	
}
