/*
 * IqTimeReply.java
 *
 * Created on 10.09.2005, 23:15
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

package org.bombusim.xmpp.handlers;

import java.io.IOException;

import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppObjectListener;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.exception.XmppException;
import org.bombusim.xmpp.stanza.Iq;

import android.text.format.Time;

/**
 *
 * @author EvgS
 */
public class IqTimeReply implements XmppObjectListener{
    
    public IqTimeReply(){};

    private final static String CAPS_XMLNS = "urn:xmpp:time";
	@Override
	public String capsXmlns() {	return CAPS_XMLNS; }
    
    
	@Override
	public int blockArrived(XmppObject data, XmppStream stream) throws IOException, XmppException {

		if (!(data instanceof Iq)) return BLOCK_REJECTED;
		
		if (!data.getAttribute("type").equals("get")) return BLOCK_REJECTED;
    
		XmppObject query=data.findNamespace("query", "jabber:iq:time");
		
		if (query!=null) {

			android.text.format.Time t = getCurrentTime();

			query.addChild("display", t.format("%d/%m/%Y %H:%M"));

			t.switchTimezone("UTC");
			query.addChild("utc", t.format("%Y%m%dT%H:%M:%S"));
			
		} else {
			query=data.findNamespace("time", CAPS_XMLNS);
			if (query==null) return BLOCK_REJECTED;
			
			android.text.format.Time t = getCurrentTime();

			StringBuilder tzo = new StringBuilder(t.format("%z"));
			if (tzo.length()==0) tzo.append("+0000");
			tzo.insert(3, ':');
			query.addChild("tzo", tzo.toString());
			
			t.switchTimezone("UTC");
			query.addChild("utc",t.format3339(false));
		}
        
        Iq reply=new Iq(data.getAttribute("from"), Iq.TYPE_RESULT, data.getAttribute("id"));
        reply.addChild(query);
        
        stream.send(reply);
        
        return BLOCK_PROCESSED;

    }
	
	Time getCurrentTime() {
		Time current=new Time(Time.getCurrentTimezone());
		current.set(System.currentTimeMillis());
		return current;
	}
}
