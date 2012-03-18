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
import org.bombusim.lime.fragments.ChatFragment.ChatFragmentListener;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

public class ChatActivity extends FragmentActivity
    implements ChatFragmentListener {
    public static final String MY_JID = "fromJid";
    public static final String TO_JID = "toJid";

    private String mChatJid;
    private String mChatRJid;
    
    /*
     * called when android:launchMode="singleTop"
     * single-chat mode, replaces existing chat;
     * @see android.app.Activity#onNewIntent(android.content.Intent)
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        attachToChat(intent);
    }

    private void attachToChat(Intent intent) {
        mChatJid = intent.getStringExtra(TO_JID);
        mChatRJid = intent.getStringExtra(MY_JID);
    }

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        //TODO: make choice based on screen resolution, not only orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // If the screen is now in landscape mode, we can show the
            // dialog in-line with the list so we don't need this activity.
            finish();
            return;
        }
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        
        setContentView(R.layout.single_chat);
        
        if(savedInstance != null) {
            mChatJid = savedInstance.getString(ChatActivity.TO_JID);
            mChatRJid = savedInstance.getString(ChatActivity.MY_JID);
        } else {
            attachToChat(getIntent());
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        ((ChatFragment)getSupportFragmentManager().findFragmentById(R.id.chatFragment))
        .attachToChat(mChatJid, mChatRJid);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        //((ChatFragment)getSupportFragmentManager().findFragmentById(R.id.chatFragment)) .suspendChat();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putString(ChatActivity.MY_JID, mChatRJid);
        outState.putString(ChatActivity.TO_JID, mChatJid);
    }

    @Override
    public void closeChat() { finish(); }

    @Override
    public boolean isTabMode() {
        return false;
    }
}

