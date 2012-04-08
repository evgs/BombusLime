package org.bombusim.lime.activity;

import java.util.ArrayList;
import java.util.Collections;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.data.Contact;
import org.bombusim.lime.data.Resource;
import org.bombusim.lime.data.Roster;
import org.bombusim.lime.widgets.ContactViewFactory;
import org.bombusim.lime.widgets.ResourceViewFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
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

public class ContactResourceSwitcher {
	
	private Bitmap[] statusIcons;
	

	public void showResources(final Activity hostActivity, Contact contact) {
	    Resources r = hostActivity.getResources();
        statusIcons = new Bitmap[] { 
                BitmapFactory.decodeResource(r, R.drawable.status_offline),
                BitmapFactory.decodeResource(r, R.drawable.status_online),
                BitmapFactory.decodeResource(r, R.drawable.status_chat),
                BitmapFactory.decodeResource(r, R.drawable.status_away),
                BitmapFactory.decodeResource(r, R.drawable.status_xa),
                BitmapFactory.decodeResource(r, R.drawable.status_dnd),
                BitmapFactory.decodeResource(r, R.drawable.status_ask),
                BitmapFactory.decodeResource(r, R.drawable.status_unknown),
                BitmapFactory.decodeResource(r, R.drawable.status_invisible)
                };
		
		final ResourceViewFactory rvf = new ResourceViewFactory(hostActivity, statusIcons);
		
		View listLayout = View.inflate(hostActivity, R.layout.list_ac, null);
		final ListView reslist = (ListView) listLayout.findViewById(R.id.listView);
		
		final ArrayList<Resource> resources = contact.getResources();
		
		Collections.sort(resources);
		
		reslist.setAdapter(new BaseAdapter() {
	        public View getView(int position, View convertView, ViewGroup parent) {
	        	return rvf.getView(convertView, resources.get(position));
	        }


	        public final int getCount() {
	            return resources.size();
	        }

	        public final Object getItem(int position) {
	            return resources.get(position);
	        }

	        public final long getItemId(int position) { return position; }
			
		});
		
		AlertDialog.Builder builder = new AlertDialog.Builder(hostActivity);
		
		//TODO: title
		//builder.setTitle(R.string.activeChats);
		
		if (resources.isEmpty()) {
			builder.setMessage(R.string.presence_offline);
		} else {
			builder.setView(listLayout);
		}
		
		builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
		});
			   
		final AlertDialog alert = builder.create();
		
		reslist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				alert.dismiss();
				
				/*Contact c = activeContacts.get(position);
				
		        //TODO: make choice based on screen resolution, not only orientation
		        int orientation = hostActivity.getResources().getConfiguration().orientation;
		        
		        Class<? extends Activity> targetClass = 
		                ( orientation == Configuration.ORIENTATION_LANDSCAPE)?
		                RosterActivity.class : ChatActivity.class;
				
		        Intent openChat =  new Intent("Chat", null, hostActivity, targetClass);
		        openChat.putExtra(ChatActivity.MY_JID, c.getRosterJid());
		        openChat.putExtra(ChatActivity.TO_JID, c.getJid());

		        hostActivity.startActivity(openChat);
		        */
			}

		});

		alert.setOwnerActivity(hostActivity);
		alert.show();
	}

}
