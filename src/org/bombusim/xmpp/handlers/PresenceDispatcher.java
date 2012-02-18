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
import org.bombusim.lime.data.Contact;
import org.bombusim.lime.data.Resource;
import org.bombusim.lime.data.Roster;
import org.bombusim.xmpp.XmppJid;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppObjectListener;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.exception.XmppException;
import org.bombusim.xmpp.stanza.XmppPresence;

public class PresenceDispatcher extends XmppObjectListener{

	@Override
	public int blockArrived(XmppObject data, XmppStream stream)
			throws IOException, XmppException {
		
		if (!(data instanceof XmppPresence)) return BLOCK_REJECTED;
		XmppPresence p=(XmppPresence)data;
		p.dispathch();
		
		//TODO: handle ask subscription
		
		//0. get barejid and resource
		XmppJid fromJid = new XmppJid( p.getFrom() );
		
		//TODO: handle "to" attribute
		
		//1. update contact in roster
		Contact c = Lime.getInstance().getRoster().findContact(fromJid.getBareJid(), stream.jid);
		
		if (c==null) return BLOCK_REJECTED;
		Resource r = c.setPresence(p.getTypeIndex(), fromJid.getResource(), p.getPriority(), p.getTimeStamp());
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
		stream.sendBroadcast(Roster.UPDATE_CONTACT, fromJid.getBareJid());

		return BLOCK_PROCESSED;
	}
}
