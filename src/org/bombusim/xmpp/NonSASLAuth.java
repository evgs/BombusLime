/*
 * NonSASLAuth.java
 *
 * Created on 8.06.2006, 22:16
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bombusim.util.strconv;
import org.bombusim.xmpp.stanza.Iq;



/**
 *
 * @author evgs
 */
public class NonSASLAuth implements XmppObjectListener{
    
	
    public NonSASLAuth() {
    }

    
    final static int AUTH_GET=0;
    final static int AUTH_PASSWORD=1;
    final static int AUTH_DIGEST=2;
    
    void jabberIqAuth(int authType, XmppStream stream) {
        int type=Iq.TYPE_GET;
        String id="auth-1";
        
        XmppObject query = new XmppObject("query", null, null);
        query.setNameSpace( "jabber:iq:auth" );
        
        XmppJid jid = new XmppJid(stream.account.userJid);
        String username = jid.getUser();
        String server = jid.getServer();
        String password = stream.account.password;
        String resource = stream.account.resource;
                
        query.addChild( "username", username );
        
        switch (authType) {
            case AUTH_DIGEST:
			MessageDigest sha1;
			
				try {
					sha1 = MessageDigest.getInstance("SHA-1");
				} catch (NoSuchAlgorithmException e) { e.printStackTrace(); return; }
				
                sha1.update(stream.getSessionId().getBytes());
                sha1.update((strconv.unicodeToUTF(password)).getBytes() );
                String digestHex = strconv.byteArrayToHexString(sha1.digest());
                query.addChild("digest", digestHex );

                query.addChild( "resource", resource );
                type=Iq.TYPE_SET;
                id="auth-s";
                break;
                
            case AUTH_PASSWORD:
                query.addChild("password", password );
                query.addChild( "resource", resource );
                type=Iq.TYPE_SET;
                id="auth-s";
                break;
        }

        
        Iq auth=new Iq(server, type, id);
        auth.addChild(query);
        
        stream.send(auth);
    }
    
	@Override
	public int blockArrived(XmppObject data, XmppStream stream) {
        try {
            if( data instanceof Iq ) {
                String type = (String) data.getTypeAttribute();
                String id=(String) data.getAttribute("id");
                if ( id.equals("auth-s") ) {
                    if (type.equals( "error" )) {
                        // Authorization error
                    	throw new XmppException(XmppError.findInStanza(data).toString());

                    } else if (type.equals( "result")) {
                    
                    	stream.loginSuccess();
                    	
                        return XmppObjectListener.NO_MORE_BLOCKS;
                    }
                }
                if (id.equals("auth-1")) {
                    try {
                        XmppObject query=data.getChildBlock("query");
                        
                        if (query.getChildBlock("digest")!=null) {
                            jabberIqAuth(AUTH_DIGEST, stream);
                            return XmppObjectListener.BLOCK_PROCESSED;
                        } 
                        
                        if (query.getChildBlock("password")!=null) {
                        	
                        	if (!stream.isSecured() && stream.account.enablePlainAuth != XmppAccount.PLAIN_AUTH_ALWAYS) {
                        		throw new XmppException("Plain authentication disabled over non-secured connection");
                        	}
                        	if (stream.account.enablePlainAuth == XmppAccount.PLAIN_AUTH_NEVER) {
                        		throw new XmppException("Plain authentication disabled");
                        	}
                        	
                            jabberIqAuth(AUTH_PASSWORD, stream);
                            return XmppObjectListener.BLOCK_PROCESSED;
                        } 
                        
                    } catch (Exception e) { 
                    	e.printStackTrace();
                    }
                    throw new XmppException("No known authentication methods provided by server");
                    //return XmppObjectListener.NO_MORE_BLOCKS;
                }
            }
            
        } catch (Exception e) { }
        return XmppObjectListener.BLOCK_REJECTED;        
    }
    
}
