package org.bombusim.lime.activity;

import java.security.InvalidParameterException;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.data.Chat;
import org.bombusim.lime.data.Contact;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class ChatActivity extends Activity {
	public static final String FROM_JID = "fromJid";
	public static final String TO_JID = "toJid";

	private String jid;
	private String rJid;
	
	private ImageView vAvatar;
	private ImageView vStatus;
	private TextView vNick;
	private TextView vStatusMessage;
	
	private EditText messageBox;
	private ImageButton sendButton;
	
	Contact visavis;
	
	Chat chat;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        
  
        Bundle params = getIntent().getExtras();
        if (params == null) throw new InvalidParameterException("No parameters specified for ChatActivity");
        jid = params.getString(TO_JID);
        rJid = params.getString(FROM_JID);
        
        //TODO: move into ChatFactory
        visavis = Lime.getInstance().getRoster().findContact(jid, rJid);
        chat = new Chat(visavis);
        
        vAvatar = (ImageView) findViewById(R.id.rit_photo);        
        vStatus = (ImageView) findViewById(R.id.rit_statusIcon);
        vNick = (TextView) findViewById(R.id.rit_jid);
        vStatusMessage = (TextView) findViewById(R.id.rit_presence);
        
        messageBox = (EditText) findViewById(R.id.messageBox);
        sendButton = (ImageButton) findViewById(R.id.sendButton);
        		
        vAvatar.setImageBitmap(visavis.getLazyAvatar(true));
        vNick.setText(visavis.getScreenName());
        vStatusMessage.setText(visavis.getStatusMessage());
        
        sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { 	sendMessage(); 	}
		});

        //TODO: optional
        messageBox.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() != KeyEvent.ACTION_DOWN) return false; //filtering only KEY_DOWN
				if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
				//if (event.isShiftPressed()) return false; //typing multiline messages with SHIFT+ENTER
				sendMessage();
				return true; //Key was processed
			}
		});
        
        //TODO: localize
        //TODO: optional behavior
        //messageBox.setImeActionLabel("Send", EditorInfo.IME_ACTION_SEND); //Keeps IME opened
        messageBox.setImeActionLabel("Send", EditorInfo.IME_ACTION_DONE); //Closes IME
        
        messageBox.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				switch (actionId) {
				case EditorInfo.IME_ACTION_SEND:
					sendMessage();
					return true;
				case EditorInfo.IME_ACTION_DONE:
					sendMessage();
					return false; //let IME to be closed
				}
				return false;
			}
		});
	}

	protected void sendMessage() {
		//TODO: send xmpp message
		String text = messageBox.getText().toString();
		messageBox.setText("");
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

}
