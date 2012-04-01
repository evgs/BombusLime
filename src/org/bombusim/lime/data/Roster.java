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

package org.bombusim.lime.data;

import java.util.ArrayList;
import java.util.Collections;

import org.bombusim.lime.Lime;

public class Roster {
	
	public static final String UPDATE_CONTACT = "org.bombusim.lime.data.UPDATE_ROSTER";
	
	private ArrayList<Contact> contacts;

	public Roster(String rosterJid){
		contacts = new ArrayList<Contact>();
		
		loadDB(rosterJid);
	}
	public Roster(){ this (null); }
	
	public void addContact(Contact c) {
		contacts.add(c);
	}
	
	public ArrayList<Contact> getContacts() { return contacts; }
	
    @SuppressWarnings("unchecked")
    public ArrayList<Contact> getContactsCopy() {
        synchronized (this) {
            return (ArrayList<Contact>) contacts.clone();
        }
    }

	public void replaceRoster(ArrayList<Contact> updates, String rosterJid, boolean replaced) {
        //finalize all pending updates
        updateDB();
        
		synchronized (this) {
	
			//1. mark all for drop
			
			if (replaced)
			for (Contact c : contacts) {
				if (c.getRosterJid().equals(rosterJid))
				c.setUpdate(Contact.UPDATE_DROP);
			}
			
			//2. import or update r
			for (Contact upd : updates) {
				int i = contacts.indexOf(upd);
				if (i<0) {
					contacts.add(upd);
					upd.setUpdate(Contact.UPDATE_SAVE);
				} else {
					Contact o = contacts.get(i);
					o.setUpdate(Contact.UPDATE_NONE);
					o.update(upd);
				}
			}
		}
		
        //3. finalize transaction
        updateDB();
	}
	
	private void loadDB(String rosterJid) {
		RosterDbAdapter db = new RosterDbAdapter(Lime.getInstance().getApplicationContext());
		
		db.open();
		
		long[] indexes = db.getContactIndexes(rosterJid);
		
		//TODO: load using Cursor
		if (indexes != null) 
		for (long index : indexes) {
			contacts.add(db.getContact(index));
		}
		
		db.close();
	}

	public void updateDB() { 
		synchronized (this) {
			RosterDbAdapter db = new RosterDbAdapter(Lime.getInstance().getApplicationContext());
			
			db.open();
			
			int index = 0;
			while (index<contacts.size()) {
				Contact c = contacts.get(index);
				switch (c.getUpdate()) {
				case Contact.UPDATE_DROP: 
					db.removeContact(c.getId());
					contacts.remove(index);
					continue;
				case Contact.UPDATE_SAVE:
					db.putContact(c, c.getId());
					c.setUpdate(Contact.UPDATE_NONE);
				default:
				}
				index++;
			}
			
			db.close();
		}
	}

	public void forceRosterOffline(String rosterJid) {
        synchronized (this) {
    		for (Contact c: contacts) {
    		    if (c.getRosterJid().equals(rosterJid)) {
    		        c.setResourcesOffline();
    		    }
    		}
	    }
	}
	
	public void notifyVcard(Vcard vcard) {
        synchronized (this) {
            for (int index = 0; index < contacts.size(); index++) {
                Contact c = contacts.get(index);
                if (c.getJid().equals(vcard.getJid())) { 
                    c.setAvatar(vcard.getAvatar(), vcard.getAvatarId());
                }
            }
        }
		updateDB();
	}

	public Contact findContact(String from, String rosterJid) {
		synchronized (this) { 
		    for (int i=0; i<contacts.size(); i++) {
    			Contact c = contacts.get(i);
    			if (c.getJid().equals(from))
    				if (c.getRosterJid().equals(rosterJid))
    					return c;
		    }
		}
		
		return null;
	}
	
	public void dropCachedAvatars() {
        synchronized (this) { 
            for (int i=0; i<contacts.size(); i++) {
                contacts.get(i).dropAvatar();
            }
        }
	}
	
    public SelfContact getSelfContact(String jid) {
        Contact c = findContact(jid, jid);
        
        if (c == null) {
            c = new SelfContact(jid, null, -1);
            c.setRJid(jid);
            
            c.setUpdate(SelfContact.UPDATE_SAVE);
            synchronized (this) { addContact(c); }
        }
        
        return (SelfContact) c;
    }
}
