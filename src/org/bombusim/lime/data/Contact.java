package org.bombusim.lime.data;

import java.util.ArrayList;
import java.util.Collections;

import org.bombusim.lime.Lime;

import android.graphics.Bitmap;

public class Contact {
	private String rosterJid;
	private String jid;
	private String name;
	
	private long _id;
	
	private int subscription;
	
	private Bitmap avatar;
	private String avatarId;
	
	private boolean activeChats;

	private int updateMark;

	private Resource activeResource;
	private ArrayList<Resource> resources;
	
	public final static int UPDATE_NONE = 0;
	public final static int UPDATE_SAVE = 1;
	public final static int UPDATE_DROP = 2;
	
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
		if (resource == null) return activeResource; //STUB
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
			if (c.resource.equals(resource)) return c;
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
	
	public void setSubscription(String attribute) {
		// TODO subscription
		
	}

	public String getRosterJid() { return rosterJid; }

	public void setRJid(String rosterJid) { this.rosterJid = rosterJid; }


	public String getAllGroups() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setAllGroups(String string) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Contact) {
			return jid.equals(((Contact)o).getJid());
		} else return false;
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
		//if (!compareNStrings(avatarId, n.avatarId)) {
		//	avatarId = n.avatarId;
		//	updateMark = UPDATE_SAVE;
		//}
		if (!compareNStrings(name, n.name)) {
			name = n.name;
			updateMark = UPDATE_SAVE;
		}
		//TODO: groups
		
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

	public void setActiveChats(boolean has) { activeChats = has; } 

}
