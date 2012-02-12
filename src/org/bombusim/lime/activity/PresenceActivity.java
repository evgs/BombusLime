package org.bombusim.lime.activity;

import org.bombusim.lime.R;
import org.bombusim.lime.activity.RosterAdapter.ViewHolder;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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
		
		if (to !=null) {
			presenceDirect.setVisibility(View.VISIBLE);
			//TODO: set "to" in form "name <jid@server.tld>
			editPresenceTo.setText(to);
		}
		
		spStatus.setAdapter(new StatusAdapter(this));
		
	}
	
	private class StatusAdapter extends BaseAdapter {
		private Context context;
		
		private TypedArray statusIcons;
		private TypedArray statusNames;
		private TypedArray statusIndexes;
		
		private LayoutInflater mInflater;
		
		public StatusAdapter(Context context) {
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
		public long getItemId(int position) { return position; }

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
