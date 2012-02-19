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

package org.bombusim.lime.activity;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.data.PresenceStorage;
import org.bombusim.lime.service.XmppService;
import org.bombusim.lime.service.XmppServiceBinding;
import org.bombusim.xmpp.XmppAccount;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.stanza.XmppPresence;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class PresenceActivity extends Activity {

	//widgets
	View presenceDirect;
	EditText editPresenceTo;
	EditText editPriority;
	Spinner spStatus;
	EditText editMessage;
	Button buttonRecent;
	
	Button buttonOk;
	Button buttonCancel;
	
	private XmppServiceBinding sb;
	
	String rJid;
	String to;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		rJid = getIntent().getStringExtra("RJID");
		to = getIntent().getStringExtra("JID");
		
		setContentView(R.layout.presence);

		
		presenceDirect = (View)     findViewById(R.id.presenceDirect);
		editPresenceTo = (EditText) findViewById(R.id.presenceToJid);
		editPriority   = (EditText) findViewById(R.id.priority);
		editMessage    = (EditText) findViewById(R.id.presenceMessage);
		spStatus       = (Spinner)  findViewById(R.id.presenceStatus);
		buttonRecent   = (Button)   findViewById(R.id.recentPresences);
		buttonOk       = (Button)   findViewById(R.id.buttonOk);
		buttonCancel   = (Button)   findViewById(R.id.buttonCancel);
		
		if (to !=null) {
			presenceDirect.setVisibility(View.VISIBLE);
			//TODO: set "to" in form "name <jid@server.tld>
			editPresenceTo.setText(to);
		}
		
		buttonOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { 
				doSetStatus();
				finish(); 
			}
		});
		
		buttonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { finish(); }
		});

		
		spStatus.setAdapter(new StatusSpinnerAdapter(this));
		
		sb = new XmppServiceBinding(this);
		
		
		if (savedInstanceState != null) {
			setStatus  ( savedInstanceState.getInt("status") );
			setMessage ( savedInstanceState.getString("message") );
			setPriority( savedInstanceState.getInt("priority") );
		} else {
			//load data to form
			//TODO: fix multiaccount logic

			PresenceStorage ps = new PresenceStorage(this);
			
			setMessage( ps.getMessage() );
			setPriority( ps.getPriority() );
			setStatus( ps.getStatus() );
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt("status", getStatus());
		outState.putString("message", getMessage());
		outState.putInt("priority", getPriority());
	}
	
	protected void doSetStatus() {

		//TODO: send direct presence
		
		sendBroadcastPresence(
				getStatus(), 
				getMessage(), 
				getPriority()
			);
	}

	private void sendBroadcastPresence(int status, String message, int priority) {
		PresenceStorage ps = new PresenceStorage(this);
		ps.setPresence(status, message, priority);
		
		if (status == XmppPresence.PRESENCE_OFFLINE) {
			sb.doDisconnect();
			Lime.getInstance().vcardResolver.restartQueue();
		} else {
			startService(new Intent(getBaseContext(), XmppService.class));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		sb.doBindService();
	}
	
	private void setStatus(int status) {
		StatusSpinnerAdapter adapter = (StatusSpinnerAdapter) spStatus.getAdapter();

		for (int i=0; i<adapter.getCount(); i++) {
			if (adapter.getItemId(i) == status) {
				spStatus.setSelection(i);
				break;
			}
		}
	}

	private void setPriority(int priority) {
		if (priority != XmppAccount.DEFAULT_PRIORITY_ACCOUNT) {
			String sPriority = String.valueOf(priority);
			editPriority.setText(sPriority);
		}
	}

	private void setMessage(String message) { editMessage.setText(message); }

	private int getStatus() { 	return (int) spStatus.getSelectedItemId(); 	}

	private String getMessage() {  return editMessage.getText().toString(); }
	
	private int getPriority() {
		try {
			return Integer.parseInt( editPriority.getText().toString().trim() );
		} catch (Exception e) {};

		return XmppAccount.DEFAULT_PRIORITY_ACCOUNT;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		sb.doUnbindService();
	}

	private class StatusSpinnerAdapter extends BaseAdapter {
		private Context context;
		
		private TypedArray statusIcons;
		private TypedArray statusNames;
		private TypedArray statusIndexes;
		
		private LayoutInflater mInflater;
		
		public StatusSpinnerAdapter(Context context) {
			statusIcons   = context.getResources().obtainTypedArray(R.array.statusIcons);
			statusIndexes = context.getResources().obtainTypedArray(R.array.presenceIndexArray);
			statusNames   = context.getResources().obtainTypedArray(R.array.presenceArray);
			
			mInflater = LayoutInflater.from(context);

		}
		
		@Override
		public int getCount() {
			return statusNames.length();
		}

		@Override
		public Object getItem(int position) { return statusNames.getString(position);	}

		@Override
		public long getItemId(int position) { return statusIndexes.getInt(position, 0); }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	        return getCustomView(position, convertView, R.layout.status_spinner);
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
	        return getCustomView(position, convertView, R.layout.status_item);
		}

		private View getCustomView(int position, View convertView,
				int itemLayout) {
			ViewHolder holder;

	        if (convertView == null) {
	            convertView = mInflater.inflate(itemLayout, null);

	            // Creates a ViewHolder and store references to the two children views
	            // we want to bind data to.
	            holder = new ViewHolder();
	            holder.icon = (ImageView) convertView.findViewById(R.id.iconStatus);
	            holder.presence = (TextView) convertView.findViewById(R.id.presence);

	            convertView.setTag(holder);
	        } else {
	            // Get the ViewHolder back to get fast access to the TextView
	            // and the ImageView.
	            holder = (ViewHolder) convertView.getTag();
	        }
	        
	        int iconIndex = statusIndexes.getInt(position, 0);
	        
	        holder.presence.setText(statusNames.getText(position));
	        holder.icon.setImageDrawable(statusIcons.getDrawable(iconIndex));
			
			return convertView;
		}
		
	}
	
    static class ViewHolder {
        ImageView icon;
        TextView presence;
    }
}
