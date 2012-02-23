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
import java.util.Collections;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.data.Contact;
import org.bombusim.lime.data.RosterGroup;
import org.bombusim.lime.widgets.AccountViewFactory;
import org.bombusim.lime.widgets.ContactViewFactory;
import org.bombusim.lime.widgets.GroupViewFactory;
import org.bombusim.xmpp.XmppAccount;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

public class RosterAdapter extends BaseAdapter {
	
	public final static int ITEM_ACCOUNT = 0;
	public final static int ITEM_CONTACT = 1;
	public final static int ITEM_GROUP = 2;
	
	public final static int ITEM_TYPECOUNT = 3;

	private ArrayList rosterObjects;

	private ContactViewFactory cvf;
    private GroupViewFactory gvf;
    private AccountViewFactory avf;
    
	private ArrayList<RosterGroup> groups;
    
    public RosterAdapter(Context context, Bitmap[] statusIcons) {

        cvf = new ContactViewFactory(context, statusIcons);
        avf = new AccountViewFactory(context, statusIcons);
        gvf = new GroupViewFactory(context);

        rosterObjects = new ArrayList();
        
        groups = new ArrayList<RosterGroup>();
    }
	
	
	@Override
	public int getCount() { return rosterObjects.size(); }

	@Override
	public Object getItem(int position) { return rosterObjects.get(position); }

	@Override
	public long getItemId(int position) { return position; }

	@Override
	public boolean hasStableIds() { return true; 	}
	
	@Override
	public int getViewTypeCount() { return ITEM_TYPECOUNT; }
	
	@Override
	public int getItemViewType(int position) {
		Object o = rosterObjects.get(position);
		if (o instanceof XmppAccount) return ITEM_ACCOUNT;
		if (o instanceof RosterGroup) return ITEM_GROUP;
		if (o instanceof Contact)     return ITEM_CONTACT;
		
		return -1;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Object o = rosterObjects.get(position);
		
		if (o instanceof Contact) return cvf.getView(convertView, (Contact)o);
		if (o instanceof RosterGroup) return gvf.getView(convertView, (RosterGroup)o);
		if (o instanceof XmppAccount) return avf.getView(convertView, (XmppAccount)o);
		
		return null;
	}
	
    @Override
    public void notifyDataSetChanged() {
    	populateRosterObjects();
    	super.notifyDataSetChanged();
    }

	public void populateRosterObjects() {
		//TODO: keep groups when RosterActivity is destroyed

		rosterObjects.clear();
		
		//TODO: loop trough accounts
		XmppAccount a = Lime.getInstance().getActiveAccount();
		rosterObjects.add(a);
		
		if (a.collapsed) {
			//TODO: next account
			return;
		}
		//0. add account item
		
		
		ArrayList<Contact> contacts = Lime.getInstance().getRoster().getContacts();
		
		//1. reset groups
		for (RosterGroup group: groups) { 	group.contacts.clear(); }
		
		//2. populate groups with contacts
		//TODO: collate by roster jid
		for (Contact contact: contacts) {
			String allGroups = contact.getAllGroups();
			if (allGroups == null) {
				//TODO: group sorting indexes
				addContactToGroup(contact, "- No group");
				continue;
			}
			
			String cgroups[] = allGroups.split("\t");
			for (String cg : cgroups) {
				addContactToGroup(contact, cg);
			}
		}
		
		//3. remove empty groups
		int i=0;
		while (i<groups.size()) {
			if (groups.get(i).contacts.isEmpty()) { 
				groups.remove(i);
			} else i++;
		}
		
		//4. sort groups
		Collections.sort(groups);
		
		//5. add reoups to roster
		//TODO 5.1 check if account collapsed
		
		for (RosterGroup group : groups) {
			rosterObjects.add(group);
			
			//skip contacts if group collapsed
			if (group.collapsed) continue;
			
			rosterObjects.addAll(group.contacts);
		}
		
		//TODO: add MUC
	}

    private void addContactToGroup(Contact contact, String groupName) {
    	for (RosterGroup g : groups) {
    		if (g.groupName.equals(groupName)) {
    			g.contacts.add(contact);
    			return;
    		}
    	}
    	
    	RosterGroup ng = new RosterGroup(groupName);
    	ng.contacts.add(contact);
    	groups.add(ng);
	}
    
}

