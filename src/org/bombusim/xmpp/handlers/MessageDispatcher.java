package org.bombusim.xmpp.handlers;

import java.io.IOException;

import org.bombusim.lime.Lime;
import org.bombusim.lime.data.Chat;
import org.bombusim.lime.data.ChatFactory;
import org.bombusim.lime.data.Message;
import org.bombusim.xmpp.XmppJid;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppObjectListener;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.exception.XmppException;
import org.bombusim.xmpp.stanza.XmppMessage;

public class MessageDispatcher implements XmppObjectListener{

	@Override
	public int blockArrived(XmppObject data, XmppStream stream)
			throws IOException, XmppException {
		if (!(data instanceof XmppMessage)) return BLOCK_REJECTED;
		
		XmppMessage m = (XmppMessage) data;
		XmppJid from = new XmppJid( m.getFrom() );

		Message msg = new Message(Message.TYPE_MESSAGE_IN, from.getJidResource(), m.getBody());
		msg.subj = m.getSubject();
		//TODO: timestamp
		
		//TODO: resource magic
		try {
			Chat c = Lime.getInstance().getChatFactory().getChat(from.getBareJid(), stream.jid); 
			c.addMessage(msg);
			Lime.getInstance().notificationMgr().showChatNotification(c.getVisavis(), m.getBody());
		} catch (Exception e) {
			e.printStackTrace(); //Will handle only NPE
		}
		
		
		
		return BLOCK_REJECTED;
	}

	@Override
	public String capsXmlns() { return null; }

}
