/*
 * IqPing.java
 *
 * Created on 11.05.2008, 19:26
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

package org.bombusim.xmpp.handlers;

import java.io.IOException;

import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppObjectListener;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.exception.XmppException;
import org.bombusim.xmpp.stanza.Iq;

public class IqPing extends XmppObjectListener{
    
    private static final String URN_XMPP_PING = "urn:xmpp:ping";

	/** Creates a new instance of Ping */
    public IqPing() {}

    @Override
    public int blockArrived(XmppObject data, XmppStream stream)
    		throws IOException, XmppException {

    	try {
        
	    	Iq ping = (Iq) data; 
	    	
	        String from=ping.getAttribute("from");
	        String id=ping.getAttribute("id");
	        String type=ping.getTypeAttribute();
	
	        if (type.equals("result") || type.equals("error")) {
	            if (!id.equals("ping")) return BLOCK_REJECTED;
	            //TODO: handle keep-alive ping result
	            return BLOCK_PROCESSED;
	        }    
	        
	        if (type.equals("get")){
	            // xep-0199 ping
	            if (ping.findNamespace("ping", URN_XMPP_PING)==null) return BLOCK_REJECTED;
	            
	            Iq pong=new Iq(from, Iq.TYPE_RESULT, data.getAttribute("id"));
	            stream.send(pong);
	            
	            return BLOCK_PROCESSED;
	        }
	        
    	} catch (Exception e) { }  //handling classcast exception and NPE
        
        return BLOCK_REJECTED;
    }

	@Override
	public String capsXmlns() { return URN_XMPP_PING; 	}
}
