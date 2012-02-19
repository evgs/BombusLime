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
import org.bombusim.lime.widgets.ContactViewFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

public class RosterAdapter extends BaseExpandableListAdapter {

    private LayoutInflater mInflater;
    
    private ContactViewFactory cvf;
    
    public RosterAdapter(Context context, Bitmap[] statusIcons) {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(context);

        cvf = new ContactViewFactory(context, statusIcons);
        
        groups = new ArrayList<RosterAdapter.RosterGroup>();
    }
	
    //-------------------------------------------------------------------------------------------
    private class RosterGroup implements Comparable<RosterGroup>{
    	public String groupName;
    	public ArrayList<Contact> contacts;
    	public boolean collapsed;
    	
    	public RosterGroup(String name) {
    		this.groupName = name;
			contacts = new ArrayList<Contact>();
		}

		@Override
		public int compareTo(RosterGroup another) {
			return groupName.compareToIgnoreCase(another.groupName);
		}
    }
    
    private ArrayList<RosterAdapter.RosterGroup> groups;
    
    @Override
    public void notifyDataSetChanged() {
    	getContacts();
    	super.notifyDataSetChanged();
    }
    
	public void getContacts() {
		//TODO: keep groups when RosterActivity is destroyed
		
		ArrayList<Contact> contacts = Lime.getInstance().getRoster().getContacts();
		
		//1. geset groups
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

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return groups.get(groupPosition)
				.contacts.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) { 	return childPosition; }

	@Override
	public int getChildrenCount(int groupPosition) {
		return groups.get(groupPosition).contacts.size();
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) { return true; }

	@Override
	public Object getGroup(int groupPosition) {  return groups.get(groupPosition); 	}

	@Override
	public long getGroupId(int groupPosition) { return groupPosition; 	}

	@Override
	public int getGroupCount() {  return groups.size(); }

	@Override
	public void onGroupCollapsed(int groupPosition) { groups.get(groupPosition).collapsed = true; }
	
	@Override
	public void onGroupExpanded(int groupPosition) { groups.get(groupPosition).collapsed = false; }

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {


        Contact c=(Contact)(getChild(groupPosition, childPosition));

		return cvf.getContactView(convertView, c);
    }


	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		// A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        ViewGroupHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.rostergroup, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewGroupHolder();
            holder.groupLabel = (TextView) convertView.findViewById(R.id.grouplabel);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewGroupHolder) convertView.getTag();
        }

        // Bind the data efficiently with the holder.
        
        RosterGroup g = (RosterGroup) getGroup(groupPosition);
        holder.groupLabel.setText(g.groupName);

        return convertView;
	}

    static class ViewGroupHolder {
        TextView groupLabel;
    }
	
	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	public void updateGroupExpandedState(ExpandableListView lv) {
		int count = groups.size();
		for (int i=0; i<count; i++) {
			if (groups.get(i).collapsed) lv.collapseGroup(i);
			else lv.expandGroup(i);
		}
	}
}
