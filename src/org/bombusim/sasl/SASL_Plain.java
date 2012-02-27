package org.bombusim.sasl;

import java.io.UnsupportedEncodingException;

import org.bombusim.lime.logger.LimeLog;
import org.bombusim.util.strconv;
import org.bombusim.xmpp.XmppJid;
import org.bombusim.xmpp.XmppObject;

public class SASL_Plain implements SaslAuthMethod {

	@Override
	public String getMethodName() { return "PLAIN"; }

	@Override
	public String init(XmppJid jid, String password) {
		LimeLog.i("SASL", "Authentication: PLAIN", null);
		
		String bareJid = jid.getBareJid();
		String username = jid.getUser();
		
        String plain=
                bareJid + (char)0x00 + username + (char)0x00 + password;
        return plain;
	}

	@Override
	public String response(String challenge) { 	return ""; }

	@Override
	public boolean success(String success) { return true; }

	@Override
	public boolean isSecure() { return false; } //PLAIN

}
