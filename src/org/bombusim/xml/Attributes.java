/*
 * XMLAttributes.java
 *
 * Created on 23.12.2011
 *
 * Copyright (c) 2005-2011, Eugene Stahov (evgs@bombus-im.org), 
 * http://bombus-im.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * You can also redistribute and/or modify this program under the
 * terms of the Psi License, specified in the accompanied COPYING
 * file, as published by the Psi Project; either dated January 1st,
 * 2005, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.bombusim.xml;

import java.util.ArrayList;

public final class Attributes {
	private ArrayList<String> values;
	
	public Attributes() {
		
	}
	
	public void putValue(String key, String value) {
		if (values==null) values = new ArrayList<String>();
		
		int index=0;
		
		while (index<values.size()) {
			if ( key.equals(values.get(index)) ) {
				if (value!=null) {
					values.set(index+1, value);
				} else {
					values.remove(index+1);
					values.remove(index);
				}
				return;
			}
			index+=2;
		}
		
		if (value==null) return;
		values.add(key);
		values.add(value);
	}
	
	public String getValue(String key) {
		if (values==null) return null;
		
		int index=0;
		
		while (index<values.size()) {
			if ( key.equals(values.get(index)) ) {
				return values.get(index+1);
			}
			index+=2;
		}
		
		return null;
	}
	
	public String toString() {
		StringBuilder sb=new StringBuilder();
		
		appendAttributes(sb);
		
		return sb.toString();
	}

	public void appendAttributes(StringBuilder sb) {
		int index=0;
		if (values==null) return;
		
		while (index<values.size()) {
			sb.append(' ');
			sb.append( values.get(index) )
			  .append("='")
			  .append( values.get(index+1) )
			  .append("'");
			index+=2;
		}
	}
}
