/*
 * StreamCompression.java
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
import org.bombusim.xmpp.exception.XmppException;

/**
 * XEP-0138: Stream Compression 
 * @author evgs
 *
 */

public class StreamCompression extends XmppObjectListener {

	private static final String XMLNS_COMPRESSION = "http://jabber.org/protocol/compress";

	@Override
	public int blockArrived(XmppObject data, XmppStream stream)
			throws IOException, XmppException {
		
		//step 1. searching feature compression
	    if (data.getTagName().equals("stream:features")) {

	    	if (stream.account.trafficCompression) {
	        	XmppObject compr=data.getChildBlock("compression");
	            if (compr!=null) { 
	                if (compr.getChildBlockByText("zlib")!=null) {
	                    //step 2. negotiating compression
	        			LimeLog.i("ZLIB", "Requesting stream compression", null);

	                    XmppObject askCompr=new XmppObject("compress", null, null);
	                    askCompr.setNameSpace(XMLNS_COMPRESSION);
	                    askCompr.addChild("method", "zlib");
	                    stream.send(askCompr);
	                    
	                    return XmppObjectListener.BLOCK_PROCESSED;
	                }
	            }
	    	}
	    }

	    //step 3. server confirms compression
        else if ( data.getTagName().equals("compressed")) {
        	//starting compression
            stream.setZlibCompression();
            //restarting xmpp stream over compressed connection
            stream.initiateStream();
            return XmppObjectListener.NO_MORE_BLOCKS;
        }

	    return BLOCK_REJECTED;
	}

	public final static int PRIORITY_ZLIB = StartTLS.PRIORITY_STARTTLS+10;
	@Override
	public int priority() { return PRIORITY_ZLIB; }
}
