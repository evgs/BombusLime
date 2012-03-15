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
import org.bombusim.lime.data.AccountsFactory;
import org.bombusim.lime.service.XmppService;
import org.bombusim.lime.service.XmppServiceBinding;
import org.bombusim.lime.widgets.OkCancelBar;
import org.bombusim.xmpp.XmppAccount;
import org.bombusim.xmpp.XmppJid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class AccountSettingsActivity extends Activity {

	EditText editJid;
	EditText editPass;
	EditText editResource;
	EditText editPriority;
	EditText editXmppHost;
	EditText editXmppPort;
	
	CheckBox checkSpecificHostPort;
	CheckBox checkZlib;
	CheckBox checkAutologin;
	
	Spinner spinSecurity;
	Spinner spinPlainPassword;
	
	OkCancelBar mOkCancel;
	
	boolean mAdvancedSettings = false;
	
	private XmppServiceBinding sb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.account_settings);
		
		editJid  = ((EditText)findViewById(R.id.jid));
		editPass = ((EditText)findViewById(R.id.password));
		editResource = ((EditText)findViewById(R.id.resource));
		editPriority = ((EditText)findViewById(R.id.priority));
		checkAutologin = ((CheckBox)findViewById(R.id.autoLogin));
		editXmppHost = ((EditText)findViewById(R.id.xmpphost));
		editXmppPort = ((EditText)findViewById(R.id.xmppport));
		checkSpecificHostPort = ((CheckBox)findViewById(R.id.specificHostPort));
		spinSecurity = ((Spinner)findViewById(R.id.ssl));
		spinPlainPassword = ((Spinner)findViewById(R.id.plainpassword));
        checkZlib = ((CheckBox)findViewById(R.id.zlib));

		mOkCancel = (OkCancelBar) findViewById(R.id.okCancel);

        showAdvancedSettings(false);

        if (savedInstanceState!=null) {
		    loadInstanceState(savedInstanceState);
		} else {
		    loadActiveAccount();
		}

		
		findViewById(R.id.advancedSettings).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			    showAdvancedSettings(true); 
			}
		});
		
		
		findViewById(R.id.showPassword).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int inputtype = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
				
				if (((CheckBox)v).isChecked())
					inputtype |= InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
				else
					inputtype |= InputType.TYPE_TEXT_VARIATION_PASSWORD;

				editPass.setInputType(inputtype);
			}
		});


		findViewById(R.id.specificHostPort).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateHostState();
			}
		});

		mOkCancel.setOnButtonActionListener(new OkCancelBar.OnButtonActionListener() {
            
            @Override
            public void onPositive() {
                sb.doDisconnect();
                
                if (!saveAccount()) return;
                
                //TODO: invalidate roster
                Lime.getInstance().loadAccounts();
                
                finish();
            }
            
            @Override
            public void onNegative() { onBackPressed(); }
        });
		
		updateHostState();
		
		sb=new XmppServiceBinding(this);
	}

	private void showAdvancedSettings(boolean show) {
	    mAdvancedSettings = show;
	    
        findViewById(R.id.advancedSettings).setVisibility((show)? View.GONE : View.VISIBLE);
        findViewById(R.id.layoutAdvancedSettings).setVisibility((show)? View.VISIBLE : View.GONE);
	}
	
	private void loadActiveAccount() {
		//TODO: account selector
		XmppAccount account = Lime.getInstance().getActiveAccount();

		editJid.setText(account.userJid);
		
		editPass.setText(account.password);
		//TODO: non-empty password
		//pass.setHint("••••••••");
		
		editResource.setText(account.resource);
		editPriority.setText(String.valueOf(account.priority));
		checkAutologin.setChecked(account.autoLogin);
		editXmppHost.setText(account.xmppHost);
		editXmppPort.setText(String.valueOf(account.xmppPort));
		checkSpecificHostPort.setChecked(account.specificHostPort);
		checkZlib.setChecked(account.trafficCompression);
		//TODO: populate in runtime to ensure correct order;
		spinSecurity.setSelection(account.secureConnection);
		//TODO: populate in runtime to ensure correct order;
		spinPlainPassword.setSelection(account.enablePlainAuth);
	}
	
	void updateHostState() {
		int visibility = ((CheckBox)findViewById(R.id.specificHostPort)).isChecked()? View.VISIBLE : View.GONE;
		findViewById(R.id.linearLayotHost).setVisibility(visibility);
	}
	
	@Override
	protected void onResume() {
		sb.doBindService();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
        super.onPause();
		sb.doUnbindService();
	}
	
	@Override
	public void onBackPressed() {
        if (Lime.getInstance().getActiveAccount()._id == -1)
            Lime.getInstance().deleteActiveAccount();
        finish(); 
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    outState.putString("jid",        editJid.getText().toString());
	    outState.putString("password",   editPass.getText().toString());
        outState.putString("resource",   editResource.getText().toString());
        outState.putString("priority",   editPriority.getText().toString());
        outState.putString("host",       editXmppHost.getText().toString());
        outState.putString("port",       editXmppPort.getText().toString());
        outState.putBoolean("autologin", checkAutologin.isChecked());
        outState.putBoolean("hostport",  checkSpecificHostPort.isChecked());
        outState.putBoolean("zlib",      checkZlib.isChecked());
        outState.putInt("ssl",           spinSecurity.getSelectedItemPosition());
        outState.putInt("plain",         spinPlainPassword.getSelectedItemPosition());
        
        outState.putBoolean("adv", mAdvancedSettings);
	}

    private void loadInstanceState(Bundle inState) {
        editJid              .setText(inState.getString("jid"));
        editPass             .setText(inState.getString("password"));
        editResource         .setText(inState.getString("resource"));
        editPriority         .setText(inState.getString("priority"));
        editXmppHost         .setText(inState.getString("host"));
        editXmppPort         .setText(inState.getString("port"));
        checkAutologin       .setChecked(inState.getBoolean("autologin"));
        checkSpecificHostPort.setChecked(inState.getBoolean("hostport"));
        checkZlib            .setChecked(inState.getBoolean("zlib"));
        spinSecurity         .setSelection(inState.getInt("ssl"));
        spinPlainPassword    .setSelection(inState.getInt("plain"));
        
        showAdvancedSettings( inState.getBoolean("adv") );
    }
	
	boolean saveAccount() {
		XmppAccount account = Lime.getInstance().getActiveAccount();

		XmppJid jid = new XmppJid(editJid.getText().toString());
		if (!jid.isValid()) return false; //not saved
		
		account.userJid = jid.getBareJid();
		
		account.password = editPass.getText().toString();
		//TODO: non-empty password
		//pass.setHint("••••••••");

		account.resource = editResource.getText().toString();
		
		try {
			account.priority = Integer.parseInt(editPriority.getText().toString());
		} catch (NumberFormatException ex) {
			account.priority = XmppAccount.DEFAULT_PRIORITY;
		}

		account.autoLogin = checkAutologin.isChecked();
		
		account.xmppHost = editXmppHost.getText().toString();
		try {
			account.xmppPort = Integer.parseInt(editXmppPort.getText().toString());
		} catch (NumberFormatException ex) {
			account.xmppPort = XmppAccount.DEFAULT_XMPP_PORT;
		}
		
		account.specificHostPort = checkSpecificHostPort.isChecked();
		
		account.trafficCompression = checkZlib.isChecked();
		
		//TODO: populate in runtime to ensure correct order;
		account.secureConnection = spinSecurity.getSelectedItemPosition();
		
		//TODO: populate in runtime to ensure correct order;
		account.enablePlainAuth = spinPlainPassword.getSelectedItemPosition();

		AccountsFactory.saveAccount(getApplicationContext(), account);
		
		return true;
	}

	
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		getMenuInflater().inflate(R.menu.account_menu, menu);
	
		return true;
	};

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.cmdAdd:    {
			sb.doDisconnect();
			
			showAdvancedSettings(false);
			
			saveAccount();
			Lime.getInstance().addNewAccount();
			loadActiveAccount();
			break;
		}
		case R.id.cmdDelete:   {
			sb.doDisconnect();
			
			Lime.getInstance().deleteActiveAccount();
			loadActiveAccount();
			break;
		}
		
		case R.id.cmdSelectActive:   {
			String accountLabels[]=Lime.getInstance().getAccountLabels();
			int activeAccount = Lime.getInstance().getActiveAccountIndex();
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Select active account")
			.setSingleChoiceItems(accountLabels, activeAccount, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					sb.doDisconnect();
				    
					Lime.getInstance().setActiveAccountIndex(which);
					
	                Lime.getInstance().loadAccounts();

					loadActiveAccount();
					
					dialog.dismiss();
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(this);
			alert.show();
			
			break;
		}

		default: return true; // on submenu
		}
		
		return false;
	}
	
}
