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