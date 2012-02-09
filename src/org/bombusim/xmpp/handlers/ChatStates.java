/*	XEP-0085 Chat states notifications
 * 
 */

package org.bombusim.xmpp.handlers;

import java.io.IOException;

import org.bombusim.lime.Lime;
import org.bombusim.lime.data.Chat;
import org.bombusim.lime.data.Roster;
import org.bombusim.xmpp.XmppJid;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppObjectListener;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.exception.XmppException;
import org.bombusim.xmpp.stanza.XmppMessage;

public class ChatStates extends XmppObjectListener{

	public static final String XMLNS_CHATSTATES = "http://jabber.org/protocol/chatstates";
	
	public final static String ACTIVE    = "active";
	public final static String COMPOSING = "composing";
	public final static String PAUSED    = "paused";
	public final static String INACTIVE  = "inactive";
	public final static String GONE      = "gone";

	@Override
	public int blockArrived(XmppObject data, XmppStream stream)
			throws IOException, XmppException {
		try {
			XmppMessage m = (XmppMessage) data;  //throws if data is not message stanza
			
			XmppObject chatStates = m.findNamespace(null, XMLNS_CHATSTATES);
			
			String state = chatStates.getTagName(); //throws if no chat state found in message stanza
			
			XmppJid from = new XmppJid( m.getFrom() );
			
			Chat c = Lime.getInstance().getChatFactory().getChat(from.getBareJid(), stream.jid);
			
			//updating chat state
			c.setChatState(state);
			
			//sending broadcast to update comosing event icon in chat and in roster
			stream.sendBroadcast(Roster.UPDATE_CONTACT, from.getBareJid());

			return BLOCK_REJECTED; //in any case passing this stanza forward by dispatching queue
		} catch (Exception e) {}
		return BLOCK_REJECTED;
	}
	
	public final static int PRIORITY_CHATSTATES = MessageDispatcher.PRIORITY_MESSAGE_DISPATCHER - 10;
	
	@Override
	public int priority() {
		return PRIORITY_CHATSTATES;
	}
	
	@Override
	public String capsXmlns() {
		return XMLNS_CHATSTATES;
	}
}
