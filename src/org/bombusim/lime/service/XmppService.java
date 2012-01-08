package org.bombusim.lime.service;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.activity.RosterActivity;
import org.bombusim.xml.XMLException;
import org.bombusim.xmpp.XmppAccount;
import org.bombusim.xmpp.XmppException;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppStream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class XmppService extends Service implements Runnable {

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.app_name;

    private NotificationManager mNM;
    
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
		// TODO Auto-generated method stub
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
        Log.i("XmppService", "Received start id " + startId + ": " + intent);

		
		br = new ConnBroadcastReceiver();
		registerReceiver(br, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		
	   	checkNetworkState();
	   	
	   	connect();
	   	   	
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		
	   	return START_STICKY;
	}
	
	public boolean running = false;
	
	public void connect() {
		if (networkAvailable) {
			if (!running) {
				running = true;
		        // TODO start multiple connections
				
				XmppAccount a=Lime.getInstance().accounts.get(0);
				s=new XmppStream(a);
			   	s.setContext(this);
		   	
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
		// TODO Auto-generated method stub
		   	try {
		        // Display a notification about us starting.  We put an icon in the status bar.
		   		/*checkNetworkState();
		   		
		   		if (!networkAvailable) { 
		   			running = false;
		   			break;
		   		}*/
		   		
		        showNotification();
		        s.connect();
				//Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				running = false;
				e.printStackTrace();
				
				//Toast.makeText(this, "Unknown host", Toast.LENGTH_SHORT).show();
			} catch (SSLException e) {
				running = false;
				e.printStackTrace();
				
		    } catch (IOException e) {
		    	if (!networkAvailable) running = false;
		    	//TODO: check network state before reconnecting
		    	//TODO: check status (online/offline)
				e.printStackTrace();
			} catch (XMLException e) {
				//Toast.makeText(this, "XML exception", Toast.LENGTH_SHORT).show();
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmppException e) {
				
				//Toast.makeText(this, "Xmpp exception", Toast.LENGTH_SHORT).show();
				running = false;
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   	
		   	Lime.getInstance().online = false;
		}
        mNM.cancel(NOTIFICATION);
	}

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);
        
        unregisterReceiver(br);
        
        running = false;
        s.close();
        
        // Tell the user we stopped.
        //Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
    }
	
    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.app_name);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.ic_launcher, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, RosterActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.app_name),
                       text, contentIntent);

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

    
    private boolean networkAvailable;
    
    public void checkNetworkState() {
    	try {
    		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    		networkAvailable = cm.getActiveNetworkInfo().isAvailable();
    	} catch (Exception e) {
    		networkAvailable = false;
    	}
    }
    
    
	private class ConnBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			
			checkNetworkState();
			
			connect();
			
		}
		
	}

}
