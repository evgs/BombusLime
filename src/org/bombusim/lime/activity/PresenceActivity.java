package org.bombusim.lime.activity;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.activity.RosterAdapter.ViewHolder;
import org.bombusim.lime.service.XmppService;
import org.bombusim.lime.service.XmppServiceBinding;
import org.bombusim.xmpp.XmppAccount;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.stanza.Presence;

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
	String rJid;
	String to;
	
	View presenceDirect;
	EditText editPresenceTo;
	EditText editPriority;
	Spinner spStatus;
	EditText editMessage;
	Button buttonRecent;
	
	Button buttonOk;
	Button buttonCancel;
	
	private XmppServiceBinding sb;
	
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
			public void onClick(View v) { setStatus(); finish(); }
		});
		
		buttonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { finish(); }
		});

		
		spStatus.setAdapter(new StatusSpinnerAdapter(this));
		
		sb = new XmppServiceBinding(this);
	}
	
	protected void setStatus() {
		
		int status = (int) spStatus.getSelectedItemId();
		
		String message = editMessage.getText().toString();
		
		int priority = XmppAccount.DEFAULT_PRIORITY_ACCOUNT;
		
		try {
			priority = Integer.parseInt( editPriority.getText().toString().trim() );
		} catch (Exception e) {};
		
		//TODO: send direct presence
		sendBroadcastPresence(status, message, priority);
	}

	private void sendBroadcastPresence(int status, String message, int priority) {
		sb.getXmppService().setCommonPresence(status, message, priority);
		
		if (status == Presence.PRESENCE_OFFLINE) {
			sb.doDisconnect();
			Lime.getInstance().vcardResolver.restartQueue();
		} else {
			startService(new Intent(getBaseContext(), XmppService.class));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		sb.setBindListener(new XmppServiceBinding.BindListener() {
			
			@Override
			public void onBindService(XmppService service) {
				updateFormFields();
			}
		});
		
		sb.doBindService();
	}
	
	protected void updateFormFields() {
		
		//TODO: create storage class instead of this hack
		int status = sb.getXmppService().getStatus();
		String message = sb.getXmppService().getStatusMessage();
		int priority = sb.getXmppService().getPriority();
		
		//TODO: fix multiaccount logic
		
		if (rJid != null) {
			XmppStream s = sb.getXmppStream(rJid);
			
			if (s!=null) {
				status = s.getStatus();
				
				message = s.getStatusMessage();
				
				priority = s.getPriority();
			}
		}
		
		editMessage.setText(message);

		if (priority != XmppAccount.DEFAULT_PRIORITY) {
			String sPriority = String.valueOf(priority);
			editPriority.setText(sPriority);
		}

		StatusSpinnerAdapter adapter = (StatusSpinnerAdapter) spStatus.getAdapter();
		
		for (int i=0; i<adapter.getCount(); i++) {
			if (adapter.getItemId(i) == status) {
				spStatus.setSelection(i);
				break;
			}
		}
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
