package org.bombusim.lime.data;

import java.util.ArrayList;

public class RosterGroup implements Comparable<RosterGroup>{
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

	public void toggleCollapsed() {
		collapsed = !collapsed;
	}
}
