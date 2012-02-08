package org.bombusim.xmpp.handlers;

import java.io.IOException;

import org.bombusim.lime.Lime;
import org.bombusim.lime.data.Contact;
import org.bombusim.lime.data.Resource;
import org.bombusim.lime.data.Roster;
import org.bombusim.xmpp.XmppJid;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppObjectListener;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.exception.XmppException;
import org.bombusim.xmpp.stanza.Presence;

public class PresenceDispatcher extends XmppObjectListener{

	@Override
	public int blockArrived(XmppObject data, XmppStream stream)
			throws IOException, XmppException {
		
		if (!(data instanceof Presence)) return BLOCK_REJECTED;
		Presence p=(Presence)data;
		p.dispathch();
		
		//TODO: handle ask subscription
		
		//0. get barejid and resource
		XmppJid fromJid = new XmppJid( p.getFrom() );
		
		//TODO: handle "to" attribute
		
		//1. update contact in roster
		Contact c = Lime.getInstance().getRoster().findContact(fromJid.getBareJid(), stream.jid);
		
		if (c==null) return BLOCK_REJECTED;
		Resource r = c.setPresence(p.getTypeIndex(), fromJid.getResource(), p.getPriority());
		r.statusMessage = p.getChildBlockText("status");
		
		//2. update avatar if available
		XmppObject x = data.findNamespace("x", "vcard-temp:x:update");
		if (x!=null) {
			String avatarId = x.getChildBlockText("photo");
			if (avatarId != null) {
				c.updateAvatarHash(avatarId);
				c.getLazyAvatar(false);		//trigger avatar update
			}
		}
		
		//3. save presence to chat
		//TODO: save to active chat
		
		//at last: repaint roster
		//TODO: broadcast contact id to avoid unnececary roster repaints
		stream.sendBroadcast(Roster.UPDATE_ROSTER, fromJid.getBareJid());

		return BLOCK_PROCESSED;
	}
}
