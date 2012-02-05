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
	
	
	public void showChatNotification(Contact visavis, String message) {
		
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
        String ringtone = Lime.getInstance().prefs.ringtoneMessage;
        if (ringtone.length() == 0) {
            notification.defaults |= Notification.DEFAULT_SOUND; 
        } else {
        	notification.sound = Uri.parse(ringtone);
        }
        
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        //notification.defaults |= Notification.DEFAULT_LIGHTS;
        
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.ledARGB = Color.GREEN;
        notification.ledOnMS  = 300;
        notification.ledOffMS = 1000;
        
        
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // Send the notification.
        mNM.notify(NOTIFICATION_CHAT, notification);

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
