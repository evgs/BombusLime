/*
 * XmppJid.java
 * Created on 28.12.2011
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


package org.bombusim.xmpp;

import org.bombusim.util.strconv;

public class XmppJid {
    
    private String bareJid;
    private String resource;
    
    /** Creates a new instance of XmppJid */
    public XmppJid(String s) {
        setJid(s);
    }
    
    public void setJid(String s){
        int resourcePos=s.indexOf('/');
        if (resourcePos<0) {
        	resourcePos=s.length();
        }
        else 
        	resource=s.substring(resourcePos+1);
        
        //TODO: nameprep
        bareJid=strconv.toLowerCase(s.substring(0,resourcePos));
    }
    
    /** Compares two Jids */
    @Override
    public boolean equals(Object o) {
    	if (o instanceof XmppJid)  
    		return equals((XmppJid) o, false);
    	if (o instanceof String)
    		return equals(new XmppJid((String)o), false);
    	
    	return false;
    }
    
    public boolean equals(XmppJid j, boolean compareResource) {
    	try {
    		if (!bareJid.equals(j.bareJid)) return false;
    		if (!compareResource) return true;
    		return (resource.equals(j.resource));
    	} catch (NullPointerException e) { /* normal case */ }
    	
    	return false;
    }
    
    
     /** returns server part of the JID */
     public String getServer(){
        int atIndex=bareJid.indexOf('@'); //-1 if no username,
        return bareJid.substring(atIndex+1); //so substring from 0 index will be here
     }
    
    /** get resource part of the JID */
    public String getResource(){ return resource; }
    
    /** get username part of JID */
    public String getUser(){
        int atIndex=bareJid.indexOf('@');
        if (atIndex<0) return null;
        return bareJid.substring(0, atIndex);
    }
    
    /** get bare JID */
    public String getBareJid(){ return bareJid; }
    
    /** get "username@server/resource" */
    public String getJidResource(){
        if (resource.length()==0) return bareJid;
        return bareJid +'/' +resource;
    }
    
}
