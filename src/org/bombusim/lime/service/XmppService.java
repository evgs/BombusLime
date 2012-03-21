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
import org.bombusim.xmpp.stanza.XmppPresence;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.ResolverConfig;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class XmppService extends Service {

    public static final String ON_KEEP_ALIVE = "onKeepAlive";
    public static final String ON_BOOT       = "onBoot";
    public static final String ON_STATUS     = "onStatusChange";

	private BroadcastReceiver mBr;

	private XmppStream mStream;

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

	public void postStanza(XmppObject stanza, String fromJid) {
		XmppStream s = getXmppStream(fromJid);
		
		if (s!=null) s.postStanza(stanza);
	}

	public XmppStream getXmppStream(String rosterJid) {
		//TODO: select the stream by "fromJid"
		return mStream;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    
        // ATENTION! intent is null if service recreated by system after being killed
        // see http://developer.android.com/reference/android/app/Service.html#START_STICKY for details
	    
        if (intent!=null) {
            if (ON_KEEP_ALIVE.equals(intent.getAction())) {
                keepAlive();
                mKeepAliveAlarm.releaseWakeLock();
                
                return START_STICKY;
            }
        }
        //LimeLog.i("XmppService", "Received start id " + startId, intent.toString());
		
		XmppAccount activeAccount = Lime.getInstance().getActiveAccount();
		
		// TODO start multiple connections
		if (mStream==null) {
			mStream=new XmppStream(activeAccount);
			mStream.setContext(this);
		} else {
			mStream.bindAccount(activeAccount);
		}
		
        //language code for xmpp stream
        //TODO: check http://developer.android.com/reference/java/util/Locale.html
        //  Note that Java uses several deprecated two-letter codes. 
        //  The Hebrew ("he") language code is rewritten as "iw", Indonesian ("id") as "in", and Yiddish ("yi") as "ji"
        String lang = getResources().getConfiguration().locale.getLanguage();
        mStream.setLocaleLang(lang);
		
		if (mBr == null) {
			mBr = new ConnBroadcastReceiver();
			registerReceiver(mBr, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		}
		
	   	checkNetworkState();
	   	
	   	PresenceStorage ps = new PresenceStorage(this);
	   	
	   	mStream.setPresence(ps.getStatus(), ps.getMessage(), ps.getPriority());
	   	
   		if (mStream.resourceAvailable) {
   			mStream.sendPresence();
	   	} else {
	   		doConnect();
	   	}
		
	   	return START_STICKY;
	}
	
	public void doConnect() {
	    
	    //TODO: perform check for every account 
	    if (mStream.getStatus() == XmppPresence.PRESENCE_OFFLINE) return;
	    
		if (networkAvailable) {
            //update DNS server info
            //TODO: refresh on network status changed
            ResolverConfig.refresh();
            Lookup.refreshDefault();
            
            mKeepAliveAlarm.setAlarm(this);
            showNotification(true);
            
            mStream.doConnect();
            
		} else {
			mStream.doForcedDisconnect();
			
			mKeepAliveAlarm.cancelAlarm(this);
	        cancelNotification();

		}
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
        	unregisterReceiver(mBr);
        	mBr = null;
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        mKeepAliveAlarm.cancelAlarm(this);
        
        if (mStream!=null)
        	mStream.doForcedDisconnect();
        
        stopSelf();
	}
	
	private KeepAliveAlarm mKeepAliveAlarm = new KeepAliveAlarm();

	private void keepAlive() {
		//TODO: keep alive all connections
	    if (mStream!=null) 
	        mStream.keepAlive();
	    //TODO: check how keepAlive may be active with s==null
	}
	
}
