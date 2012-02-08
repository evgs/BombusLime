package org.bombusim.xmpp.handlers;

import java.io.IOException;

import org.bombusim.xmpp.XmppError;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppObjectListener;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.exception.XmppException;
import org.bombusim.xmpp.stanza.Iq;

public class IqFallback extends XmppObjectListener {

	@Override
	public int blockArrived(XmppObject data, XmppStream stream)
			throws IOException, XmppException {

		try {
			Iq f = (Iq)data;

			String from = f.getAttribute("from");
			
			String type = f.getTypeAttribute();
			
			if (type.equals("result")) { return BLOCK_REJECTED; }
			if (type.equals("error"))  { return BLOCK_REJECTED; }
				
			XmppError err = new XmppError(XmppError.FEATURE_NOT_IMPLEMENTED, null);

			f.setAttribute("to", from);
			f.setAttribute("from", null);
			f.setAttribute("type", "error");
			
			f.addChild(err.construct());

			stream.send(f);
		} catch (Exception e) {};
		
		return BLOCK_PROCESSED;
	}

	@Override
	public int priority() { return PRIORITY_IQUNKNOWN_FALLBACK; }
}
