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

import org.bombusim.xmpp.exception.XmppException;

public class StartTLS implements XmppObjectListener{
	public final static String XMLNS_STARTTLS = "urn:ietf:params:xml:ns:xmpp-tls";
	
	@Override
	public int blockArrived(XmppObject data, XmppStream stream)
			throws IOException, XmppException {

		//step 1. searching feature starttls
		if (data.getTagName().equals("stream:features")) {

           	int requiredSecurity=stream.account.secureConnection;

        	if (requiredSecurity >= XmppAccount.SECURE_CONNECTION_IF_AVAILABLE) {
        		XmppObject starttls=data.getChildBlock("starttls");
        		if (starttls!=null) { 
        			if (starttls.compareNameSpace(XMLNS_STARTTLS)) {
	                    //step 2. negotiating starttls
        				stream.send(starttls);
        				return XmppObjectListener.BLOCK_PROCESSED;
        			}
        		} else {
        			if (requiredSecurity == XmppAccount.SECURE_CONNECTION_ALWAYS) 
        				throw new XmppException("Server doesn't provide secure TLS connection (REQUIRED)");
        		}
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

	@Override
	public String capsXmlns() { return null; }
}
