package org.bombusim.sasl;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.bombusim.lime.logger.LimeLog;
import org.bombusim.util.strconv;
import org.bombusim.xmpp.XmppJid;

import android.util.Log;

public class SASL_ScramSha1 implements SaslAuthMethod {

	private XmppJid jid;
	String pass;
	String cnonce;
	String clientFirstMessageBare;

	Mac hmac;
	

	
	@Override
	public String getMethodName() { return "SCRAM-SHA-1"; }

	@Override
	public String init(XmppJid jid, String password) {
		this.jid = jid;
		this.pass = password;
		
		LimeLog.i("SASL", "Authentication: SCRAM-SHA-1", null);

        calculateClientFirstMessage();
        
        return "n,,"+clientFirstMessageBare;
		
	}

	@Override
	public String response(String challenge) {
        String serverFirstMessage = challenge;
        
        String clientFinalMessage = processServerMessage(serverFirstMessage);
        
        return clientFinalMessage;
	}

	@Override
	public boolean success(String success) {
		//TODO: verify server response
		return true;
	}
	
	@Override
	public boolean isSecure() { return true; }
	
	public SASL_ScramSha1() {
		try {
			hmac = Mac.getInstance("HmacSHA1");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void xorB(byte[] dest, byte[] source) {
		int l = dest.length;

		for (int i = 0; i < l; i++) {
			dest[i] ^= source[i];
		}
	}

	private String getAttribute(String[] attrs, char id) {
		for (String s: attrs) {
			if (s.charAt(0) == id) return s.substring(2);
		}
		
		return null;
	}
	
	private String processServerMessage(String serverFirstMessage) {
		String[] attrs = serverFirstMessage.split(",");
		
		int i=Integer.parseInt( getAttribute(attrs, 'i') );
		String salt=getAttribute(attrs, 's');
		String r=getAttribute(attrs, 'r');
		
		byte[] pwd;
		
		try {
			pwd = pass.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		
		byte[] saltedPassword = Hi(pwd, strconv.fromBase64(salt), i);
	
		byte[] clientKey = getHMAC(saltedPassword).doFinal( "Client Key".getBytes() );
		
		MessageDigest sha;
		
		try {
			sha = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) { return null; }
		
		byte[] storedKey = sha.digest(clientKey);
	
		String clientFinalMessageWithoutProof = "c=biws,r="+r;
		
		String authMessage = clientFirstMessageBare + "," 
		                   + serverFirstMessage + "," 
				           + clientFinalMessageWithoutProof;
		
		byte[] clientSignature = getHMAC(storedKey).doFinal( authMessage.getBytes() );
		
		byte[] clientProof = clientKey.clone();
		xorB(clientProof, clientSignature);
		
		return clientFinalMessageWithoutProof + ",p=" + strconv.toBase64(clientProof);
		
	}

	protected void calculateClientFirstMessage() {
		Random rnd = new Random(System.currentTimeMillis());
        cnonce="Lime" + rnd.nextLong();

		String username = jid.getUser();
       
        clientFirstMessageBare = "n="+username + ",r=" + cnonce;
	}
	
	
	private byte[] Hi(byte[] str, byte[] salt, int i)  {
		byte[] dest;
		
		Mac hmac = getHMAC(str);
		
		hmac.update(salt);

		//INT(1), MSB first
		hmac.update((byte)0);
		hmac.update((byte)0);
		hmac.update((byte)0);
		hmac.update((byte)1);
		
		byte[] U = hmac.doFinal();
		
		dest = U.clone();
		
		i--;
		
		while (i>0) {
			U = hmac.doFinal(U);
			xorB(dest, U);
			i--;
		}

		return dest;
	}

	private Mac getHMAC(byte[] str) {
		try {
			SecretKeySpec secret = new SecretKeySpec(str, "HmacSHA1");
			
			hmac.init(secret);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hmac;
	}
	
	
	public void test() {
		pass="pencil";
		cnonce = "fyko+d2lbbFgONRv9qkxdawL";
		clientFirstMessageBare="n=user,r=fyko+d2lbbFgONRv9qkxdawL";
		String clientFinalMessage = processServerMessage("r=fyko+d2lbbFgONRv9qkxdawL3rfcNHYJY1ZVvWVs7j,s=QSXCR+Q6sek8bf92,i=4096");
		Log.d("SCRAM-SHA1", clientFinalMessage);
	}

}
