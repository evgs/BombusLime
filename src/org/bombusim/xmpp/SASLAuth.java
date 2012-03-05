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
import org.bombusim.sasl.SASL_DigestMD5;
import org.bombusim.sasl.SASL_Plain;
import org.bombusim.sasl.SASL_ScramSha1;
import org.bombusim.sasl.SaslAuthMechanism;
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
	
    public SASLAuth() { }
    
    private SaslAuthMechanism[] saslMechanisms = {
    		new SASL_ScramSha1(),
    		new SASL_DigestMD5(),
    		new SASL_Plain()
    };
    
    private SaslAuthMechanism selectedMechanism;

    boolean needSessionEstablish;
    
    public int blockArrived(XmppObject data, XmppStream stream) throws XmppException, IOException {
        //System.out.println(data.toString());
        if (data.getTagName().equals("stream:features")) {

        	XmppObject mech=data.getChildBlock("mechanisms");
            if (mech!=null) {
                // first stream - step 1. selecting authentication mechanism
                
            	for (SaslAuthMechanism m : saslMechanisms) {
            		if (mech.getChildBlockByText(m.getName()) != null) {
            			
            			if (m.isSecure()) {
            				//check if secure auth is not disabled
            				if (stream.account.enablePlainAuth == XmppAccount.PLAIN_AUTH_ALWAYS) continue;
            			} else {
            				//check if we can use unsecured auth
                        	if (stream.account.enablePlainAuth == XmppAccount.PLAIN_AUTH_NEVER) {
                        		throw new XmppAuthException("Plain authentication disabled");
                        	}

                        	if (!stream.isSecured()) {
            					if (stream.account.enablePlainAuth != XmppAccount.PLAIN_AUTH_ALWAYS) {
            						throw new XmppAuthException("Plain authentication disabled over non-secured connection");
            					} else {
                        			LimeLog.w("SASL", "UNSECURE PLAIN auth", null);
            					}
                        	}
            			}
            			
            			selectedMechanism = m;
            			
            			String authText = selectedMechanism.init(new XmppJid(stream.account.userJid), stream.account.password);
            			
                        XmppObject auth=new XmppObject("auth", null,null);
                        auth.setNameSpace("urn:ietf:params:xml:ns:xmpp-sasl");
                		
                        auth.setAttribute("mechanism", selectedMechanism.getName());

                       	auth.setText(strconv.toBase64(authText.getBytes())); // TODO: (Android-default UTF8)
                        
                        stream.send(auth);
                        //listener.loginMessage(SR.MS_AUTH);
                        return XmppObjectListener.BLOCK_PROCESSED;
            		}
            	} //for methods
            	
            	throw new XmppAuthException("No known SASL auth mechanisms found");
            	
            } //mech
            // second stream - step 1. binding resource
            else if (data.getChildBlock("bind")!=null) {
        		LimeLog.i("XMPP", "Binding resource", null);
            	
                XmppObject bindIq=new Iq(null, Iq.TYPE_SET, "bind");
                XmppObject bind=bindIq.addChildNs("bind", "urn:ietf:params:xml:ns:xmpp-bind");
                bind.addChild("resource", stream.account.resource);
                stream.send(bindIq);

                needSessionEstablish = (data.getChildBlock("session") != null);

                //listener.loginMessage(SR.MS_RESOURCE_BINDING);
                
                return XmppObjectListener.BLOCK_PROCESSED;
            }
            
            return BLOCK_REJECTED;
            
        } else if (data.getTagName().equals("challenge")) {
        	
            String challenge = strconv.decodeBase64(data.getText());
        	
            String response = selectedMechanism.response(challenge);

            XmppObject resp=new XmppObject("response", null, null);
            resp.setNameSpace("urn:ietf:params:xml:ns:xmpp-sasl");
 
            resp.setText(strconv.toBase64(response.getBytes()));

            stream.send(resp);
            
            return BLOCK_PROCESSED;
            
        } else if ( data.getTagName().equals("failure")) {
            // first stream - step 4a. not authorized
        	throw new XmppAuthException(XmppError.decodeSaslError(data).toString());
        	
        } else if ( data.getTagName().equals("success")) {
        	String serverResponse = strconv.decodeBase64(data.getText());
        	
        	if (!selectedMechanism.success(serverResponse)) 
        		throw new XmppAuthException("Can not verify server identity proof");
        	
        	// okay, we are logged in, so removing AuthFallback
        	stream.cancelBlockListenerByClass(AuthFallback.class);
        	
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
                    
                    if (!needSessionEstablish) {

                	LimeLog.i("XMPP", "Signed in successfully", null);
                	
                	stream.loginSuccess();
                	
                        return XmppObjectListener.NO_MORE_BLOCKS;
                    }

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
