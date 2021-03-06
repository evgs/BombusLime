/*
 * StartTLS.java
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

import org.bombusim.lime.logger.LimeLog;
import org.bombusim.xmpp.exception.XmppAuthException;
import org.bombusim.xmpp.exception.XmppException;

public class StartTLS extends XmppObjectListener{
	public final static String XMLNS_STARTTLS = "urn:ietf:params:xml:ns:xmpp-tls";
	
	@Override
	public int blockArrived(XmppObject data, XmppStream stream)
			throws IOException, XmppException {

		//step 1. searching feature starttls
		if (data.getTagName().equals("stream:features")) {

           	int clientReqiredSecurity=stream.account.secureConnection;

           	XmppObject starttls=data.getChildBlock("starttls");
           	
           	//check if server doesn't provide STARTTLS when required by client
           	if (starttls == null) {
           		if (clientReqiredSecurity == XmppAccount.SECURE_CONNECTION_ALWAYS)
           			throw new XmppAuthException("Server doesn't provide secure TLS connection (REQUIRED)");
           		else return BLOCK_REJECTED;
           	}
           	
           	//check if server requires STARTTLS when disabled on client side
       		if (clientReqiredSecurity == XmppAccount.SECURE_CONNECTION_DISABLED) {
       			if (starttls.getChildBlock("required") !=null)
           			throw new XmppAuthException("Server requires TLS connection. Please enable it!");
       			else return BLOCK_REJECTED;
           	}

       		//server provides STARTTLS, and it is enabled (or required) on client side 
			if (starttls.compareNameSpace(XMLNS_STARTTLS)) {
                //step 2. negotiating starttls
				LimeLog.i("STARTTLS", "Requesting stream encryption", null);
				stream.send(starttls);
				return XmppObjectListener.BLOCK_PROCESSED;
			}
			
		}  
		
		//step 3. server confirms starttls

		if ( data.getTagName().equals("proceed")) {
			//securing connection
            stream.setTLS();
            //restarting xmpp stream over secured connection 
            stream.initiateStream();
            return XmppObjectListener.NO_MORE_BLOCKS;
        }       
        	
		return BLOCK_REJECTED;
	}

	public final static int PRIORITY_STARTTLS = PRIORITY_STREAM;
	@Override
	public int priority() { return PRIORITY_STARTTLS; }
}
