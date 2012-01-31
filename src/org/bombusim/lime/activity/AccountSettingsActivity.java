package org.bombusim.lime.activity;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.data.AccountsFactory;
import org.bombusim.xmpp.XmppAccount;
import org.bombusim.xmpp.XmppJid;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
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
	
	CheckBox checkSspecificHostPort;
	CheckBox checkZlib;
	CheckBox checkAutologin;
	
	Spinner spinSecurity;
	Spinner spinPlainPassword;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//TODO: account selector
		XmppAccount account = Lime.getInstance().accounts.get(0);
		
		setContentView(R.layout.account_settings);
		
		editJid  = ((EditText)findViewById(R.id.jid));
		editJid.setText(account.userJid);
		
		editPass = ((EditText)findViewById(R.id.password));
		editPass.setText(account.password);
		//TODO: non-empty password
		//pass.setHint("••••••••");
		
		editResource = ((EditText)findViewById(R.id.resource));
		editResource.setText(account.resource);
		
		editPriority = ((EditText)findViewById(R.id.priority));
		editPriority.setText(String.valueOf(account.priority));

		checkAutologin = ((CheckBox)findViewById(R.id.autoLogin));
		checkAutologin.setChecked(account.autoLogin);

		editXmppHost = ((EditText)findViewById(R.id.xmpphost));
		editXmppHost.setText(account.xmppHost);

		editXmppPort = ((EditText)findViewById(R.id.xmppport));
		editXmppPort.setText(String.valueOf(account.xmppPort));
		
		checkSspecificHostPort = ((CheckBox)findViewById(R.id.specificHostPort));
		checkSspecificHostPort.setChecked(account.specificHostPort);
		
		checkZlib = ((CheckBox)findViewById(R.id.zlib));
		checkZlib.setChecked(account.trafficCompression);
		
		//TODO: populate in runtime to ensure correct order;
		spinSecurity = ((Spinner)findViewById(R.id.ssl));
		spinSecurity.setSelection(account.secureConnection);
		
		//TODO: populate in runtime to ensure correct order;
		spinPlainPassword = ((Spinner)findViewById(R.id.plainpassword));
		spinPlainPassword.setSelection(account.enablePlainAuth);

		
		findViewById(R.id.advancedSettings).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				findViewById(R.id.advancedSettings).setVisibility(View.GONE);
				findViewById(R.id.layoutAdvancedSettings).setVisibility(View.VISIBLE);
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

		
		findViewById(R.id.layoutAdvancedSettings).setVisibility(View.GONE);
		
		updateHostState();
	}
	
	void updateHostState() {
		int visibility = ((CheckBox)findViewById(R.id.specificHostPort)).isChecked()? View.VISIBLE : View.GONE;
		findViewById(R.id.linearLayotHost).setVisibility(visibility);
	}
	
	@Override
	protected void onPause() {
		// TODO add save/cancel buttons into form
		// TODO check saveAccount() result
		saveAccount();
		super.onPause();
	}
	boolean saveAccount() {
		XmppAccount account = Lime.getInstance().accounts.get(0);

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
		
		account.specificHostPort = checkSspecificHostPort.isChecked();
		
		account.trafficCompression = checkZlib.isChecked();
		
		//TODO: populate in runtime to ensure correct order;
		account.secureConnection = spinSecurity.getSelectedItemPosition();
		
		//TODO: populate in runtime to ensure correct order;
		account.enablePlainAuth = spinPlainPassword.getSelectedItemPosition();

		AccountsFactory.saveAccount(getApplicationContext(), account);
		
		return true;
	}

}
