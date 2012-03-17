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
    @Override
    protected void onCreate(Bundle savedInstance) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstance);
        
        //setContentView(R.layout.main);
        setContentView(R.layout.main_tab);
    }

    public void openChat(String jid, String rosterJid) {
        ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.chatFragment);
        
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
}
