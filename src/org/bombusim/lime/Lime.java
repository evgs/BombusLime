package org.bombusim.lime;

import java.util.ArrayList;

import org.bombusim.lime.data.AccountsFactory;
import org.bombusim.lime.data.Roster;
import org.bombusim.lime.data.VcardResolver;
import org.bombusim.lime.logger.LoggerData;
import org.bombusim.lime.service.XmppService;
import org.bombusim.xmpp.XmppAccount;
import org.bombusim.xmpp.XmppStream;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.IBinder;

public class Lime extends Application {
	private static Lime instance;
	
	public static Lime getInstance() { return instance; }
	
	public int avatarSize;
	
	//TODO: free memory when activity destroyed (avatars)
	private Roster roster;
	
	private LoggerData log;

	public Preferences prefs;
	
	//TODO: temporary
	public boolean online;

	public boolean localXmlEnabled = false;
	
	public ArrayList<XmppAccount> accounts;
	
	public Roster getRoster() { return roster; } 
	
	public LoggerData getLog() { return log; }
	
	public VcardResolver vcardResolver;
	
	public String getOsId() {
		StringBuilder sb = new StringBuilder();
		sb.append(android.os.Build.MANUFACTURER).append(' ');
		sb.append(android.os.Build.MODEL).append(" / Android");
		sb.append(" sdk=").append(android.os.Build.VERSION.SDK);
		sb.append(' ').append(android.os.Build.VERSION.INCREMENTAL);
		
		return sb.toString();
	}

	
	
	@Override
	public final void onCreate() {
		super.onCreate();
		
		instance = this;
		
		prefs = new Preferences(getApplicationContext());
		
		accounts = AccountsFactory.loadAccounts(getApplicationContext());
		
		log = new LoggerData();
		
		vcardResolver = new VcardResolver();

		roster=new Roster(accounts.get(0).userJid);
		
		avatarSize = getResources().getDimensionPixelSize(R.dimen.avatarSize);
		
	}

	@Override
	public final void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
		doUnbindService();
	}

	@Override
	public final void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}
	
	
	@Override
	public final void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	private XmppService xmppService;
	private ServiceConnection xsc = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			xmppService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			xmppService = ((XmppService.LocalBinder)service).getService();
		}
	};
	
	public void doBindService() { 
		bindService(new Intent(getBaseContext(), XmppService.class), xsc, Context.BIND_AUTO_CREATE); 
	}

	public void doUnbindService() {
		unbindService(xsc);
	}
	
	public XmppStream getXmppStream(String rosterJid) {
		// TODO Auto-generated method stub
		if (!online) return null;
		XmppStream s = xmppService.getXmppStream(rosterJid);
		
		return s;
	}

	private String version;
	
	public String getVersion() {
		if (version==null) {
			try {
				PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);

				version = pinfo.versionName + " ("+ pinfo.versionCode + ")";
			} catch (NameNotFoundException e) {
				version = "unknown";
			}
			
		}
		return version;
	}
}
