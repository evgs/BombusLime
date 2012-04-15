package org.bombusim.lime.data;

import java.util.ArrayList;
import java.util.Collections;

public class RosterGroup implements Comparable<RosterGroup>{
    public String rJid;
    
	public String groupName;
	public ArrayList<Contact> contacts;
	public boolean collapsed;
	
	public int onlineCount;
	
	public RosterGroup(String name, String rJid) {
		this.groupName = name;
		this.rJid = rJid;
		
		contacts = new ArrayList<Contact>();
	}

	@Override
	public int compareTo(RosterGroup another) {
		return groupName.compareToIgnoreCase(another.groupName);
	}

	public void toggleCollapsed() {
		collapsed = !collapsed;
	}

    public void clear() {
        contacts.clear();
        onlineCount = 0;
    }

    public void sort() {
        Collections.sort(contacts);
    }
}
