
/*
 * IqVersionReply.java
 *
 * Created on 27.02.2005, 18:31
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
 *
 */


package org.bombusim.xmpp.handlers;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppObjectListener;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.stanza.Iq;

/**
 *
 * @author Eugene Stahov
 */
public class IqVersionReply extends XmppObjectListener {
	
    private final static String CAPS_XMLNS = "jabber:iq:version";
	@Override
	public String capsXmlns() {	return CAPS_XMLNS; }
	
    public IqVersionReply(){};

    public int blockArrived(XmppObject data, XmppStream stream) {
        if (!(data instanceof Iq)) return BLOCK_REJECTED;
        String type=data.getAttribute("type");
        if (type.equals("get")) {
            
            XmppObject query=data.findNamespace("query", CAPS_XMLNS);
            if (query==null) return BLOCK_REJECTED;
            
            Iq reply=new Iq(data.getAttribute("from"), Iq.TYPE_RESULT, data.getAttribute("id"));
            reply.addChild(query);
            query.addChild("name", Lime.getInstance().getString(R.string.app_name));
            query.addChild("version",Lime.getInstance().getVersion());
            query.addChild("os", Lime.getInstance().getOsId());
            
            stream.send(reply);
            
            return XmppObjectListener.BLOCK_PROCESSED;
        }
        
        return BLOCK_REJECTED;

    }
}
