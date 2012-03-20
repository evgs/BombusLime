package org.bombusim.lime.activity;

import java.util.ArrayList;
import java.util.Collections;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.data.Contact;
import org.bombusim.lime.widgets.ContactViewFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ActiveChats {
	
	private Bitmap[] statusIcons;
	

	public void setStatusIcons(Bitmap[] statusIcons) {
		this.statusIcons = statusIcons;
	}
	
	public void showActiveChats(final Activity hostActivity, String rJid) {
		
		final ContactViewFactory cvf = new ContactViewFactory(hostActivity, statusIcons);
		
		View listLayout = View.inflate(hostActivity, R.layout.list_ac, null);
		final ListView acl = (ListView) listLayout.findViewById(R.id.listView);
		
		//ListView acl = new ListView(hostActivity);
		
		final ArrayList<Contact> activeContacts = new ArrayList<Contact>();
		
		// populating active contact list
		ArrayList<Contact> contacts = Lime.getInstance().getRoster().getContacts();
		
		for (Contact c : contacts) {
			if (rJid != null)  
				if (!c.getRosterJid().equals(rJid)) continue;
			
			if (!c.hasActiveChats()) continue;
			
			activeContacts.add(c);
		}
		
		Collections.sort(activeContacts);
		
		acl.setAdapter(new BaseAdapter() {
	        public View getView(int position, View convertView, ViewGroup parent) {
	        	return cvf.getView(convertView, activeContacts.get(position));
	        }


	        public final int getCount() {
	            return activeContacts.size();
	        }

	        public final Object getItem(int position) {
	            return activeContacts.get(position);
	        }

	        public final long getItemId(int position) { return position; }
			
		});
		
		AlertDialog.Builder builder = new AlertDialog.Builder(hostActivity);
		builder.setTitle(R.string.activeChats);
		if (activeContacts.isEmpty()) {
			builder.setMessage(R.string.noActiveChats);
		} else {
			builder.setView(listLayout);
		}
		
		builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
		});
			   
		final AlertDialog alert = builder.create();
		
		acl.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				alert.dismiss();
				
				Contact c = activeContacts.get(position);
				
				((RosterActivity) hostActivity).openChat(c.getJid(), c.getRosterJid());
				
			}

		});

		alert.setOwnerActivity(hostActivity);
		alert.show();
	}

}
