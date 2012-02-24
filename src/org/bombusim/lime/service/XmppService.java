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

import java.io.IOException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import org.bombusim.lime.Lime;
import org.bombusim.lime.data.PresenceStorage;
import org.bombusim.lime.data.Roster;
import org.bombusim.lime.logger.LimeLog;
import org.bombusim.xml.XMLException;
import org.bombusim.xmpp.XmppAccount;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.exception.XmppAuthException;
import org.bombusim.xmpp.exception.XmppException;
import org.bombusim.xmpp.exception.XmppTerminatedException;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.ResolverConfig;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;

public class XmppService extends Service implements Runnable {

    private BroadcastReceiver br;

	private XmppStream s;

	 /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
	public class LocalBinder extends Binder {
		public XmppService getService() { 	return XmppService.this; }
	}
	
	 // This is the object that receives interactions from clients.
	private final IBinder binder = new LocalBinder();  
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public void sendStanza(XmppObject stanza, String fromJid) {
		//TODO: select the stream by "fromJid"
		if (s!=null) s.send(stanza);
	}

	public XmppStream getXmppStream(String rosterJid) {
		//TODO: select the stream by "fromJid"
		return s;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        //LimeLog.i("XmppService", "Received start id " + startId, intent.toString());
		
		XmppAccount activeAccount = Lime.getInstance().getActiveAccount();
		
		// TODO start multiple connections
		if (s==null) {
			s=new XmppStream(activeAccount);
			s.setContext(this);
		} else {
			s.bindAccount(activeAccount);
		}
		
		if (br == null) {
			br = new ConnBroadcastReceiver();
			registerReceiver(br, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		}
		
	   	checkNetworkState();
	   	
	   	PresenceStorage ps = new PresenceStorage(this);
	   	
	   	s.setPresence(ps.getStatus(), ps.getMessage(), ps.getPriority());
	   	
	   	if (running) {
	   		if (s.loggedIn)
	   			s.sendPresence();
	   	} else {
	   		doConnect();
	   	}
		
	   	return START_STICKY;
	}
	
	public boolean running = false;
	
	public void doConnect() {
		//TODO: check presence status
		if (networkAvailable) {
			if (!running) {
				running = true;
			   	
				Thread thread=new Thread( this );
				thread.setName("XmppStream->"+s.jid);
				thread.start();
			} 
		} else {
			s.close();
		}
	}
	
	@Override
	public void run() {
		while (running) {
		   	try {
		        showNotification(false);
			   	//update DNS server info
			   	ResolverConfig.refresh();
			   	Lookup.refreshDefault();

			   	//language code for xmpp stream
			   	//TODO: check http://developer.android.com/reference/java/util/Locale.html
			   	//  Note that Java uses several deprecated two-letter codes. 
			   	//  The Hebrew ("he") language code is rewritten as "iw", Indonesian ("id") as "in", and Yiddish ("yi") as "ji"
			   	String lang = getResources().getConfiguration().locale.getLanguage();
			   	s.setLocaleLang(lang);

		        showNotification(true);
			   	s.connect();
			   	
			} catch (UnknownHostException e) {
				//TODO: sometimes Unknown host may be thrown if interface switching is in progress
		    	LimeLog.e("XmppStream", "Unknown Host", e.toString());
				running = false;
				
			} catch (SSLException e) {
				//TODO: Raise error notification if Certificate exception
		        showNotification(false);
				if (s.isSecured()) {
					LimeLog.e("XmppStream", "SSL Error (IO)", e.toString());
				} else {
					LimeLog.e("XmppStream", "SSL Error (Handshake)", e.toString());
					running = false;
				}
				
		    } catch (IOException e) {
		    	if (!networkAvailable) running = false;
		    	LimeLog.e("XmppStream", "IO Error", e.toString());
		        showNotification(false);
			} catch (XmppAuthException e) {
		    	LimeLog.e("XmppStream", "Authentication error", e.getMessage());
				running = false;
				e.printStackTrace();
			} catch (XmppTerminatedException e) {
		    	LimeLog.e("XmppStream", "Stream shutdown", e.getMessage());
				running = false;
				e.printStackTrace();
			} catch (XmppException e) {
		    	LimeLog.e("XmppStream", "Xmpp Error", e.getMessage());
				running = false;
				e.printStackTrace();
			} catch (XMLException e) {
		    	LimeLog.e("XmppStream", "XML broken", e.toString());
		        showNotification(false);
			}
		   	
	    	//TODO: check status (online/offline)
		   	if (!networkAvailable) running = false;
		   	
		   	s.close();
		   	
		   	Lime.getInstance().getRoster().forceRosterOffline(s.jid);
			s.sendBroadcast(Roster.UPDATE_CONTACT);

		   	
		}
        cancelNotification();
	}

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        cancelNotification();
    }
	
    /**
     * Show a notification while this service is running.
     */
    private void showNotification(boolean online) {
    	Lime.getInstance().notificationMgr().showOnlineNotification(online);    
    }

    private void cancelNotification() {
    	Lime.getInstance().notificationMgr().cancelOnlineNotification();
    }
    
    private boolean networkAvailable;

	private int networkType;
	
    public void checkNetworkState() {
    	try {
    		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    		networkAvailable = cm.getActiveNetworkInfo().isAvailable();
    		int networkType = cm.getActiveNetworkInfo().getType();
    		this.networkType = networkType;
    		
    		//TODO: change behavior after VcardResolver refactoring
    		Lime.getInstance().vcardResolver.setOnMobile(networkType==ConnectivityManager.TYPE_MOBILE);
    		
    	} catch (Exception e) {
    		networkAvailable = false;
    	}
    	
    }
    
    
	private class ConnBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			checkNetworkState();

			LimeLog.i("XmppService", "Network state: "  
					+ ((networkType==ConnectivityManager.TYPE_WIFI)?"WiFi":"GPRS")
					+ ((networkAvailable)?" Up":" Down" ),
					null);
			
			doConnect();
			
		}
		
	}


	public void disconnectAll() {
        //TODO: remove try/catch when service on/off behavior will be changed 
        try {
        	unregisterReceiver(br);
        	br = null;
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        //TODO: remove when service on/off behavior will be changed 
        running = false;
        if (s!=null)
        	s.close();
        
        stopSelf();
	}
}
