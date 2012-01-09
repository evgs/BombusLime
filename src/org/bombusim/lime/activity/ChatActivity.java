package org.bombusim.lime.activity;

import java.security.InvalidParameterException;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.data.Chat;
import org.bombusim.lime.data.Contact;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatActivity extends Activity {
	public static final String FROM_JID = "fromJid";
	public static final String TO_JID = "toJid";

	private String jid;
	private String rJid;
	
	private ImageView vAvatar;
	private ImageView vStatus;
	private TextView vNick;
	private TextView vStatusMessage;
	
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
        
        
        vAvatar.setImageBitmap(visavis.getLazyAvatar(true));
        vNick.setText(visavis.getScreenName());
        vStatusMessage.setText(visavis.getStatusMessage());
	}

}
