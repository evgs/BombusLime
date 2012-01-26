package org.bombusim.lime.activity;

import java.security.InvalidParameterException;

import org.bombusim.lime.R;
import org.bombusim.lime.service.XmppServiceBinding;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class VCardActivity extends ListActivity {
	public static final String JID = "jid";
	public static final String MY_JID = "fromJid";
	
	private String jid;
	private String rJid;
	
	private XmppServiceBinding serviceBinding;
	
	private ProgressDialog pgsDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
        Bundle params = getIntent().getExtras();
        if (params == null) throw new InvalidParameterException("No parameters specified for ChatActivity");
        jid = params.getString(JID);
        rJid = params.getString(MY_JID);

        serviceBinding = new XmppServiceBinding(this);
        
        setListAdapter(new VCardAdapter(this));

        pgsDialog = ProgressDialog.show(this, "", 
        		getString(R.string.loadingVcardProgress), true, true, new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						//Cancelling VCard fetching 
						onBackPressed();
					}
				});  
	}
	
	@Override
	protected void onResume() {
		serviceBinding.doBindService();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		serviceBinding.doUnbindService();
		super.onPause();
	}
	
	private class VCardAdapter extends BaseAdapter {
		
		private Context context;

		public VCardAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
