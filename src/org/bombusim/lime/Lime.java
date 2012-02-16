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

package org.bombusim.lime;

import java.util.ArrayList;

import org.bombusim.lime.activity.Smilify;
import org.bombusim.lime.data.AccountsFactory;
import org.bombusim.lime.data.ChatFactory;
import org.bombusim.lime.data.Roster;
import org.bombusim.lime.data.VcardResolver;
import org.bombusim.lime.logger.LoggerData;
import org.bombusim.lime.service.XmppServiceBinding;
import org.bombusim.xmpp.XmppAccount;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;

public class Lime extends Application {
	private static Lime instance;
	
	public static Lime getInstance() { return instance; }
	
	public int avatarSize;
	
	//TODO: free memory when activity destroyed (avatars)
	private Roster roster;
	
	private LoggerData log;

	private ChatFactory chatFactory;
	
	private Smilify smilify;
	
	public Preferences prefs;
	
	private ArrayList<XmppAccount> accounts;
	private int activeAccountIndex;	
	
	public Roster getRoster() { return roster; } 
	
	public LoggerData getLog() { return log; }
	
	//temporary
	public VcardResolver vcardResolver;
	
	//temporary
	public XmppServiceBinding sb;
	
	@Override
	public final void onCreate() {
		super.onCreate();
		
		instance = this;
		
		loadPreferences();

		log = new LoggerData();
		
		vcardResolver = new VcardResolver(this);

		loadAccounts();
		
		avatarSize = getResources().getDimensionPixelSize(R.dimen.avatarSize);
		
	}

	public void loadAccounts() {
		accounts = AccountsFactory.loadAccounts(getApplicationContext());
		activeAccountIndex = AccountsFactory.getActiveAccountIndex(getApplicationContext());
		//TODO: remove workaround when new AccountListActivity will be added 
		if (activeAccountIndex >= accounts.size())
			activeAccountIndex = 0;
		roster=new Roster(getActiveAccount().userJid);
	}

	public void loadPreferences() {
		prefs = new Preferences(getApplicationContext());
	}

	@Override
	public final void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}

	//TODO: clean memory if inactive
	
	@Override
	public final void onLowMemory() {
		smilify = null;
		//TODO: cleanup log
		
		//drop chats (not critical since they are stored in db)
		chatFactory = null;
		
		//drop cached avatars
		roster.dropCachedAvatars();
		super.onLowMemory();
	}
	
	
	@Override
	public final void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	private String version;

	//tracking last message notification. 
	//TODO: variable may be lost if application restarted :(
	public long lastMessageId = -1;
	
	public String getVersion() {
		if (version==null) {
			try {
				PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);

				//version = pinfo.versionName + " ("+ pinfo.versionCode + ")";
				version = pinfo.versionName 
						+ "." + getResources().getString(R.string.git_count)
						+ " ("+ getResources().getString(R.string.git_short) + ")";
			} catch (NameNotFoundException e) {
				version = "unknown";
			}
			
		}
		return version;
	}

	public XmppAccount getActiveAccount() {
		return accounts.get(activeAccountIndex);
	}
	
	public String getOsId() {
		StringBuilder sb = new StringBuilder();
		sb.append(android.os.Build.MANUFACTURER).append(' ');
		sb.append(android.os.Build.MODEL).append(" / Android");
		sb.append(" sdk=").append(android.os.Build.VERSION.SDK);
		sb.append(' ').append(android.os.Build.VERSION.INCREMENTAL);
		
		return sb.toString();
	}

	public ChatFactory getChatFactory() {
		if (chatFactory == null) chatFactory = new ChatFactory();
		return chatFactory;
	}

	public NotificationMgr notificationMgr() {
		// TODO Auto-generated method stub
		return new NotificationMgr(this);
	}

	public Smilify getSmilify() {
		if (smilify == null) {
			smilify = new Smilify();
		}
		return smilify;
	}

	public void addNewAccount() {
		int active=AccountsFactory.addNew(accounts);
		AccountsFactory.saveAccount(getApplicationContext(), accounts.get(active));
		setActiveAccountIndex(active);
	}

	public void setActiveAccountIndex(int active) {
		AccountsFactory.saveActiveAccountIndex(getApplicationContext(), active);
		activeAccountIndex = active;
	}

	public void deleteActiveAccount() {
		AccountsFactory.removeAccount(getApplicationContext(), getActiveAccount());
		accounts.remove(getActiveAccount());
		setActiveAccountIndex(accounts.size()-1);
	}

	public String[] getAccountLabels() {
		return AccountsFactory.getLabels(accounts);
	}

	public int getActiveAccountIndex() { return activeAccountIndex; }
}
