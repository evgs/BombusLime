package org.bombusim.lime.activity;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.data.Contact;
import org.bombusim.lime.data.Roster;
import org.bombusim.lime.logger.LoggerActivity;
import org.bombusim.lime.service.XmppService;
import org.bombusim.lime.service.XmppServiceBinding;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.handlers.IqRoster;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

public class RosterActivity extends ListActivity {
	XmppServiceBinding sb;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		ListAdapter adapter=new RosterAdapter(this);
		
		setListAdapter(adapter);
		
		registerForContextMenu(getListView());
	
		sb = new XmppServiceBinding(this);
		
		//temporary
		Lime.getInstance().sb=sb;
	}

	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//String item = (String) getListAdapter().getItem(position);
		//Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
		Contact c = (Contact) getListAdapter().getItem(position);
	
		openChatActivity(c);
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
			break;
		}
		case R.id.cmdLogout:   {
			sb.doDisconnect();
			Lime.getInstance().vcardResolver.restartQueue();
			break;
		}
		
		case R.id.cmdAccount:  startActivityForResult(new Intent(getBaseContext(), AccountSettingsActivity.class), 0); break;
		//case R.id.cmdChat:     startActivityForResult(new Intent(getBaseContext(), ChatActivity.class),            0); break;
		case R.id.cmdLog:      startActivityForResult(new Intent(getBaseContext(), LoggerActivity.class),          0); break;
		case R.id.cmdSettings: startActivityForResult(new Intent(getBaseContext(), LimePrefs.class),               0); break;
			
		default: return true; // on submenu
		}
		
		return false;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);
		
		AdapterContextMenuInfo cmi = (AdapterContextMenuInfo) menuInfo;
		
		//TODO: select menu to be created
		Contact c = (Contact) getListAdapter().getItem(cmi.position);

		menu.setHeaderTitle(c.getScreenName());
		
		Drawable icon = new BitmapDrawable(c.getAvatar());
		menu.setHeaderIcon(icon);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contact_menu, menu);
		
		//enable items available only if logged in
		
		menu.setGroupEnabled(R.id.groupLoggedIn, sb.isLoggedIn(c.getRosterJid()) );
		
	}
	
	private int contextMenuItemPosition;
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		// workaround for issue http://code.google.com/p/android/issues/detail?id=7139
		// item.getMenuInfo() returns null for submenu items
		int pos= (info!=null)? info.position : contextMenuItemPosition;
		contextMenuItemPosition = pos;

		final Contact ctc = (Contact) getListAdapter().getItem(pos);

		switch (item.getItemId()) {
		case R.id.cmdChat: 
			openChatActivity(ctc);
			return true;
		case R.id.cmdVcard:
			openVCardActivity(ctc);
			return true;
		case R.id.cmdDelete:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(ctc.getFullName())
				   .setIcon(new BitmapDrawable(ctc.getAvatar()) )
				   .setMessage(R.string.confirmDelete)
			       .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                deleteContact(ctc);
			           }
			       })
			       .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
			       });
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(this);
			alert.show();
			return true;
		default:
			Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
		}

		return super.onContextItemSelected(item);
	}

	private class RosterBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			refreshVisualContent();
		}
		
	}
	
	void refreshVisualContent(){
		((BaseAdapter)getListAdapter()).notifyDataSetChanged();
		getListView().invalidate();
	}
	
	RosterBroadcastReceiver br;
	@Override
	protected void onResume() {
		super.onResume();
		sb.doBindService();
		//update view to actual state
		refreshVisualContent();
		br = new RosterBroadcastReceiver();
		registerReceiver(br, new IntentFilter(Roster.UPDATE_ROSTER));
	}
	
	@Override
	protected void onPause() {
		sb.doUnbindService();
		unregisterReceiver(br);
		super.onPause();
	}
	
	public void openChatActivity(Contact c) {
		Intent openChat =  new Intent(this, ChatActivity.class);
		openChat.putExtra(ChatActivity.MY_JID, c.getRosterJid());
		openChat.putExtra(ChatActivity.TO_JID,   c.getJid());
		startActivityForResult(openChat, 0);
	}

	public void openVCardActivity(Contact c) {
		Intent openVcard =  new Intent(this, VCardActivity.class);
		openVcard.putExtra(VCardActivity.MY_JID, c.getRosterJid());
		openVcard.putExtra(VCardActivity.JID,   c.getJid());
		startActivity(openVcard);
	}
	
	protected void deleteContact(Contact c) {
		IqRoster.deleteContact(
			c.getJid(), 
			sb.getXmppStream(c.getRosterJid())
		);
	}
	
}
