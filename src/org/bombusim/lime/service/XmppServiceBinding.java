package org.bombusim.lime.service;

import org.bombusim.lime.Lime;
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
		}
	};
	
	public void doBindService() { 
		Lime.getInstance().bindService(new Intent(context, XmppService.class), xsc, Context.BIND_AUTO_CREATE); 
	}

	public void doUnbindService() {
		if (xmppService != null)
			Lime.getInstance().unbindService(xsc);
		xmppService = null;
	}
	
	public XmppStream getXmppStream(String rosterJid) {
		XmppStream s = xmppService.getXmppStream(rosterJid);
		if (s == null) return null;
		if (!s.loggedIn) return null;
		
		return s;
	}
	
	public boolean isLoggedIn(String rosterJid) {
		return getXmppStream(rosterJid)!=null;
	}
	
	public void doDisconnect() {
		xmppService.disconnectAll();
	}
}
