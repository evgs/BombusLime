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
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.bombusim.xmpp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.bombusim.lime.logger.LimeLog;
import org.bombusim.util.strconv;
import org.bombusim.xmpp.exception.XmppAuthException;
import org.bombusim.xmpp.exception.XmppException;
import org.bombusim.xmpp.stanza.Iq;

/**
 *
 * @author evgs
 */
public class SASLAuth extends XmppObjectListener{
    
    /** Creates a new instance of SASLAuth */
    //public SASLAuth(Account account, LoginListener listener, JabberStream stream) {
	
    public SASLAuth() {  }
    
    public int blockArrived(XmppObject data, XmppStream stream) throws XmppException, IOException {
        //System.out.println(data.toString());
        if (data.getTagName().equals("stream:features")) {

        	XmppObject mech=data.getChildBlock("mechanisms");
            if (mech!=null) {
                // first stream - step 1. selecting authentication mechanism
                
                //trying secure authentication if enabled
                if (stream.account.enablePlainAuth != XmppAccount.PLAIN_AUTH_ALWAYS)  {
                	// SCRAM-SHA-1 mechanism
                	if (mech.getChildBlockByText("SCRAM-SHA-1")!=null) {
                		new SASL_ScramSha1().start(stream);
                		
	                    return XmppObjectListener.BLOCK_PROCESSED;
	                }

                	// DIGEST-MD5 mechanism
                	if (mech.getChildBlockByText("DIGEST-MD5")!=null) {
                		new SASL_DigestMD5().start(stream);
                		
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
                	
                	//in normal case PLAIN auth may be secured only over SSL/TLS connection
                	
                	if (!stream.isSecured() && stream.account.enablePlainAuth != XmppAccount.PLAIN_AUTH_ALWAYS) {
                		throw new XmppAuthException("Plain authentication disabled over non-secured connection");
                	}
                	if (stream.account.enablePlainAuth == XmppAccount.PLAIN_AUTH_NEVER) {
                		throw new XmppAuthException("Plain authentication disabled");
                	}


            		if (stream.isSecured()) {
            			LimeLog.i("SASL", "PLAIN auth over secured stream", null);
            		} else {
            			LimeLog.w("SASL", "UNSECURE PLAIN auth", null);
            		}
                	
                	XmppJid jid = new XmppJid(stream.account.userJid);
            		String bareJid = jid.getBareJid();
            		String username = jid.getUser();
            		String password = stream.account.password;
            		
                    XmppObject auth=new XmppObject("auth", null,null);
                    auth.setNameSpace("urn:ietf:params:xml:ns:xmpp-sasl");
            		
                    auth.setAttribute("mechanism", "PLAIN");
                    String plain=
                            bareJid + (char)0x00 + username + (char)0x00 + password;
                    
                    try {
                    	auth.setText(strconv.toBase64(plain.getBytes("UTF-8")));
                    } catch (UnsupportedEncodingException e) {
                    	e.printStackTrace();
                    	return BLOCK_REJECTED;
                    }
                    
                    stream.send(auth);
                    //listener.loginMessage(SR.MS_AUTH);
                    return XmppObjectListener.BLOCK_PROCESSED;
                }
                // no more method found
                return BLOCK_REJECTED;
                
            } //SASL mechanisms
            
            // second stream - step 1. binding resource
            else if (data.getChildBlock("bind")!=null) {
        		LimeLog.i("XMPP", "Binding resource", null);
            	
                XmppObject bindIq=new Iq(null, Iq.TYPE_SET, "bind");
                XmppObject bind=bindIq.addChildNs("bind", "urn:ietf:params:xml:ns:xmpp-bind");
                bind.addChild("resource", stream.account.resource);
                stream.send(bindIq);

                //listener.loginMessage(SR.MS_RESOURCE_BINDING);
                
                return XmppObjectListener.BLOCK_PROCESSED;
            }
            
            return BLOCK_REJECTED;
            
        } else if ( data.getTagName().equals("failure")) {
            // first stream - step 4a. not authorized
        	throw new XmppAuthException(XmppError.decodeSaslError(data).toString());
        	
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

                	stream.jidSession = data.getChildBlock("bind").getChildBlockText("jid");
                    
            		LimeLog.i("XMPP", "Starting session", null);
                    
                    XmppObject session=new Iq(null, Iq.TYPE_SET, "sess");
                    session.addChildNs("session", "urn:ietf:params:xml:ns:xmpp-session");
                    stream.send(session);
                    //listener.loginMessage(SR.MS_SESSION);
                    return XmppObjectListener.BLOCK_PROCESSED;
                    
                // second stream - step 3. session opened - reporting success login
                } else if (data.getAttribute("id").equals("sess")) {

                	LimeLog.i("XMPP", "Signed in successfully", null);
                	
                	stream.loginSuccess();
                	
                    return XmppObjectListener.NO_MORE_BLOCKS;
                    //return JabberBlockListener.BLOCK_PROCESSED;
                }
            }
        }
        return XmppObjectListener.BLOCK_REJECTED;
    }
    


	public final static int PRIORITY_SASLAUTH = PRIORITY_AUTH;
	@Override
	public int priority() { return PRIORITY_SASLAUTH; }

}
