package org.bombusim.lime.activity;

import java.security.InvalidParameterException;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.data.Chat;
import org.bombusim.lime.data.Contact;
import org.bombusim.lime.data.Message;
import org.bombusim.lime.service.XmppServiceBinding;
import org.bombusim.xmpp.stanza.XmppMessage;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.format.Time;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

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
	
	private View contactHead;
	
	private XmppServiceBinding serviceBinding;
	
	Contact visavis;
	
	Chat chat;
	int chatSize; //caching chat.getChatSize() to provide atomic updating
	
	protected String visavisNick;
	protected String myNick;
	
	/*
	 * called when android:launchMode="singleTop"
	 * single-chat mode, replaces existing chat;
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);

		attachToChat();
	}
	
	private void attachToChat(){
        Bundle params = getIntent().getExtras();
        if (params == null) throw new InvalidParameterException("No parameters specified for ChatActivity");
        jid = params.getString(TO_JID);
        rJid = params.getString(MY_JID);

        //TODO: move into ChatFactory
        visavis = Lime.getInstance().getRoster().findContact(jid, rJid);
        chat = Lime.getInstance().getChatFactory().getChat(jid, rJid);

        chatListView.setAdapter(new ChatListAdapter(this));

        vAvatar.setImageBitmap(visavis.getLazyAvatar(true));
        vNick.setText(visavis.getScreenName());
        vStatusMessage.setText(visavis.getStatusMessage());
        
        contactHead.requestFocus(); //stealing focus from messageBox
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
        setContentView(R.layout.chat);
        
        
        serviceBinding = new XmppServiceBinding();
  
        contactHead = findViewById(R.id.contact_head);
        vAvatar = (ImageView) findViewById(R.id.rit_photo);        
        vStatus = (ImageView) findViewById(R.id.rit_statusIcon);
        vNick = (TextView) findViewById(R.id.rit_jid);
        vStatusMessage = (TextView) findViewById(R.id.rit_presence);

        messageBox = (EditText) findViewById(R.id.messageBox);
        sendButton = (ImageButton) findViewById(R.id.sendButton);

        chatListView = (ListView) findViewById(R.id.chatListView);

        enableTrackballTraversing();
        
        attachToChat();
        
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

    private void enableTrackballTraversing() {
    	//TODO: http://stackoverflow.com/questions/2679948/focusable-edittext-inside-listview
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
            
            String sender = (m.type == Message.TYPE_MESSAGE_OUT)? myNick : visavisNick ;  
            sv.setText(m.timestamp, sender, m.messageBody, m.type);
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
            
            mMessageBody = new TextView(context);
            
            //TODO: available in API 11
            //mMessageBody.setTextIsSelectable(true);
            
            addView(mMessageBody, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            setExpanded(true);
        }
        
        
        public void setText(long time, String sender, String message, int messageType) {

        	//TODO: smart time formatting
        	// 1 minute ago,
        	// hh:mm (after 1 hour)
        	// etc...
        	
        	tf.set(time);
        	String tm=tf.format("%H:%M ");

        	SpannableStringBuilder ss = new SpannableStringBuilder(tm);

        	int addrEnd=0;
        	
        	if (message.startsWith("/me ")) {
        		message = "*" + sender + message.substring(3);
        	} else {
        		ss.append('<').append(sender).append("> ");
        	}
        	
    		addrEnd = ss.length()-1;

    		int color= Message.getColor(messageType);
       		ss.setSpan(new ForegroundColorSpan(color), 0, addrEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

       		SpannableString msg = new SpannableString(message);
        	
            Linkify.addLinks(msg, Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
            Lime.getInstance().getSmilify().addSmiles(msg);

            ss.append(msg);
            
            mMessageBody.setText(ss);
            mMessageBody.setMovementMethod(LinkMovementMethod.getInstance());
            
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
		
		//avoid sending of empty messages
		if (text.length() == 0) return;
		
		String to = visavis.getJid(); 

		Message out = new Message(Message.TYPE_MESSAGE_OUT, to, text);
		chat.addMessage(out);
		refreshVisualContent();
		
		//TODO: resource magic
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

        visavisNick = visavis.getScreenName();
        //TODO: get my nick
        myNick = "Me"; //serviceBinding.getXmppStream(visavis.getRosterJid()).jid; 

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
