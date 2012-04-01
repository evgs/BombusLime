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

import java.util.ArrayList;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.data.Chat;
import org.bombusim.lime.data.Contact;
import org.bombusim.lime.fragments.ChatFragment;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class RosterActivity extends SherlockFragmentActivity
    implements ChatFragment.ChatFragmentListener,
    ActionBar.TabListener {
    private String mChatJid;
    private String mChatRJid;
    
    public boolean isTabMode() {
        //TODO: make choice based on screen resolution, not only orientation 
        return (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }
    
    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        
        //TODO: set layout based on screen resolution, not only orientation 
        if (isTabMode()) {
            setContentView(R.layout.main_tab);
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);            
        } else {
            setContentView(R.layout.main);
        }
        
        handleIntent(getIntent());

        if (savedInstance!=null) {
            mChatJid = savedInstance.getString(ChatActivity.TO_JID);
            mChatRJid = savedInstance.getString(ChatActivity.MY_JID);
        }
        
    }

    private void handleIntent(Intent intent) {
        
        String intentAction = intent.getAction();
        if (intentAction == null) return;
        
        if (intentAction.startsWith("Chat")) {
            mChatJid = intent.getStringExtra(ChatActivity.TO_JID);
            mChatRJid = intent.getStringExtra(ChatActivity.MY_JID);
            
            showChat(false);
        }
    }

    /*
     * called when android:launchMode="singleTop"
     * single-chat mode, replaces existing chat;
     * @see android.app.Activity#onNewIntent(android.content.Intent)
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        handleIntent(intent);
    }
    
    private ChatFragment getChatFragment() {
        if (!isTabMode()) return null;
        return (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.chatFragment);
    }
    
    public void openChat(String jid, String rosterJid) {
        mChatJid = jid;
        mChatRJid = rosterJid;
        
        showChat(true);
        updateTabs();
    }

    public void updateTabs() {
        if(!isTabMode()) return;
        
        ArrayList<Chat> chats = Lime.getInstance().getChatFactory().getChats();
        Chat active = getChatFragment().getChat();
        
        //TODO: replace only changed tab
        getSupportActionBar().removeAllTabs();
        
        for (int i=0; i<chats.size(); i++) {
            Chat c = chats.get(i);
            ActionBar.Tab tab = getSupportActionBar().newTab();
            tab.setText(c.getVisavis().getScreenName());
            tab.setTabListener(this);
            
            boolean selected = (c==active);
            
            getSupportActionBar().addTab(tab, i, selected);
        }
        
    }
    
    public void showChat(boolean openActivity) {
        ChatFragment chatFragment = getChatFragment();
        
        if (chatFragment !=null) {
            chatFragment.suspendChat();
            chatFragment.attachToChat(mChatJid, mChatRJid);
            
            return;
        } 
        
        if (openActivity) {
            Intent openChat =  new Intent(this, ChatActivity.class);
            openChat.putExtra(ChatActivity.MY_JID, mChatRJid);
            openChat.putExtra(ChatActivity.TO_JID, mChatJid);
            startActivity(openChat);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        showChat(false);
        updateTabs();
    }
    
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putString(ChatActivity.MY_JID, mChatRJid);
        outState.putString(ChatActivity.TO_JID, mChatJid);
    }

    @Override
    public void closeChatFragment() {
        // TODO Auto-generated method stub
    }
    
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        int t=tab.getPosition();
        Chat target = Lime.getInstance().getChatFactory().getChats().get(t);
        
        Contact visavis = target.getVisavis(); 
        mChatJid = visavis.getJid();
        mChatRJid = visavis.getRosterJid();
        
        showChat(true);
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
        
    }
}
