package org.bombusim.xmpp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.bombusim.lime.logger.LimeLog;
import org.bombusim.util.strconv;
import org.bombusim.xmpp.exception.XmppException;

public class SASL_DigestMD5 extends XmppObjectListener {

	@Override
	public int blockArrived(XmppObject data, XmppStream stream)
			throws IOException, XmppException {
		
		if (data.getTagName().equals("challenge")) {
            // first stream - step 2,3. reaction to challenges
            
            String challenge = strconv.decodeBase64(data.getText());
            //System.out.println(challenge);
            
            XmppObject resp=new XmppObject("response", null, null);
            resp.setNameSpace("urn:ietf:params:xml:ns:xmpp-sasl");
            
            int nonceIndex=challenge.indexOf("nonce=");
                // first stream - step 2. generating DIGEST-MD5 response due to challenge
            if (nonceIndex>=0) {
                nonceIndex+=7;
                String nonce=challenge.substring(nonceIndex, challenge.indexOf('\"', nonceIndex));
                
                Random rnd = new Random(System.currentTimeMillis());
                String cnonce="Lime" + rnd.nextLong();
                
            	XmppJid jid = new XmppJid(stream.account.userJid);
        		String username = jid.getUser();
        		String server = jid.getServer();
        		String password = stream.account.password;
                
                resp.setText(responseMd5Digest(
                        username,
                        password,
                        server,
                        "xmpp/"+server,
                        nonce,
                        cnonce ));
                
                
                stream.send(resp);
                
                return BLOCK_PROCESSED;
            }
                // first stream - step 3. sending second empty response due to second challenge
            //if (challenge.startsWith("rspauth")) {}
            stream.send(resp);
                
            return XmppObjectListener.NO_MORE_BLOCKS;
        }
		return BLOCK_REJECTED;
	}

	public void start(XmppStream stream) {
		stream.addBlockListener(this);
		
		LimeLog.i("SASL", "Authentication: DIGEST-MD5", null);
    	
        XmppObject auth=new XmppObject("auth", null,null);
        auth.setNameSpace("urn:ietf:params:xml:ns:xmpp-sasl");
		
        auth.setAttribute("mechanism", "DIGEST-MD5");
        
        stream.send(auth);
		
	}
	
	@Override
	public int priority() { return SASLAuth.PRIORITY_SASLAUTH; }

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
        
        String out = "username=\""+user +"\",realm=\""+realm+"\"," +
                "nonce=\""+nonce+"\",nc="+nc+",cnonce=\""+cnonce+"\"," +
                "qop=auth,digest-uri=\""+digestUri+"\"," +
                "response=\""+hResp+"\",charset=utf-8";
		try {
			return strconv.toBase64(out.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
        
    }

}
