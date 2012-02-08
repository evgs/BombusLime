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
import org.bombusim.xmpp.stanza.Iq;

public class IqVcard extends XmppObjectListener {

	@Override
	public int blockArrived(XmppObject data, XmppStream stream)
			throws IOException, XmppException {
		
		if (!(data instanceof Iq)) return BLOCK_REJECTED;
		
		if (data.getTypeAttribute().equals("error")) return BLOCK_REJECTED;
		
		XmppObject vcard = data.findNamespace("vCard", "vcard-temp");
		
		if (vcard == null) return BLOCK_REJECTED;
		
		String from = data.getAttribute("from");
		
		Vcard result = new Vcard(from, vcard);
		
		Lime.getInstance().getRoster().notifyVcard(result);
		
		stream.sendBroadcast(Roster.UPDATE_ROSTER, new XmppJid(from).getBareJid());
		
		return NO_MORE_BLOCKS;
	}

	@Override
	public String capsXmlns() { return null; }
	
	
	public void vcardRequest(String jid, XmppStream stream) {
		Iq request = new Iq(jid, Iq.TYPE_GET, "getVc");
		request.addChildNs("vCard", "vcard-temp");
		
		stream.addBlockListener(this);
		stream.send(request);
	}

}
