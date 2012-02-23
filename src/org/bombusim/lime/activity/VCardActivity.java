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

import java.security.InvalidParameterException;

import org.bombusim.lime.R;
import org.bombusim.lime.service.XmppServiceBinding;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.util.Log;
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
        if (params == null) throw new InvalidParameterException("No parameters specified for VCardActivity");
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
