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

import org.bombusim.lime.R;
import org.bombusim.lime.fragments.ChatFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class RosterActivity extends FragmentActivity{
    private String mChatJid;
    private String mChatRJid;
    
    }
    
    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        
        //setContentView(R.layout.main);
        setContentView(R.layout.main_tab);
        
        handleIntent(getIntent());

        if (savedInstance!=null) {
            mChatJid = savedInstance.getString(ChatActivity.TO_JID);
            mChatRJid = savedInstance.getString(ChatActivity.MY_JID);
        }
        
    }

    private void handleIntent(Intent intent) {
        
        String intentAction = intent.getAction(); 
        if (intentAction.startsWith("Msg")) {
            mChatJid = intent.getStringExtra(ChatActivity.TO_JID);
            mChatRJid = intent.getStringExtra(ChatActivity.MY_JID);
            
            showChat(true);
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
    }

    public void showChat(boolean openActivity) {
        ChatFragment chatFragment = getChatFragment();
        
        if (chatFragment !=null) {
            chatFragment.suspendChat();
            chatFragment.attachToChat(jid, rosterJid);
        } else {
            Intent openChat =  new Intent(this, ChatActivity.class);
            openChat.putExtra(ChatActivity.MY_JID, rosterJid);
            openChat.putExtra(ChatActivity.TO_JID, jid);
            startActivity(openChat);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        showChat(false);
    }
    
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putString(ChatActivity.MY_JID, mChatRJid);
        outState.putString(ChatActivity.TO_JID, mChatJid);
    }
}
