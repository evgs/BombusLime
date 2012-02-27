package org.bombusim.sasl;

import org.bombusim.xmpp.XmppJid;

public interface SaslAuthMechanism {
	public String getName();
	public boolean isSecure();
	public String init(XmppJid jid, String password);
	public String response(String challenge);
	public boolean success(String success);
}
