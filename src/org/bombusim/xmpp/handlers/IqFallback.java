/*
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

import org.bombusim.xmpp.XmppError;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppObjectListener;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.exception.XmppException;
import org.bombusim.xmpp.stanza.Iq;

public class IqFallback extends XmppObjectListener {

	@Override
	public int blockArrived(XmppObject data, XmppStream stream)
			throws IOException, XmppException {

		try {
			Iq f = (Iq)data;

			String from = f.getAttribute("from");
			
			String type = f.getTypeAttribute();
			
			if (type.equals("result")) { return BLOCK_REJECTED; }
			if (type.equals("error"))  { return BLOCK_REJECTED; }
				
			XmppError err = new XmppError(XmppError.FEATURE_NOT_IMPLEMENTED, null);

			f.setAttribute("to", from);
			f.setAttribute("from", null);
			f.setAttribute("type", "error");
			f.setAttribute("xml:lang", null);
			
			f.addChild(err.construct());

			stream.send(f);
		} catch (Exception e) {};
		
		return BLOCK_PROCESSED;
	}

	@Override
	public int priority() { return PRIORITY_IQUNKNOWN_FALLBACK; }
}
