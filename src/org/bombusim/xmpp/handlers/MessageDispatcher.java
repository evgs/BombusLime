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
import org.bombusim.lime.data.Chat;
import org.bombusim.lime.data.Message;
import org.bombusim.lime.data.Roster;
import org.bombusim.lime.logger.LimeLog;
import org.bombusim.xmpp.XmppJid;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppObjectListener;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.exception.XmppException;
import org.bombusim.xmpp.stanza.XmppMessage;

public class MessageDispatcher extends XmppObjectListener{

	public static final String URN_XMPP_RECEIPTS = "urn:xmpp:receipts";

	@Override
	public int blockArrived(XmppObject data, XmppStream stream)
			throws IOException, XmppException {
		if (!(data instanceof XmppMessage)) return BLOCK_REJECTED;
		
		XmppMessage m = (XmppMessage) data;
		XmppJid from = new XmppJid( m.getFrom() );

		String body = m.getBody();

		String id = m.getAttribute("id");

		if (m.findNamespace("received", URN_XMPP_RECEIPTS) != null) {
			stream.sendBroadcast(Chat.DELIVERED, id);
			return BLOCK_PROCESSED;
		}
		
		//TODO: composing events
		if (body.length() == 0) return BLOCK_REJECTED;
		
		Message msg = new Message(Message.TYPE_MESSAGE_IN, from.getBareJid(), body);
		msg.subj = m.getSubject();
		
		msg.timestamp = m.getTimeStamp();
		
        Chat c = Lime.getInstance().getChatFactory()
                .getChat(from.getBareJid(), stream.jid);

        if (c == null) {
            LimeLog.w("MessageDispatcher",
                    "Message dropped from unknown contact",
                    from.getJidResource());
            return BLOCK_REJECTED;
        }

        // TODO: resource magic - should switch active resource

        msg.unread = true;
        c.addMessage(msg);
        Lime.getInstance().notificationMgr()
                .showChatNotification(c.getVisavis(), body, msg.getId());

        if (m.findNamespace("request", URN_XMPP_RECEIPTS) != null) {
            XmppMessage confirmReceived = new XmppMessage(m.getFrom());
            confirmReceived.setAttribute("id", id);
            confirmReceived.addChildNs("received", URN_XMPP_RECEIPTS);

            stream.send(confirmReceived);
        }

        stream.sendBroadcast(Chat.UPDATE_CHAT, from.getBareJid());
        stream.sendBroadcast(Roster.UPDATE_CONTACT, from.getBareJid());
		
		return BLOCK_PROCESSED;
	}

	public final static int PRIORITY_MESSAGE_DISPATCHER = PRIORITY_BASIC_IM;
	
	@Override
	public int priority() {
		return PRIORITY_MESSAGE_DISPATCHER;
	}
	
	@Override
	public String capsXmlns() { return URN_XMPP_RECEIPTS; }

}
