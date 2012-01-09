package org.bombusim.lime.data;

public class Resource implements Comparable<Resource>{
	public int presence;
	public int priority;
	public String resource;
	public String statusMessage;
	
	public long seenTime;

	public Resource() {}

	public Resource(int presence, String resource, int priority) {
		this.presence = presence;
		this.resource = resource;
		this.priority = priority;
	}

	@Override
	public int compareTo(Resource another) {
		//TODO: check priority when contact goes offline
		
		int difference =  another.priority - priority;
		if (difference == 0) difference =  presence - another.presence; 
		return difference;
	}
}