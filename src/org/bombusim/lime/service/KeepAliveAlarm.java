package org.bombusim.lime.service;

import org.bombusim.lime.Lime;
import org.bombusim.lime.logger.LimeLog;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class KeepAliveAlarm extends BroadcastReceiver {
    private static final long KEEPALIVE_PERIOD_MINUTE = 60*1000; //1 minute

    PowerManager.WakeLock wakeLock;
    
    XmppServiceBinding sb;
    
	@Override
	public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BombusKeepAlive");
        
        wakeLock.acquire();
        LimeLog.d("ALARM", "KEEP-ALIVE", null);
        
		context.startService(new Intent(XmppService.ON_KEEP_ALIVE, null, context, XmppService.class));
        
    }
	
	public void releaseWakeLock() {
		if (wakeLock!=null) wakeLock.release();
	}
	
	private AlarmManager getAlarmManager(Context context) { 
		return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE); 
	}
    
	public void setAlarm(Context context) {
		cancelAlarm(context);
	    
        PendingIntent keepAlivePendingIntent 
           = PendingIntent.getBroadcast(context, 0, new Intent(context, KeepAliveAlarm.class), 0);
		
		long period = Lime.getInstance().prefs.keepAlivePeriodMinutes * KEEPALIVE_PERIOD_MINUTE;
		
		getAlarmManager(context).setRepeating(
				AlarmManager.RTC_WAKEUP, 
				System.currentTimeMillis() + period, 
				period, keepAlivePendingIntent); // Millisec * Second * Minute
     }

     public void cancelAlarm(Context context) {
    	 PendingIntent keepAlivePendingIntent
	           = PendingIntent.getBroadcast(context, 0, new Intent(context, KeepAliveAlarm.class), 0);
	        
    	 getAlarmManager(context).cancel(keepAlivePendingIntent);
     }		
	
}
