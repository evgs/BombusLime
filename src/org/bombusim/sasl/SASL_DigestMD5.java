package org.bombusim.sasl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.bombusim.lime.logger.LimeLog;
import org.bombusim.util.strconv;
import org.bombusim.xmpp.SASLAuth;
import org.bombusim.xmpp.XmppJid;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppObjectListener;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.exception.XmppException;

public class SASL_DigestMD5 implements SaslAuthMechanism {

	private String password;
	private XmppJid jid;

	@Override
	public String getName() { return "DIGEST-MD5"; }

	@Override
	public boolean isSecure() { return true; }

	@Override
	public String init(XmppJid jid, String password) {
		LimeLog.i("SASL", "Authentication: DIGEST-MD5", null);

		this.jid = jid;
		this.password = password;
		return "";
	}

	@Override
	public String response(String challenge) {
		
        int nonceIndex=challenge.indexOf("nonce=");
        // first stream - step 2. generating DIGEST-MD5 response due to challenge
        if (nonceIndex>=0) {
	        nonceIndex+=7;
	        String nonce=challenge.substring(nonceIndex, challenge.indexOf('\"', nonceIndex));
	        
	        Random rnd = new Random(System.currentTimeMillis());
	        String cnonce="Lime" + rnd.nextLong();
	        
			String username = jid.getUser();
			String server = jid.getServer();
	        
	        return responseMd5Digest(
	                username,
	                password,
	                server,
	                "xmpp/"+server,
	                nonce,
	                cnonce );
	    }
        
        //TODO: verify server identity in 2nd challenge
        
	    return "";
	}

	@Override
	public boolean success(String success) { return true; }



    /**
     * This routine generates MD5-DIGEST response via SASL specification
     * @param user
     * @param pass
     * @param realm
     * @param digest_uri
     * @param nonce
     * @param cnonce
     * @return
     */
    private String responseMd5Digest(String user, String pass, String realm, String digestUri, String nonce, String cnonce) {

    	MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) { e.printStackTrace(); return null; }
		
        try {
        	
			md5.update(user.getBytes("UTF-8"));
	        md5.update((byte)':');
	        md5.update(realm.getBytes("UTF-8"));
	        md5.update((byte)':');
	        md5.update(pass.getBytes("UTF-8"));

        } catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
        
        byte[] hUserRealmPass = md5.digest();
        
        md5.update(hUserRealmPass);
        md5.update((byte)':');
        md5.update(nonce.getBytes());
        md5.update((byte)':');
        md5.update(cnonce.getBytes());
        String hA1 = strconv.byteArrayToHexString( md5.digest() );
        
        md5.update("AUTHENTICATE:".getBytes());
        md5.update(digestUri.getBytes());
        String hA2 = strconv.byteArrayToHexString( md5.digest() );
        
        String nc="00000001";
        
        md5.update(hA1.getBytes());
        md5.update((byte)':');
        md5.update(nonce.getBytes());
        md5.update((byte)':');
        md5.update(nc.getBytes());
        md5.update((byte)':');
        md5.update(cnonce.getBytes());
        md5.update(":auth:".getBytes());
        md5.update(hA2.getBytes());
        String hResp = strconv.byteArrayToHexString( md5.digest() );
        
        return "username=\""+user +"\",realm=\""+realm+"\"," +
                "nonce=\""+nonce+"\",nc="+nc+",cnonce=\""+cnonce+"\"," +
                "qop=auth,digest-uri=\""+digestUri+"\"," +
                "response=\""+hResp+"\",charset=utf-8";
        
    }

}
