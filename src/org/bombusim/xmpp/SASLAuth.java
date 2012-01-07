/*
 * SASLAuth.java
 *
 * Created on 8.06.2006, 23:34
 *
 * Copyright (c) 2005-2011, Eugene Stahov (evgs@bombus-im.org), 
 * http://bombus-im.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * You can also redistribute and/or modify this program under the
 * terms of the Psi License, specified in the accompanied COPYING
 * file, as published by the Psi Project; either dated January 1st,
 * 2005, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.bombusim.xmpp;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.bombusim.util.strconv;
import org.bombusim.xmpp.stanza.Iq;


import android.util.Log;


/**
 *
 * @author evgs
 */
public class SASLAuth implements XmppObjectListener{
    
    /** Creates a new instance of SASLAuth */
    //public SASLAuth(Account account, LoginListener listener, JabberStream stream) {
	
    public SASLAuth() {  }
    
    public int blockArrived(XmppObject data, XmppStream stream) throws XmppException, IOException {
        //System.out.println(data.toString());
        if (data.getTagName().equals("stream:features")) {

        	//SSL/TLS
        	if (!stream.isSecured()) {
        		
            	int requiredSecurity=stream.account.secureConnection;

            	if (requiredSecurity >= XmppAccount.SECURE_CONNECTION_IF_AVAILABLE) {
	        		XmppObject starttls=data.getChildBlock("starttls");
	        		if (starttls!=null) { 
	        			if (starttls.compareNameSpace("urn:ietf:params:xml:ns:xmpp-tls")) {
		                    // negotiating starttls
	        				stream.send(starttls);
	        				return XmppObjectListener.BLOCK_PROCESSED;
	        			}
	        		} else {
	        			if (requiredSecurity == XmppAccount.SECURE_CONNECTION_ALWAYS) 
	        				throw new XmppException("Server doesn't provide secure TLS connection (REQUIRED)");
	        		}
	        	}
        	}

        	if (stream.account.trafficCompression) {
	        	XmppObject compr=data.getChildBlock("compression");
	            if (compr!=null) { 
	                if (compr.getChildBlockByText("zlib")!=null) {
	                    // negotiating compression
	                    XmppObject askCompr=new XmppObject("compress", null, null);
	                    askCompr.setNameSpace("http://jabber.org/protocol/compress");
	                    askCompr.addChild("method", "zlib");
	                    stream.send(askCompr);
	                    
	                    //TODO: listener.loginMessage(SR.MS_ZLIB);
	                    return XmppObjectListener.BLOCK_PROCESSED;
	                }
	            }
        	}

        	XmppObject mech=data.getChildBlock("mechanisms");
            if (mech!=null) {
                // first stream - step 1. selecting authentication mechanism
                //common body
                XmppObject auth=new XmppObject("auth", null,null);
                auth.setNameSpace("urn:ietf:params:xml:ns:xmpp-sasl");
                
                if (stream.account.enablePlainAuth != XmppAccount.PLAIN_AUTH_NEVER) {
                		// DIGEST-MD5 mechanism
	                	if (mech.getChildBlockByText("DIGEST-MD5")!=null) {
	                	
	            		Log.i("SASL", "Authentication: DIGEST-MD5");
	                	
	                    auth.setAttribute("mechanism", "DIGEST-MD5");
	                    
	                    //System.out.println(auth.toString());
	                    
	                    stream.send(auth);
	                    return XmppObjectListener.BLOCK_PROCESSED;
	                }
                }
                
//#if SASL_XGOOGLETOKEN
                // X-GOOGLE-TOKEN mechanism
/*                if (mech.getChildBlockByText("X-GOOGLE-TOKEN")!=null  && token!=null) {
                    auth.setAttribute("mechanism", "X-GOOGLE-TOKEN");
                    auth.setText(token);
                    
                    //System.out.println(auth.toString());
                    
                    stream.send(auth);
                    listener.loginMessage(SR.MS_AUTH);
                    return JabberBlockListener.BLOCK_PROCESSED;
                    
                }
//#endif
 * 
 */

                if (mech.getChildBlockByText("PLAIN")!=null) {
                	
                	if (!stream.isSecured() && stream.account.enablePlainAuth != XmppAccount.PLAIN_AUTH_ALWAYS) {
                		throw new XmppException("Plain authentication disabled over non-secured connection");
                	}
                	if (stream.account.enablePlainAuth == XmppAccount.PLAIN_AUTH_NEVER) {
                		throw new XmppException("Plain authentication disabled");
                	}


            		Log.w("SASL", "PLAIN unsecure auth");
                	
                	XmppJid jid = new XmppJid(stream.account.userJid);
            		String bareJid = jid.getBareJid();
            		String username = jid.getUser();
            		String password = stream.account.password;
            		
                    auth.setAttribute("mechanism", "PLAIN");
                    String plain=
                            strconv.unicodeToUTF(bareJid)
                            +(char)0x00
                            +strconv.unicodeToUTF(username)
                            +(char)0x00
                            +strconv.unicodeToUTF(password);
                    auth.setText(strconv.toBase64(plain));
                    
                    stream.send(auth);
                    //listener.loginMessage(SR.MS_AUTH);
                    return XmppObjectListener.BLOCK_PROCESSED;
                }
                // no more method found
                throw new XmppException("No known authentication methods provided by server");
                
            } //SASL mechanisms
            
            // second stream - step 1. binding resource
            else if (data.getChildBlock("bind")!=null) {
        		Log.i("XMPP", "Binding resource");
            	
                XmppObject bindIq=new Iq(null, Iq.TYPE_SET, "bind");
                XmppObject bind=bindIq.addChildNs("bind", "urn:ietf:params:xml:ns:xmpp-bind");
                bind.addChild("resource", stream.account.resource);
                stream.send(bindIq);

                //listener.loginMessage(SR.MS_RESOURCE_BINDING);
                
                return XmppObjectListener.BLOCK_PROCESSED;
            }
            
//#ifdef NON_SASL_AUTH
            if (data.findNamespace("auth", "http://jabber.org/features/iq-auth")!=null) {
            	NonSASLAuth nsa = new NonSASLAuth();
                stream.addBlockListener(nsa);
                nsa.jabberIqAuth(NonSASLAuth.AUTH_GET, stream);
                return XmppObjectListener.NO_MORE_BLOCKS;
            }
//#endif            
            
            //fallback if no known authentication methods were found
            throw new XmppException("No known authentication methods provided");
            
        } else if (data.getTagName().equals("challenge")) {
            // first stream - step 2,3. reaction to challenges
            
            String challenge=decodeBase64(data.getText());
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
                        strconv.unicodeToUTF(username),
                        strconv.unicodeToUTF(password),
                        server,
                        "xmpp/"+server,
                        nonce,
                        cnonce ));
            }
                // first stream - step 3. sending second empty response due to second challenge
            //if (challenge.startsWith("rspauth")) {}
                
            stream.send(resp);
            return XmppObjectListener.BLOCK_PROCESSED;
        }
        

        else if ( data.getTagName().equals("proceed")) {
            stream.setTLS();
            stream.initiateStream();
            return XmppObjectListener.BLOCK_PROCESSED;
        }       
        
        else if ( data.getTagName().equals("compressed")) {
            stream.setZlibCompression();
            stream.initiateStream();
            return XmppObjectListener.BLOCK_PROCESSED;
        }
        
            
        else if ( data.getTagName().equals("failure")) {
            // first stream - step 4a. not authorized
        	throw new XmppException(XmppError.decodeSaslError(data).toString());
        	
        } else if ( data.getTagName().equals("success")) {
            // first stream - step 4b. success.
            try {
                stream.initiateStream();
            } catch (IOException ex) { }
            return XmppObjectListener.BLOCK_PROCESSED; // at first stream
        }

        if (data instanceof Iq) {
            if (data.getTypeAttribute().equals("result")) {
                // second stream - step 2. resource binded - opening session
                if (data.getAttribute("id").equals("bind")) {

                	stream.jid = data.getChildBlock("bind").getChildBlockText("jid");
                    
            		Log.i("XMPP", "Starting session");
                    
                    XmppObject session=new Iq(null, Iq.TYPE_SET, "sess");
                    session.addChildNs("session", "urn:ietf:params:xml:ns:xmpp-session");
                    stream.send(session);
                    //listener.loginMessage(SR.MS_SESSION);
                    return XmppObjectListener.BLOCK_PROCESSED;
                    
                // second stream - step 3. session opened - reporting success login
                } else if (data.getAttribute("id").equals("sess")) {

                	Log.i("XMPP", "Signed in successfully");
                	
                	stream.loginSuccess();
                	
                    return XmppObjectListener.NO_MORE_BLOCKS;
                    //return JabberBlockListener.BLOCK_PROCESSED;
                }
            }
        }
        return XmppObjectListener.BLOCK_REJECTED;
    }
    
    private String decodeBase64(String src)  {
        int len=0;
        int ibuf=1;
        StringBuilder out=new StringBuilder();
        
        for (int i=0; i<src.length(); i++) {
            int nextChar = src.charAt(i);
            int base64=-1;
            if (nextChar>'A'-1 && nextChar<'Z'+1) base64=nextChar-'A';
            else if (nextChar>'a'-1 && nextChar<'z'+1) base64=nextChar+26-'a';
            else if (nextChar>'0'-1 && nextChar<'9'+1) base64=nextChar+52-'0';
            else if (nextChar=='+') base64=62;
            else if (nextChar=='/') base64=63;
            else if (nextChar=='=') {base64=0; len++;} else if (nextChar=='<') break;
            if (base64>=0) ibuf=(ibuf<<6)+base64;
            if (ibuf>=0x01000000){
                out.append( (char)((ibuf>>16) &0xff) );
                if (len<2) out.append( (char)((ibuf>>8) &0xff) );
                if (len==0) out.append( (char)(ibuf &0xff) );
                //len+=3;
                ibuf=1;
            }
        }
        return out.toString();
    }

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
		
        md5.update(user.getBytes());
        md5.update((byte)':');
        md5.update(realm.getBytes());
        md5.update((byte)':');
        md5.update(pass.getBytes());
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
        String resp = strconv.toBase64(out);
        //System.out.println(decodeBase64(resp));
        
        return resp;
    }

	@Override
	public String capsXmlns() {	return null; }
    
}
