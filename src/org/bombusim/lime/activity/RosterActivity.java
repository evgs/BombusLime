package org.bombusim.lime.activity;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.data.Contact;
import org.bombusim.lime.data.Roster;
import org.bombusim.lime.logger.LoggerActivity;
import org.bombusim.lime.service.XmppService;
import org.bombusim.lime.service.XmppServiceBinding;
import org.bombusim.xmpp.handlers.IqRoster;
import org.bombusim.xmpp.stanza.Presence;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
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
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.TextView;
import android.widget.Toast;

public class RosterActivity extends ExpandableListActivity {
	XmppServiceBinding sb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		
		ExpandableListAdapter adapter=new RosterAdapter(this);
		
		setListAdapter(adapter);
		
		registerForContextMenu(getExpandableListView());

		sb = new XmppServiceBinding(this);

		//temporary
		Lime.getInstance().sb=sb;
	
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.roster_title);
		updateRosterTitle();
		
		OnClickListener sslShowCert = new OnClickListener() {
			@Override
			public void onClick(View v) {
				try { 
					String rJid = Lime.getInstance().getActiveAccount().userJid;
					showSslStatus(sb.getXmppStream(rJid).getCertificateInfo());
				} catch (Exception e){};
			}
		};
		
		((ImageView) findViewById(R.id.imageSSL)).setOnClickListener(sslShowCert);
		((TextView) findViewById(R.id.activeJid)).setOnClickListener(sslShowCert);
	}

	protected void showSslStatus(String certificateChain) {
		if (certificateChain == null) return;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.sslInfo)
			   .setIcon(R.drawable.ssl_yes)
			   .setMessage(certificateChain)
		       .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
		       });
		AlertDialog alert = builder.create();
		alert.setOwnerActivity(this);
		alert.show();
	}

	private void updateRosterTitle() {
		String rJid = Lime.getInstance().getActiveAccount().userJid;
		((TextView) findViewById(R.id.activeJid)).setText(rJid);

		boolean isSecure = false; 
		try { 
			isSecure = sb.getXmppStream(rJid).isSecured();
		} catch (Exception e){};
		
		((ImageView) findViewById(R.id.imageSSL)).setVisibility(isSecure? View.VISIBLE : View.GONE);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {

		Contact c = (Contact) getExpandableListAdapter().getChild(groupPosition, childPosition);
	
		openChatActivity(c);
		
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		getMenuInflater().inflate(R.menu.roster_menu, menu);

		//enable items available only if logged in
		//TODO: modify behavior if multiple account
		/*
		String rJid = Lime.getInstance().accounts.get(0).userJid;
		menu.setGroupEnabled(R.id.groupLoggedIn, sb.isLoggedIn(rJid) );
		*/
		
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
		
		case R.id.cmdAddContact: openEditContactActivity(null); break;
		
		case R.id.cmdAccount:  startActivityForResult(new Intent(getBaseContext(), AccountSettingsActivity.class), 0); break;
		//case R.id.cmdChat:     startActivityForResult(new Intent(getBaseContext(), ChatActivity.class),            0); break;
		case R.id.cmdLog:      startActivityForResult(new Intent(getBaseContext(), LoggerActivity.class),          0); break;
		case R.id.cmdSettings: startActivityForResult(new Intent(getBaseContext(), LimePrefs.class),               0); break;
			
		case R.id.cmdAbout: About.showAboutDialog(this); break;
		default: return true; // on submenu
		}
		
		return false;
	}
	
	public Contact getContextContact(long pos) {
		int type=ExpandableListView.getPackedPositionType(pos);

		if (type!=ExpandableListView.PACKED_POSITION_TYPE_CHILD) return null; 

		int childPosition = ExpandableListView.getPackedPositionChild(pos);
		int groupPosition = ExpandableListView.getPackedPositionGroup(pos);
		
		Contact c = (Contact) getExpandableListAdapter().getChild(groupPosition, childPosition);
		return c;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);
		
		ExpandableListContextMenuInfo cmi = (ExpandableListContextMenuInfo) menuInfo;
		
		long pos = cmi.packedPosition;
		
		Contact c = getContextContact(pos);
		if (c==null) return; //TODO: context menu for group
		
		menu.setHeaderTitle(c.getScreenName());
		
		Drawable icon = new BitmapDrawable(c.getAvatar());
		menu.setHeaderIcon(icon);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contact_menu, menu);
		
		//enable items available only if logged in
		menu.setGroupEnabled(R.id.groupLoggedIn, sb.isLoggedIn(c.getRosterJid()) );
		
	}

	private long contextMenuItemPosition;
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		ExpandableListContextMenuInfo cmi = (ExpandableListContextMenuInfo) item.getMenuInfo();

		// workaround for issue http://code.google.com/p/android/issues/detail?id=7139
		// item.getMenuInfo() returns null for submenu items
		long pos= (cmi!=null)? cmi.packedPosition : contextMenuItemPosition;
		contextMenuItemPosition = pos;

		final Contact ctc = getContextContact(pos);

		switch (item.getItemId()) {
		case R.id.cmdChat: 
			openChatActivity(ctc);
			return true;
		case R.id.cmdVcard:
			openVCardActivity(ctc);
			return true;
		case R.id.cmdEdit:
			openEditContactActivity(ctc);
			return true;
		case R.id.cmdDelete:
			confirmDeleteContact(ctc);
			return true;
			
		case R.id.cmdSubscrRequestFrom:
			subscription(ctc, Presence.PRESENCE_SUBSCRIBE);
			return true;
		case R.id.cmdSubscrSendTo:
			subscription(ctc, Presence.PRESENCE_SUBSCRIBED);
			return true;
		case R.id.cmdSubscrRemove:
			subscription(ctc, Presence.PRESENCE_UNSUBSCRIBED);
			return true;
			
		default:
			Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
		}

		return super.onContextItemSelected(item);
	}

	private class RosterBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String from = intent.getStringExtra("param");
			
			if (from !=null) {
				for (Contact c: Lime.getInstance().getRoster().getContacts()) {
					if (c.getJid().equals(from)) {
						refreshSingleContact(c);
					}
				}
				
				return;
			}
			refreshVisualContent();
		}
		
	}
	
	void refreshSingleContact(Contact c) {
		ExpandableListView lv = getExpandableListView();

		int firstVisible = lv.getFirstVisiblePosition();
		int lastVisible = lv.getLastVisiblePosition();

		for (int position = firstVisible; position<=lastVisible; position++) {
			try {
				Contact clv = (Contact) lv.getItemAtPosition(position);
				if (c == clv) {
					View cv = lv.getChildAt(position);
					if (cv !=null) { 
						cv.invalidate();
					} else {
						//can't find view for contact, so refresh all
						//TODO: is it real need to refresh all?
						refreshVisualContent();
						return;
					}
				}
			} catch (ClassCastException e) {}
		}

	}
	
	//TODO: update only group if presence update 
	void refreshVisualContent(){
		//TODO: fix update
		updateRosterTitle();
		
		ExpandableListView lv = getExpandableListView(); 
		lv.setVisibility(View.GONE);
		RosterAdapter ra = (RosterAdapter)getExpandableListAdapter();
		lv.invalidate();
		ra.notifyDataSetChanged();
		ra.updateGroupExpandedState(lv);
	
		lv.setVisibility(View.VISIBLE);
	}
	
	RosterBroadcastReceiver br;
	@Override
	protected void onResume() {
		super.onResume();
		
		sb.setBindListener(new XmppServiceBinding.BindListener() {
			@Override
			public void onBindService(XmppService service) {
				updateRosterTitle();
			}
		});
		
		sb.doBindService();
		//update view to actual state
		refreshVisualContent();
		br = new RosterBroadcastReceiver();
		registerReceiver(br, new IntentFilter(Roster.UPDATE_CONTACT));
	}
	
	@Override
	protected void onPause() {
		sb.doUnbindService();
		unregisterReceiver(br);
		super.onPause();
	}
	
	private void openChatActivity(Contact c) {
		Intent openChat =  new Intent(this, ChatActivity.class);
		openChat.putExtra(ChatActivity.MY_JID, c.getRosterJid());
		openChat.putExtra(ChatActivity.TO_JID,   c.getJid());
		startActivity(openChat);
	}

	private void openVCardActivity(Contact c) {
		Intent openVcard =  new Intent(this, VCardActivity.class);
		openVcard.putExtra(VCardActivity.MY_JID, c.getRosterJid());
		openVcard.putExtra(VCardActivity.JID,   c.getJid());
		startActivity(openVcard);
	}
	
	private void openEditContactActivity(Contact c) {
		Intent openEditContact =  new Intent(this, EditContactActivity.class);
		if (c!=null) {
			openEditContact.putExtra(EditContactActivity.MY_JID, c.getRosterJid());
			openEditContact.putExtra(EditContactActivity.JID,   c.getJid());
		} else {
			String rJid = Lime.getInstance().getActiveAccount().userJid;
			openEditContact.putExtra(EditContactActivity.MY_JID, rJid);
			//openEditContact.putExtra(EditContactActivity.JID,   c.getJid());
		}
		startActivity(openEditContact);
	}

	private void confirmDeleteContact(final Contact ctc) {
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
	}

	private void subscription(Contact contact, String subscriptionAction) {
		IqRoster.setSubscription(
				contact.getJid(), 
				subscriptionAction, 
				sb.getXmppStream(contact.getRosterJid()));
	}

	protected void deleteContact(Contact c) {
		IqRoster.deleteContact(
			c.getJid(), 
			sb.getXmppStream(c.getRosterJid())
		);
	}
	
}
