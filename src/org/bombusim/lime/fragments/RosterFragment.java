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

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.activity.About;
import org.bombusim.lime.activity.AccountSettingsActivity;
import org.bombusim.lime.activity.ActiveChats;
import org.bombusim.lime.activity.ChatActivity;
import org.bombusim.lime.activity.EditContactActivity;
import org.bombusim.lime.activity.LimePrefs;
import org.bombusim.lime.activity.LoggerActivity;
import org.bombusim.lime.activity.PresenceActivity;
import org.bombusim.lime.activity.RosterActivity;
import org.bombusim.lime.activity.RosterAdapter;
import org.bombusim.lime.activity.RosterLimePrefsActivity;
import org.bombusim.lime.activity.VCardActivity;
import org.bombusim.lime.data.Contact;
import org.bombusim.lime.data.Roster;
import org.bombusim.lime.data.RosterGroup;
import org.bombusim.lime.service.XmppService;
import org.bombusim.lime.service.XmppServiceBinding;
import org.bombusim.xmpp.XmppAccount;
import org.bombusim.xmpp.handlers.IqRoster;
import org.bombusim.xmpp.stanza.XmppPresence;

import com.actionbarsherlock.app.SherlockListFragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class RosterFragment extends SherlockListFragment {
    XmppServiceBinding sb;
    
    private Bitmap[] statusIcons;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setHasOptionsMenu(true);
        
        statusIcons = new Bitmap[] { 
                BitmapFactory.decodeResource(getResources(), R.drawable.status_offline),
                BitmapFactory.decodeResource(getResources(), R.drawable.status_online),
                BitmapFactory.decodeResource(getResources(), R.drawable.status_chat),
                BitmapFactory.decodeResource(getResources(), R.drawable.status_away),
                BitmapFactory.decodeResource(getResources(), R.drawable.status_xa),
                BitmapFactory.decodeResource(getResources(), R.drawable.status_dnd),
                BitmapFactory.decodeResource(getResources(), R.drawable.status_ask),
                BitmapFactory.decodeResource(getResources(), R.drawable.status_unknown),
                BitmapFactory.decodeResource(getResources(), R.drawable.status_invisible)
                };

        ListAdapter adapter=new RosterAdapter(getActivity(), statusIcons);
        
        setListAdapter(adapter);
        
        sb = new XmppServiceBinding(getActivity());

        //temporary
        Lime.getInstance().saveBinding(sb);
        
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        registerForContextMenu(getListView());
    }
    
    protected void showSslStatus(String certificateChain) {
        if (certificateChain == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.sslInfo)
               .setIcon(R.drawable.ssl_yes)
               .setMessage(certificateChain)
               .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
               });
        AlertDialog alert = builder.create();
        alert.setOwnerActivity(getActivity());
        alert.show();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //TODO: collapse/expand account
        
        Object item = getListAdapter().getItem(position);
        
        if (item instanceof RosterGroup) {
            ((RosterGroup)item).toggleCollapsed();
            refreshVisualContent();
            return;
        }
        if (item instanceof XmppAccount) {
            ((XmppAccount)item).toggleCollapsed();
            refreshVisualContent();
            return;
        }
        if (item instanceof Contact) {
            Contact c = (Contact) item;
            openChatActivity(c);
        }
    }
    
    @Override
    public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
        inflater.inflate(R.menu.roster_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
        
        //enable items available only if logged in
        //TODO: modify behavior if multiple account
        /*
        String rJid = Lime.getInstance().accounts.get(0).userJid;
        menu.setGroupEnabled(R.id.groupLoggedIn, sb.isLoggedIn(rJid) );
        */
    };
    
    
    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        Context context = getActivity().getBaseContext(); 
        switch (item.getItemId()) {
        case R.id.cmdLogin:     startActivityForResult(new Intent(context, PresenceActivity.class),        0); break;
        
        case R.id.cmdAddContact: openEditContactActivity(null); break;
        
        case R.id.cmdAccount:  startActivityForResult(new Intent(context, AccountSettingsActivity.class), 0); break;
        
        case R.id.cmdChat:  {
            ActiveChats chats = new ActiveChats();
            chats.setStatusIcons(statusIcons);
            chats.showActiveChats(getActivity(), null);
            
            break;
        }
        
        case R.id.cmdLog:      startActivityForResult(new Intent(context, LoggerActivity.class),          0); break;
        case R.id.cmdSettings: startActivityForResult(new Intent(context, LimePrefs.class),               0); break;
        case R.id.cmdRosterSettings: startActivityForResult(new Intent(context, RosterLimePrefsActivity.class), 0); break;
            
        case R.id.cmdAbout: About.showAboutDialog(getActivity()); break;
        default: return true; // on submenu
        }
        
        return false;
    }
    
    Object contextItem;
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        
        int pos = ((AdapterContextMenuInfo)menuInfo).position;
        
        contextItem = getListAdapter().getItem(pos);
        
        if (contextItem instanceof RosterGroup) {
            //TODO: context menu for group
            return;
        }
        if (contextItem instanceof XmppAccount) {
            //TODO: context menu for account
            return;
        }
        
        if (contextItem instanceof Contact) {
        Contact c = (Contact) contextItem;
            menu.setHeaderTitle(c.getScreenName());
            
            Drawable icon = new BitmapDrawable(c.getAvatar());
            menu.setHeaderIcon(icon);
            
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.contact_menu, menu);
            
            //enable items available only if logged in
            menu.setGroupEnabled(R.id.groupLoggedIn, sb.isLoggedIn(c.getRosterJid()) );
            return;
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        
        AdapterContextMenuInfo cmi = (AdapterContextMenuInfo) item.getMenuInfo();

        //contextItem = getListAdapter().getItem(cmi.position);

        if (contextItem instanceof Contact) {
            final Contact ctc = (Contact) contextItem;
    
            switch (item.getItemId()) {
            case R.id.cmdChat: 
                openChatActivity(ctc);
                return true;
            case R.id.cmdVcard:
                openVCardActivity(ctc);
                return true;
            case R.id.cmdEdit:
                openEditContactActivity(ctc);
                return true;
            case R.id.cmdDelete:
                confirmDeleteContact(ctc);
                return true;
                
            case R.id.cmdSubscrRequestFrom:
                subscription(ctc, XmppPresence.PRESENCE_SUBSCRIBE);
                return true;
            case R.id.cmdSubscrSendTo:
                subscription(ctc, XmppPresence.PRESENCE_SUBSCRIBED);
                return true;
            case R.id.cmdSubscrRemove:
                subscription(ctc, XmppPresence.PRESENCE_UNSUBSCRIBED);
                return true;
                
            default:
                Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_SHORT).show();
            }
        }
        
        return super.onContextItemSelected(item);
    }

    private class RosterBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String from = intent.getStringExtra("param");
            
            if (! Lime.getInstance().prefs.hideOfflines) {
                //TODO: optimize if hide offlines
                if (from !=null) {
                    for (Contact c: Lime.getInstance().getRoster().getContacts()) {
                        if (c.getJid().equals(from)) {
                            refreshSingleContact(c);
                        }
                    }
                    
                    return;
                }
            }
            refreshVisualContent();
        }
        
    }
    
    void refreshSingleContact(Contact c) {
        ListView lv = getListView();

        int firstVisible = lv.getFirstVisiblePosition();
        int lastVisible = lv.getLastVisiblePosition();

        for (int position = firstVisible; position<=lastVisible; position++) {
            try {
                Contact clv = (Contact) lv.getItemAtPosition(position);
                if (c == clv) {
                    
                    //TODO: remove this workaround
                    refreshVisualContent();
                    /*
                    View cv = lv.getChildAt(position);
                    if (cv !=null) { 
                        cv.invalidate();
                    } else {
                        //can't find view for contact, so refresh all
                        //TODO: is it real need to refresh all?
                        refreshVisualContent();
                        return;
                    }*/
                }
            } catch (ClassCastException e) {}
        }

    }
    
    //TODO: update only group if presence update 
    void refreshVisualContent(){
        //TODO: fix update
        //updateRosterTitle();
        
        ListView lv = getListView(); 
        lv.setVisibility(View.GONE);
        RosterAdapter ra = (RosterAdapter)getListAdapter();
        lv.invalidate();
        ra.notifyDataSetChanged();
    
        lv.setVisibility(View.VISIBLE);
    }
    
    RosterBroadcastReceiver br;
    @Override
    public void onResume() {
        super.onResume();
        
        sb.setBindListener(new XmppServiceBinding.BindListener() {
            @Override
            public void onBindService(XmppService service) {
                //updateRosterTitle();
            }
        });
        
        sb.doBindService();
        //update view to actual state
        refreshVisualContent();
        br = new RosterBroadcastReceiver();
        getActivity().registerReceiver(br, new IntentFilter(Roster.UPDATE_CONTACT));
    }
    
    @Override
    public void onPause() {
        sb.doUnbindService();
        getActivity().unregisterReceiver(br);
        super.onPause();
    }
    
    //todo: move logic out of fragment
    public void openChatActivity(Contact c) {
        ((RosterActivity) getActivity()).openChat(c.getJid(), c.getRosterJid());
    }

    //todo: move logic out of fragment
    private void openVCardActivity(Contact c) {
        Intent openVcard =  new Intent(getActivity(), VCardActivity.class);
        openVcard.putExtra(VCardActivity.MY_JID, c.getRosterJid());
        openVcard.putExtra(VCardActivity.JID,   c.getJid());
        startActivity(openVcard);
    }
    
    //todo: move logic out of fragment
    private void openEditContactActivity(Contact c) {
        Intent openEditContact =  new Intent(getActivity(), EditContactActivity.class);
        if (c!=null) {
            openEditContact.putExtra(EditContactActivity.MY_JID, c.getRosterJid());
            openEditContact.putExtra(EditContactActivity.JID,   c.getJid());
        } else {
            String rJid = Lime.getInstance().getActiveAccount().userJid;
            openEditContact.putExtra(EditContactActivity.MY_JID, rJid);
            //openEditContact.putExtra(EditContactActivity.JID,   c.getJid());
        }
        startActivity(openEditContact);
    }

    private void confirmDeleteContact(final Contact ctc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(ctc.getFullName())
               .setIcon(new BitmapDrawable(ctc.getAvatar()) )
               .setMessage(R.string.confirmDelete)
               .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        deleteContact(ctc);
                   }
               })
               .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
               });
        AlertDialog alert = builder.create();
        alert.setOwnerActivity(getActivity());
        alert.show();
    }

    private void subscription(Contact contact, String subscriptionAction) {
        IqRoster.setSubscription(
                contact.getJid(), 
                subscriptionAction, 
                sb.getXmppStream(contact.getRosterJid()));
    }

    protected void deleteContact(Contact c) {
        IqRoster.deleteContact(
            c.getJid(), 
            sb.getXmppStream(c.getRosterJid())
        );
    }

}
