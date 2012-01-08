package org.bombusim.lime.activity;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.data.Roster;
import org.bombusim.lime.logger.LoggerActivity;
import org.bombusim.lime.service.XmppService;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class RosterActivity extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		ListAdapter adapter=new RosterAdapter(this);
		
		setListAdapter(adapter);
	
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//String item = (String) getListAdapter().getItem(position);
		//Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		getMenuInflater().inflate(R.menu.roster_menu, menu);
		
		return true;
	};
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.cmdLogin:    {
			startService(new Intent(getBaseContext(), XmppService.class));
			Lime.getInstance().doBindService();
			break;
		}
		case R.id.cmdLogout:   {
			Lime.getInstance().doUnbindService();
			stopService(new Intent(getBaseContext(), XmppService.class)); 
			break;
		}
		
		case R.id.cmdAccount:  startActivityForResult(new Intent(getBaseContext(), AccountSettingsActivity.class), 0); break;
		case R.id.cmdChat:     startActivityForResult(new Intent(getBaseContext(), ChatActivity.class),            0); break;
		case R.id.cmdLog:      startActivityForResult(new Intent(getBaseContext(), LoggerActivity.class),          0); break;
		case R.id.cmdSettings: startActivityForResult(new Intent(getBaseContext(), LimePrefs.class),               0); break;
			
		default: return true; // on submenu
		}
		
		return false;
	}
	
	private class RosterBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			((BaseAdapter)getListAdapter()).notifyDataSetChanged();
			getListView().invalidate();
		}
		
	}
		
	RosterBroadcastReceiver br;
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		br = new RosterBroadcastReceiver();
		registerReceiver(br, new IntentFilter(Roster.UPDATE_ROSTER));
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterReceiver(br);
	}
	
	
}
