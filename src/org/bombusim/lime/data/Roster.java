package org.bombusim.lime.data;

import java.util.ArrayList;
import java.util.Collections;

import org.bombusim.lime.Lime;

public class Roster {
	
	public static final String UPDATE_ROSTER = "org.bombusim.lime.data.UPDATE_ROSTER";
	
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

	public void replaceRoster(ArrayList<Contact> updates, String rosterJid, boolean replaced) {
		//finalize all pending updates
		updateDB();  

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
		
		//3. finalize transaction
		updateDB();
		
		//4. check all avatars
		
		//massive avatar request
		//for (int index = 0; index < contacts.size(); index++) {
		//	contacts.get(index).getLazyAvatar(false);
		//}
		
		Collections.sort(contacts);
		
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
		synchronized (contacts) {
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
		for (Contact c: contacts) {
			if (c.getRosterJid().equals(rosterJid)) {
				c.setResourcesOffline();
			}
		}
	}
	
	public void notifyVcard(Vcard vcard) {
		
		for (int index = 0; index < contacts.size(); index++) {
			Contact c = contacts.get(index);
			if (c.getJid().equals(vcard.getJid())) { 
				c.setAvatar(vcard.getAvatar(), vcard.getAvatarId());
				Lime.getInstance().vcardResolver.vcardNotify(c);
			}
		}
		
		updateDB();
	}

	public Contact findContact(String from, String rosterJid) {
		for (int i=0; i<contacts.size(); i++) {
			Contact c = contacts.get(i);
			if (c.getJid().equals(from))
				if (c.getRosterJid().equals(rosterJid))
					return c;
		}
		
		return null;
	}
	
	public void dropCachedAvatars() {
		for (int i=0; i<contacts.size(); i++) {
			contacts.get(i).dropAvatar();
		}
	}


}
