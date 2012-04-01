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

package org.bombusim.lime.fragments;

import java.security.InvalidParameterException;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.activity.ActiveChats;
import org.bombusim.lime.data.Chat;
import org.bombusim.lime.data.ChatHistoryDbAdapter;
import org.bombusim.lime.data.Contact;
import org.bombusim.lime.data.Message;
import org.bombusim.lime.data.Roster;
import org.bombusim.lime.data.SimpleCursorLoader;
import org.bombusim.lime.service.XmppService;
import org.bombusim.lime.service.XmppServiceBinding;
import org.bombusim.lime.widgets.ChatEditText;
import org.bombusim.lime.widgets.ContactBar;
import org.bombusim.xmpp.handlers.ChatStates;
import org.bombusim.xmpp.handlers.MessageDispatcher;
import org.bombusim.xmpp.stanza.XmppPresence;
import org.bombusim.xmpp.stanza.XmppMessage;

import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.format.Time;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class ChatFragment extends SherlockFragment 
        implements LoaderCallbacks<Cursor> {
    
	private static final int CHAT_LOADER_ID = 0;
	
    private String jid;
	private String rJid;
	
	private ChatEditText mMessageBox;
	private ImageButton mSendButton;
    private ImageButton mSmileButton;
	
	private ListView chatListView;
	
	private ContactBar contactBar;
	
    private View mChatActive;
    private View mChatInactive;
	
	private XmppServiceBinding serviceBinding;
	
	Contact visavis;
	
	private static Chat mChat;
	
	String sentChatState;
	
	private CursorAdapter mCursorAdapter;
	
	protected String visavisNick;
	protected String myNick;

    public interface ChatFragmentListener {
        public void closeChatFragment();
        
        public boolean isTabMode();
    }

    
    @Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    
        if (!(activity instanceof ChatFragmentListener)) 
            throw new ClassCastException(activity.toString() + " must implement ChatFragmentListener");
        
        serviceBinding = new XmppServiceBinding(activity);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //TODO: remove when ActionBar will be implemented
        if (!getChatFragmentListener().isTabMode())
            setHasOptionsMenu(true);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
	    View v = inflater.inflate(R.layout.chat, container,  false);
	    
        contactBar =   (ContactBar)   v.findViewById(R.id.contact_head);
        mMessageBox =   (ChatEditText) v.findViewById(R.id.messageBox);
        mSendButton =   (ImageButton)  v.findViewById(R.id.sendButton);
        mSmileButton =  (ImageButton)  v.findViewById(R.id.smileButton);
        chatListView = (ListView)     v.findViewById(R.id.chatListView);
        
        mChatActive = v.findViewById(R.id.chatActive);
        mChatInactive = v.findViewById(R.id.chatInactive);

        if (!getChatFragmentListener().isTabMode()) {
            contactBar.setVisibility(View.GONE);

            View abCustomView = getSherlockActivity()
                    .getSupportActionBar().getCustomView();
            
            contactBar = (ContactBar) abCustomView.findViewById(R.id.contactHeadActionbar);
            contactBar.removeBackground();
        }
        
        
        registerForContextMenu(chatListView);
        enableTrackballTraversing();
        
        mSendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {   sendMessage();  }
        });
        
        mSmileButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) { mMessageBox.showAddSmileDialog(); }
        });

        //TODO: optional
        mMessageBox.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() != KeyEvent.ACTION_DOWN) return false; //filtering only KEY_DOWN
                
                if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
                //if (event.isShiftPressed()) return false; //typing multiline messages with SHIFT+ENTER
                sendMessage();
                return true; //Key was processed
            }
        });

        mMessageBox.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>0)
                    sendChatState(ChatStates.COMPOSING);
            }
        });
        
        //TODO: optional behavior
        //messageBox.setImeActionLabel("Send", EditorInfo.IME_ACTION_SEND); //Keeps IME opened
        mMessageBox.setImeActionLabel(getString(R.string.sendMessage), EditorInfo.IME_ACTION_DONE); //Closes IME
        
        
        mMessageBox.setOnEditorActionListener(new OnEditorActionListener() {
            
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

	    return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    //TODO: set empty view until chat will be attached
	    
	    mCursorAdapter = new ChatListAdapter(getActivity(), null);
	    chatListView.setAdapter(mCursorAdapter);
	    
        if (jid==null) mChat = null;
        getLoaderManager().initLoader(CHAT_LOADER_ID, null, this);
	}
	
    public void attachToChat(String jid, String rJid) {
        
        this.jid=jid;
        this.rJid=rJid;
        
        //TODO: remove workaround after splash screen will be created
        //disable message box until actual chat selected
        mMessageBox.setEnabled( jid != null );
        
        if (jid == null || rJid ==null) {
            showChatActive(false);
            return; 
        }
        //throw new InvalidParameterException("No parameters specified for ChatActivity");

        //TODO: move into ChatFactory
        visavis = Lime.getInstance().getRoster().findContact(jid, rJid);
        
        if (visavis == null) {
            showChatActive(false);
            return;
        }
        showChatActive(true);

        mChat = Lime.getInstance().getChatFactory().getChat(jid, rJid);

        updateContactBar();
        
        visavisNick = visavis.getScreenName();
        //TODO: get my nick
        myNick = "Me"; //serviceBinding.getXmppStream(visavis.getRosterJid()).jid; 

        refreshVisualContent();
        
        String s = mChat.getSuspendedText();
        if (s!=null) {
            mMessageBox.setText(s);
        }
	}

	private void showChatActive(boolean active) {
	    if (active) {
	        mChatInactive.setVisibility(View.GONE);
	        mChatActive.setVisibility(View.VISIBLE);
	    } else {
            mChatActive.setVisibility(View.GONE);
            mChatInactive.setVisibility(View.VISIBLE);
	    }
    }

    private void updateContactBar() {
	    
	    contactBar.bindContact(visavis, mChat.isComposing());
		
	}
	
	private ChatFragmentListener getChatFragmentListener() {
	    return (ChatFragmentListener) getActivity();
	}
	
	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
        inflater.inflate(R.menu.chat_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case R.id.closeChat:
		    
			Lime.getInstance().getChatFactory().resetActiveState(mChat);
			getChatFragmentListener().closeChatFragment(); //finish activity if single mode chat
			break;
		
		case R.id.addSmile:   mMessageBox.showAddSmileDialog();  break;
		
		case R.id.addMe:      mMessageBox.addMe(); break;
		
        case R.id.cmdChat:  {
            ActiveChats chats = new ActiveChats();
            chats.showActiveChats(getActivity(), null);
            
            break;
        }
		
		default: return true; // on submenu
		}
		
		return false;
	}
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		
		super.onCreateContextMenu(menu, v, menuInfo);
		
		menu.setHeaderTitle(R.string.messageMenuTitle);
		
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.message_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.cmdCopy:
			try {
				String s = ((MessageView)(info.targetView)).toString();

				// Gets a handle to the clipboard service.
				ClipboardManager clipboard = (ClipboardManager)
				        getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
			
				// Set the clipboard's primary clip.
				clipboard.setText(s);
				
			} catch (Exception e) {}
			return true;
			
		case R.id.cmdDelete:

			chatListView.setVisibility(View.GONE);
			mChat.removeFromHistory(info.id);
			refreshVisualContent();
			
			return true;

		default:
			return super.onContextItemSelected(item);
	  }
	}	
	
	
    private void enableTrackballTraversing() {
    	//TODO: http://stackoverflow.com/questions/2679948/focusable-edittext-inside-listview
    	chatListView.setItemsCanFocus(true);
	}

	private class ChatListAdapter extends CursorAdapter {
    	
        
        public ChatListAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			MessageView sv = (MessageView) view;
			
			// TODO Auto-generated method stub
			Message m = ChatHistoryDbAdapter.getMessageFromCursor(cursor);
			
            String sender = (m.type == Message.TYPE_MESSAGE_OUT)? myNick : visavisNick ;  
            sv.setText(m.timestamp, sender, m.messageBody, m.type);
            sv.setUnread(m.unread);

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = new MessageView(context);
			bindView(v, context, cursor);
			return v;
		}
        
    }
	
	// Time formatter
	private Time tf=new Time(Time.getCurrentTimezone());
	private final static long MS_PER_DAY = 1000*60*60*24;
    
    private class MessageView extends TextView {
        public MessageView(Context context) {
            super(context);
            
            //TODO: available in API 11
            //setTextIsSelectable(true);
            
            //setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
        
        public void setUnread(boolean unread) {
        	setBackgroundColor(Message.getBkColor(unread));
        }
        
        public void setText(long time, String sender, String message, int messageType) {

        	//TODO: smart time formatting
        	// 1 minute ago,
        	// hh:mm (after 1 hour)
        	// etc...
        	
        	long delay = System.currentTimeMillis() - time;
        	
        	String fmt = "%H:%M ";
        	if (delay > MS_PER_DAY)  {
        		fmt = "%d.%m.%Y %H:%M ";
        	}

        	tf.set(time);
        	String tm= tf.format(fmt);

        	SpannableStringBuilder ss = new SpannableStringBuilder(tm);

        	int addrEnd=0;
        	
        	if (message.startsWith("/me ")) {
        		message = "*" + message.replaceAll("(/me)(?:\\s|$)", sender+' ');;
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
            
            setText(ss);
            setMovementMethod(LinkMovementMethod.getInstance());
            
        }
        
        @Override
        public String toString() {
        	return getText().toString();
        }
    }
    
	
	protected void sendMessage() {
		String text = mMessageBox.getText().toString();
		
		//avoid sending of empty messages
		if (text.length() == 0) return;
		
		String to = visavis.getJid();
		
		Message out = new Message(Message.TYPE_MESSAGE_OUT, to, text);
		mChat.addMessage(out);

		//TODO: resource magic
		XmppMessage msg = new XmppMessage(to, text, null, false);
		msg.setAttribute("id", String.valueOf(out.getId()) );
		
		//TODO: optional delivery confirmation request
		msg.addChildNs("request", MessageDispatcher.URN_XMPP_RECEIPTS);
		
		//TODO: optional chat state notifications
		msg.addChildNs(ChatStates.ACTIVE, ChatStates.XMLNS_CHATSTATES);
		sentChatState = ChatStates.ACTIVE;
		
		//TODO: message queue
		if ( serviceBinding.isLoggedIn(visavis.getRosterJid()) ) {
		    
		    serviceBinding.postStanza(visavis.getRosterJid(), msg);
		    
			//clear box after success sending
			mMessageBox.setText("");

			if (!visavis.isAvailable()) {
				Toast.makeText(getActivity(), R.string.chatSentOffline, Toast.LENGTH_LONG).show();
			}
		
		} else {
			Toast.makeText(getActivity(), R.string.shouldBeLoggedIn, Toast.LENGTH_LONG).show();
			//not sent - removing from history
			mChat.removeFromHistory(out.getId());
		}
		
		refreshVisualContent();
	}

	protected void sendChatState(String state) {

	    if (jid==null) return;
        
		//TODO: optional chat state notifications

		if (!mChat.acceptComposingEvents()) return;
		
		if (state.equals(sentChatState)) return; //no duplicates
		
		//state machine check: composing->paused
		if (state.equals(ChatStates.PAUSED))
			if (!ChatStates.COMPOSING.equals(sentChatState)) return;
		
		if ( !visavis.isAvailable() ) return;
		
		String to = visavis.getJid(); 

		//TODO: resource magic
		XmppMessage msg = new XmppMessage(to);
		//msg.setAttribute("id", "chatState");
		
		msg.addChildNs(state, ChatStates.XMLNS_CHATSTATES);
		sentChatState = state;
		
		serviceBinding.postStanza(visavis.getRosterJid(), msg);
		
	}
	
	
	private class ChatBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			refreshVisualContent();
		}
		
	}
	
	private class DeliveredReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(ChatFragment.this.getActivity(), R.string.messageDelivered, Toast.LENGTH_SHORT).show();
		}
	}
	
	private class PresenceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
		    if (jid == null) return;
			String jid = intent.getStringExtra("param");
			if (visavis.getJid().equals(jid) ) {
				updateContactBar();
			}
		}
	}
	
	private PresenceReceiver bcPresence;
	private ChatBroadcastReceiver bcUpdateChat;
	private DeliveredReceiver bcDelivered;
	
	@Override
    public void onResume() {
        super.onResume();
        
		//TODO: refresh message list, focus to last unread
		serviceBinding.doBindService();

        bcUpdateChat = new ChatBroadcastReceiver();
        //TODO: presence receiver
        getActivity().registerReceiver(bcUpdateChat, new IntentFilter(Chat.UPDATE_CHAT));

        bcDelivered = new DeliveredReceiver();
        getActivity().registerReceiver(bcDelivered, new IntentFilter(Chat.DELIVERED));
        
        bcPresence = new PresenceReceiver();
        getActivity().registerReceiver(bcPresence, new IntentFilter(Roster.UPDATE_CONTACT));

        mMessageBox.setDialogHostActivity(getActivity());

	}
	
	public void refreshVisualContent() {
		
        getLoaderManager().restartLoader(CHAT_LOADER_ID, null, this);
		
	}
	
	
	@Override
    public void onPause() {
        super.onPause();
        
        //avoid memory leak
        mMessageBox.setDialogHostActivity(null);
		
		serviceBinding.doUnbindService();
		getActivity().unregisterReceiver(bcUpdateChat);
		getActivity().unregisterReceiver(bcDelivered);
		getActivity().unregisterReceiver(bcPresence);
		
		suspendChat();
	}

	private void markAllRead() {
		synchronized(visavis) {
			int unreadCount = visavis.getUnread();
			
			visavis.setUnread(0);

			Cursor cursor = mCursorAdapter.getCursor();
			if (cursor == null) return;
			
			if (cursor.moveToLast()) do {
				Message m = ChatHistoryDbAdapter.getMessageFromCursor(cursor);
				if (m.unread) {
					mChat.markRead(m.getId());
					Lime.getInstance().notificationMgr().cancelChatNotification(m.getId());
					
					unreadCount--;
				}
			} while ( (unreadCount != 0) && cursor.moveToPrevious());
		}
	}

    public void suspendChat() {
        if (jid==null) return;

        mChat.saveSuspendedText(mMessageBox.getText().toString());
        
        sendChatState(ChatStates.PAUSED);

        markAllRead();

        getLoaderManager().restartLoader(CHAT_LOADER_ID, null, this);        
        jid = null;
    }
    
    private static class ChatCursorLoader extends SimpleCursorLoader {

        public ChatCursorLoader(Context context) {
            super(context);
        }
        
        @Override
        public Cursor loadInBackground() {
            if (mChat == null) return null;
            
            return mChat.getCursor();
        }
        
    }


    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new ChatCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        
        mCursorAdapter.swapCursor(cursor);
        
        chatListView.setSelection(mCursorAdapter.getCount()-1);
        
        //TODO: hide progress
        chatListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
        
        //TODO: show progress
        chatListView.setVisibility(View.GONE);
    }

    public Chat getChat() {   
        return mChat;
    }
}
