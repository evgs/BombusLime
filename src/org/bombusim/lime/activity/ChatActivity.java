package org.bombusim.lime.activity;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.data.Chat;
import org.bombusim.lime.data.Contact;
import org.bombusim.lime.data.Message;
import org.bombusim.lime.logger.LoggerEvent;
import org.bombusim.lime.service.XmppServiceBinding;
import org.bombusim.xmpp.stanza.XmppMessage;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class ChatActivity extends Activity {
	public static final String MY_JID = "fromJid";
	public static final String TO_JID = "toJid";

	private String jid;
	private String rJid;
	
	private ImageView vAvatar;
	private ImageView vStatus;
	private TextView vNick;
	private TextView vStatusMessage;
	
	private EditText messageBox;
	private ImageButton sendButton;
	
	private ListView chatListView;
	
	private XmppServiceBinding serviceBinding;
	
	Contact visavis;
	
	Chat chat;
	int chatSize; //caching chat.getChatSize() to provide atomic updating
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        
  
        Bundle params = getIntent().getExtras();
        if (params == null) throw new InvalidParameterException("No parameters specified for ChatActivity");
        jid = params.getString(TO_JID);
        rJid = params.getString(MY_JID);
        
        serviceBinding = new XmppServiceBinding();
        
        //TODO: move into ChatFactory
        visavis = Lime.getInstance().getRoster().findContact(jid, rJid);
        chat = Lime.getInstance().getChatFactory().getChat(jid, rJid);
        
        vAvatar = (ImageView) findViewById(R.id.rit_photo);        
        vStatus = (ImageView) findViewById(R.id.rit_statusIcon);
        vNick = (TextView) findViewById(R.id.rit_jid);
        vStatusMessage = (TextView) findViewById(R.id.rit_presence);
        
        messageBox = (EditText) findViewById(R.id.messageBox);
        sendButton = (ImageButton) findViewById(R.id.sendButton);
        		
        vAvatar.setImageBitmap(visavis.getLazyAvatar(true));
        vNick.setText(visavis.getScreenName());
        vStatusMessage.setText(visavis.getStatusMessage());

        chatListView = (ListView) findViewById(R.id.chatListView);
        chatListView.setAdapter(new ChatListAdapter(this));

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

    private class ChatListAdapter extends BaseAdapter {
    	
        public ChatListAdapter(Context context)
        {
            mContext = context;
        }

        
        /**
         * The number of items in the list is determined by the number of speeches
         * in our array.
         * 
         * @see android.widget.ListAdapter#getCount()
         */
        public int getCount() {
            return chatSize;
        }

        /**
         * Since the data comes from an array, just returning
         * the index is sufficent to get at the data. If we
         * were using a more complex data structure, we
         * would return whatever object represents one 
         * row in the list.
         * 
         * @see android.widget.ListAdapter#getItem(int)
         */
        public Object getItem(int position) {
            return position;
        }

        /**
         * Use the array index as a unique id.
         * @see android.widget.ListAdapter#getItemId(int)
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * Make a SpeechView to hold each row.
         * @see android.widget.ListAdapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            MessageView sv;
            Message m=chat.getMessage(position);
            
            if (convertView == null) {
                sv = new MessageView(mContext);
            } else {
                sv = (MessageView)convertView;
            }
                sv.setMessageType(m.type);
                sv.setText(m.timestamp, m.nick, m.messageBody);
                //sv.setExpanded(p.expanded);
            
            return sv;
        }

        /*public void toggle(int position) {
            LoggerEvent p=getLogRecords().get(position);
            if (p.message !=null) {
            	p.expanded = !p.expanded; 
            	notifyDataSetChanged();
            }
        }*/
        
        /**
         * Remember our context so we can use it when constructing views.
         */
        private Context mContext;
        
    }

	// Time formatter
	private Time tf=new Time(Time.getCurrentTimezone());
    
    private class MessageView extends LinearLayout {
        public MessageView(Context context) {
            super(context);
            
            this.setOrientation(VERTICAL);
            
            // Here we build the child views in code. They could also have
            // been specified in an XML file.
            
            mMessageBody = new TextView(context);
            mFrom = new TextView(context);
            mTime = new TextView(context);
            
            mTime.setPadding(0, 0, 6, 0);
            
            LinearLayout s1 = new LinearLayout(context);
            s1.setOrientation(HORIZONTAL);
            s1.addView(mTime, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            s1.addView(mFrom, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            
            addView(s1);
            addView(mMessageBody, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            setExpanded(true);
        }
        
        
        public void setText(long time, String title, String message) {
        	tf.set(time);
        	
        	mTime.setText(tf.format("%H:%M"));
        	
            mFrom.setText(title);
            mMessageBody.setText(message);
		}

        
        public void setMessageType(int type) {
        	int color= Message.getColor(type);
        	mFrom.setTextColor(color);
        	mMessageBody.setTextColor(color);
        }
        /**
         * Convenience method to expand or hide the item
         */
        public void setExpanded(boolean expanded) {
            mMessageBody.setVisibility(expanded ? VISIBLE : GONE);
        }
        
        private TextView mTime;
        private TextView mMessageBody;
        private TextView mFrom;
    }
    
	
	protected void sendMessage() {
		String text = messageBox.getText().toString();
		messageBox.setText("");
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
		
		Message out = new Message(Message.TYPE_MESSAGE_OUT, "Me", text);
		chat.addMessage(out);
		refreshVisualContent();
		
		String to = visavis.getJid(); //TODO: resource
		
		XmppMessage msg = new XmppMessage(to, text, null, false);
		//TODO: message queue
		serviceBinding.getXmppStream(visavis.getRosterJid()).send(msg);
		
	}

	private class ChatBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			refreshVisualContent();
		}
		
	}
	
	private ChatBroadcastReceiver br;
	
	@Override
	protected void onResume() {
		//TODO: refresh message list, focus to last unread
		serviceBinding.doBindService();

		refreshVisualContent();
		br = new ChatBroadcastReceiver();
		//TODO: presence receiver
		registerReceiver(br, new IntentFilter(Chat.UPDATE_CHAT));
		
		super.onResume();
	}
	
	public void refreshVisualContent() {
		
		chatListView.setVisibility(View.GONE);
		chatSize = chat.getChatSize();
		((BaseAdapter)chatListView.getAdapter()).notifyDataSetChanged();
		chatListView.invalidate();

		//Move focus to last message
		chatListView.setSelection(chatSize-1);
		
		chatListView.setVisibility(View.VISIBLE);
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		serviceBinding.doUnbindService();
		unregisterReceiver(br);
	}
}
