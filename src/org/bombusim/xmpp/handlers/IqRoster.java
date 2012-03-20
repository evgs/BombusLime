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
import java.util.ArrayList;

import org.bombusim.lime.Lime;
import org.bombusim.lime.data.Contact;
import org.bombusim.lime.data.Roster;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppObjectListener;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.stanza.Iq;
import org.bombusim.xmpp.stanza.XmppPresence;

public class IqRoster extends XmppObjectListener{

	private static final String XMLNS_JABBER_IQ_ROSTER = "jabber:iq:roster";
	private static final String REMOVE = "remove";
	private static final String SUBSCRIPTION = "subscription";
	//TODO: set random id
	private String id="LimeR" ;
	
	public void queryRoster(XmppStream stream) {
		XmppObject q=new Iq(null, Iq.TYPE_GET, id);
		q.addChildNs("query", XMLNS_JABBER_IQ_ROSTER);
		stream.postStanza(q);
	}
	
	@Override
	public int blockArrived(XmppObject data, XmppStream stream)
			throws IOException {
		try {
			XmppObject iq=(Iq)data;
			
			XmppObject query=iq.findNamespace("query", XMLNS_JABBER_IQ_ROSTER);
			if (query == null) return BLOCK_REJECTED;

			String from = data.getAttribute("from");
			
			String type = iq.getTypeAttribute();
			
			String id = iq.getAttribute("id");

			if (type.equals("set")) {
				// http://tools.ietf.org/html/rfc6121#section-2.1.6
				//    2.  A receiving client MUST ignore the stanza unless it has no 'from'
			    //   attribute (i.e., implicitly from the bare JID of the user's
			    //   account) or it has a 'from' attribute whose value matches the
			    //   user's bare JID <user@domainpart>.
				if (from != null) 
					if (!from.equals(stream.jid)) {
						//TODO: send <service-unavailable/> to protect privacy
						return BLOCK_REJECTED;
					}
				
			} else if (type.equals("result")) {
				
				if (!this.id.equals(id)) return BLOCK_REJECTED;
				
 			} else {
 				// type == "get" || type == "error
 				return BLOCK_REJECTED;
			}
			
			from = stream.jid;

			//handle roster result or roster push
			
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

			//Global roster update
			stream.sendBroadcast(Roster.UPDATE_CONTACT);
			
			//sending initial presence
			stream.sendPresence();
			
			return BLOCK_PROCESSED;
			
		} catch (Exception e) { /* normal case */ }
		return BLOCK_REJECTED;
	}
	
	public static void deleteContact(String jid, XmppStream stream) {
		XmppObject q = new Iq(null, Iq.TYPE_SET, "rm_"+jid);
		
		XmppObject item = q.addChildNs("query", XMLNS_JABBER_IQ_ROSTER)
			.addChild("item", null);
		
		item.setAttribute("jid", jid);
		item.setAttribute(SUBSCRIPTION, REMOVE);
		
		stream.postStanza(q);
	}

	public static void setContact(Contact contact, XmppStream stream) {
		XmppObject q = new Iq(null, Iq.TYPE_SET, "upd_"+contact.getJid());
		
		XmppObject item = q.addChildNs("query", XMLNS_JABBER_IQ_ROSTER)
			.addChild("item", null);
		
		item.setAttribute("jid", contact.getJid());
		
		item.setAttribute("name", contact.getName());
		for (String group : contact.getAllGroupsArray()) {
			item.addChild("group", group);
		}
		
		stream.postStanza(q);
	}
	
	public static void setSubscription(String toJid, String subscription, XmppStream stream) {
		XmppObject q = new XmppPresence(toJid, subscription);
		
		stream.postStanza(q);
	}

}
