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

package org.bombusim.lime.service;

import org.bombusim.lime.Lime;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppStream;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class XmppServiceBinding {
	private Context context;
	
	private XmppServiceBinding(){};
	public XmppServiceBinding(Context context) {
		this();
		this.context = context;
	}
	
	private XmppService xmppService;
	private ServiceConnection xsc = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			xmppService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			xmppService = ((XmppService.LocalBinder)service).getService();
			if (bindlistener !=null) bindlistener.onBindService(xmppService);
		}
	};
	
	public interface BindListener {
		abstract void onBindService(XmppService service);
	}
	
	private BindListener bindlistener;
	
	public void setBindListener(BindListener bl) {
		this.bindlistener = bl;
	}
	
	public void doBindService() { 
		Lime.getInstance().bindService(new Intent(context, XmppService.class), xsc, Context.BIND_AUTO_CREATE); 
	}

	public void doUnbindService() {
		if (xmppService != null)
			Lime.getInstance().unbindService(xsc);
	}
	
	public XmppStream getXmppStream(String rosterJid) {
		XmppStream s = xmppService.getXmppStream(rosterJid);
		if (s == null) return null;
		if (!s.resourceConnected) return null;
		
		return s;
	}
	
	public boolean isLoggedIn(String rosterJid) {
		return getXmppStream(rosterJid)!=null;
	}
	
	public void doDisconnect() {
		xmppService.disconnectAll();
	}
	
	/**
	 * Sending XMPP stanza
	 * @param streamJid - JID identifies stream so be used  
	 * @param stanza stanza to be sent
	 */
	public void postStanza(String streamJid, XmppObject stanza) {
		try {
		    xmppService.postStanza(stanza, streamJid);
		} catch (Exception e) {	e.printStackTrace(); }
	}
	
	/**
	 * USE WITH CAUTION!!!
	 * @return XmppService object
	 */
	@Deprecated
	public XmppService getXmppService() {
		return xmppService;
	}
}
