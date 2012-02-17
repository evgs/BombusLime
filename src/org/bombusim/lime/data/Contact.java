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
import org.bombusim.xmpp.stanza.Presence;

import android.graphics.Bitmap;

public class Contact implements Comparable<Contact>{
	public static final String GROUP_SEPARATOR = "\t";
	private String rosterJid;
	private String jid;
	private String name;
	
	private long _id;
	
	private int subscription;
	
	private Bitmap avatar;
	private String avatarId;
	
	private boolean activeChats;

	private int unread = -1;

	private int updateMark;

	//Tab-separated group names
	private String groups;
	
	private Resource activeResource;
	private ArrayList<Resource> resources;
	
	public final static int UPDATE_NONE = 0;
	public final static int UPDATE_SAVE = 1;
	public final static int UPDATE_DROP = 2;

	public final static int SUBSCR_NONE = 0;
	public final static int SUBSCR_FROM = 1;
	public final static int SUBSCR_TO =   2;
	public final static int SUBSCR_BOTH = 3;
	public final static int SUBSCR_REMOVE = -1;
	
	public Contact(String jid, String name, long id) {
		this.jid=jid;
		this.name=name;
		this._id = id;
		
		resources = new ArrayList<Resource>(1);
		activeResource = new Resource();
	}
	
	public Contact(String jid, String name) {
		this(jid, name, -1);
	}
	
	public long getId() { return _id; }
	public void setId(long id) { this._id = id; } 

	public Resource setPresence(int presenceIndex, String resource, int priority) {
		//TODO: check usecase "unavailable"
		Resource c =  getResource(resource);
		if (c == null) {
			c = new Resource();
			resources.add(c);
		}
		c.presence = presenceIndex;
		c.resource = resource;
		c.priority = priority;
		
		Collections.sort(resources);
		activeResource = resources.get(0);
		
		return c;
	}
	
	public Resource getResource(String resource) {
		for (int i=0; i<resources.size(); i++) {
			Resource c = resources.get(i);
			if (compareNStrings(c.resource, resource)) return c;
		}
		return null;
	}
	
	public int getPresence() { return activeResource.presence; }
	
	public String getStatusMessage() { return activeResource.statusMessage; }
	
	public String getJid() { return jid; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getScreenName() {
		return (name==null)?jid:name;
	}

	public Bitmap getAvatar() { return avatar; }

	public String getAvatarId() { return avatarId; }
	
	public void setAvatar( Bitmap avatar, String avatarId ) { 
		if (avatarId == null) return;
		
		this.avatar = avatar;
		
		if (!compareNStrings(this.avatarId, avatarId)) updateMark = UPDATE_SAVE;
		this.avatarId = avatarId;
	} 
	
	public int getSubscription() { return subscription; }

	public void setSubscription(int subscr) { this.subscription = subscr; }
	
	public void setSubscription(String subscr) {
		if (subscr.equals("none")) { subscription = SUBSCR_NONE; }
		if (subscr.equals("from")) { subscription = SUBSCR_FROM; }
		if (subscr.equals("to"))   { subscription = SUBSCR_TO;   }
		if (subscr.equals("both")) { subscription = SUBSCR_BOTH; }
		if (subscr.equals("remove")) {
			subscription = SUBSCR_REMOVE;
			updateMark = UPDATE_DROP;
		}
	}

	public String getRosterJid() { return rosterJid; }

	public void setRJid(String rosterJid) { this.rosterJid = rosterJid; }


	public String getAllGroups() { return groups; }
	
	public String[] getAllGroupsArray() {
		if (groups == null) return new String[0];
		
		return groups.split(GROUP_SEPARATOR);
	}

	public void setAllGroups(String groups) {
		this.groups = groups;
	}

	public void addGroup(String groupName) {
		if (groups==null) {
			groups = groupName;
		} else {
			groups = groups + GROUP_SEPARATOR + groupName;
		}
	}

	public int getUpdate() { return updateMark; }
	
	public void setUpdate(int upd) { this.updateMark = upd; }

	/**
	  * Updates fields name, group, subscription
	  * 
	  *  @param
	  *  n - data to update
	  */
	
	public void updateAvatarHash(String avatarId) {
		if (!compareNStrings(this.avatarId, avatarId)) 
			avatar = null;
	}
	
	public void update(Contact n) {
		if (n.updateMark == UPDATE_DROP) {
			updateMark = UPDATE_DROP;
			return;
		}

		if (!compareNStrings(name, n.name)) {
			name = n.name;
			updateMark = UPDATE_SAVE;
		}
		
		if (!compareNStrings(groups, n.groups)) {
			groups = n.groups;
			updateMark = UPDATE_SAVE;
		}
		
		if (subscription != n.subscription) {
			subscription = n.subscription;
			updateMark = UPDATE_SAVE;
		}
		
	}

	private final static boolean compareNStrings(String s1, String s2) {
		if (s2==null) {
			if (s1==null) return true;
		} else {
			if (s1==null) return false;
		} 
		return s1.equals(s2);
	}

	public Bitmap getLazyAvatar(boolean delayed) {
		if (avatar!=null) return avatar;

		Lime.getInstance().vcardResolver.queryTop();
		

		if (avatarId !=null) {
			if (avatarId.equals(Vcard.AVATAR_PENDING)) return null;
			if (avatarId.equals(Vcard.AVATAR_MISSING)) return null;
			
			//cache lookup
			Vcard cached = new Vcard(jid, avatarId);
			avatar = cached.getAvatar();
			
			//cache hit?
			if (avatar!=null)  return avatar; 	
			
			//cache miss
			avatarId = null;
		}
		

		if (delayed) return null;

		//query vcard
		if (avatarId==null) {
			//TODO: request avatar
			avatarId = Vcard.AVATAR_PENDING;
			
			Lime.getInstance().vcardResolver.queryVcard(this);
		}
		
		//fallback - avatar request already queried
		return null;
	}

	public boolean hasActiveChats() { return activeChats; }
	
	public int getUnread() {
		if (unread < 0) 
			unread = Chat.countUnread(jid, rosterJid);
		return unread; 	
	}

	public void setUnread(int i) { this.unread = i; }
	
	public void setActiveChats(boolean has) { activeChats = has; }

	public void dropAvatar() {
		//will be loaded on demand
		avatar = null; 
	}

	public void setResourcesOffline() {
		for (Resource r : resources) {
			r.presence = Presence.PRESENCE_OFFLINE; 
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Contact) {
			return jid.equals(((Contact)o).getJid());
		} else return false;
	}

	@Override
	public int compareTo(Contact another) {
		int diff = activeResource.presence - another.activeResource.presence;
		if (diff !=0) return diff;
		//TODO: compare fast indexes
		return getScreenName().compareToIgnoreCase(another.getScreenName());
	}

	public CharSequence getFullName() {
 		return (name==null)?
 				jid : name+ " <"+jid+">";
	}

}
