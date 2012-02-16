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

import org.bombusim.lime.activity.ChatActivity;
import org.bombusim.lime.activity.RosterActivity;
import org.bombusim.lime.data.Contact;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

public class NotificationMgr {
    private static final int MESSAGE_MAXLEN_NOTIFY = 250;
	private final static int NOTIFICATION_ONLINE = R.string.app_name;
    private final static int NOTIFICATION_CHAT = R.string.chatNotify;
	
    //TODO: read from configuration
	private boolean serviceIcon = true;
    
    
	private NotificationManager mNM;
	private Context context;

	public NotificationMgr(Context context) {
		this.context = context;
		mNM = (NotificationManager)(context.getSystemService(Context.NOTIFICATION_SERVICE));
	}
	
	public void showChatNotification(Contact visavis, String message, long id) {
		
		// target ChatActivity
		Intent openChat =  new Intent(context, ChatActivity.class);
		openChat.putExtra(ChatActivity.MY_JID, visavis.getRosterJid());
		openChat.putExtra(ChatActivity.TO_JID, visavis.getJid());
		
		if (message.length()>MESSAGE_MAXLEN_NOTIFY) {
			message = message.substring(0, MESSAGE_MAXLEN_NOTIFY) + "...";
		}
		
		
		int icon = android.R.drawable.stat_notify_chat; 
		String title = visavis.getScreenName(); 
		//TODO: vibration
		//TODO: play sound

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(icon, title + ": "+message, System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = 
        		PendingIntent.getActivity(context, 0, openChat, 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(context, title, message, contentIntent);
        
        //TODO: optional
        //TODO: check activity is already visible
        Preferences p = Lime.getInstance().prefs;
        
        String ringtone = p.ringtoneMessage;
        if (ringtone.length() != 0) {
        	notification.sound = Uri.parse(ringtone);
        }
        
        if (p.vibraNotifyMessage)
        	notification.defaults |= Notification.DEFAULT_VIBRATE;
        
        
        if (p.ledNotifyMessage) {
        	//notification.defaults |= Notification.DEFAULT_LIGHTS;
            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
            notification.ledARGB = Color.GREEN;
            notification.ledOnMS  = 300;
            notification.ledOffMS = 1000;
        }
        
        
        
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // Send the notification.
        synchronized (this) {
            mNM.notify(NOTIFICATION_CHAT, notification);
            Lime.getInstance().lastMessageId = id;
		}

	}
	
	public void cancelChatNotification(long id) {
		synchronized (this) {
			if (id == Lime.getInstance().lastMessageId) {
				mNM.cancel(NOTIFICATION_CHAT);
				Lime.getInstance().lastMessageId = -1;
			}
		}
	}
	
	public void showOnlineNotification(boolean online) {
		if (!serviceIcon) return;
	
		int icon = ((online)? R.drawable.status_online : R.drawable.status_offline);
		CharSequence title = context.getText(R.string.app_name);
		CharSequence message = context.getText((online)? R.string.presence_online : R.string.presence_offline); 
		
        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(icon, title, System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = 
        		PendingIntent.getActivity(context, 0, new Intent(context, RosterActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(context, title, message, contentIntent);
        
        notification.flags |= Notification.FLAG_NO_CLEAR;  
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        // Send the notification.
        mNM.notify(NOTIFICATION_ONLINE, notification);
    }

	public void cancelOnlineNotification() { 
		mNM.cancel(NOTIFICATION_ONLINE);
	}
}
