/*
 * XmppAccount.java
 *
 * Created on 27.12.2011, 23:34
 *
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

package org.bombusim.xmpp;

public class XmppAccount {
	
	public long _id;

	public boolean active = true;
	
	//////////////////// generic settings  //////////////////////
	public String userJid;
	public String password;
	
	public boolean savedPassword = true;
	//////////////////// advanced settings //////////////////////
	public String resource = "mobile";
	
	public int priority = DEFAULT_PRIORITY;
	
	public boolean autoLogin;

	public boolean specificHostPort = false;
	public String xmppHost = null;
	public int    xmppPort = DEFAULT_XMPP_PORT;
	
	public final static int SECURE_CONNECTION_DISABLED=0;
	public final static int SECURE_CONNECTION_IF_AVAILABLE=1;
	public final static int SECURE_CONNECTION_ALWAYS=2;
	public final static int SECURE_CONNECTION_LEGACY_SSL=3;
	
	public int secureConnection = SECURE_CONNECTION_IF_AVAILABLE;
	
	public final static int PLAIN_AUTH_ALWAYS = 0;
	public final static int PLAIN_AUTH_OVER_SSL = 1;
	public final static int PLAIN_AUTH_NEVER = 2;

	public static final int DEFAULT_XMPP_PORT        = 5222;
	public static final int DEFAULT_SECURE_XMPP_PORT = 5223;
	public static final int DEFAULT_PRIORITY = 50;
	public static final int DEFAULT_PRIORITY_ACCOUNT = -255;
	
	public int enablePlainAuth = PLAIN_AUTH_OVER_SSL;
	
	public boolean trafficCompression = true;
	
	public XmppAccount() {
		//TODO: resource initialization here
		_id = -1;
	};
	
	//temporary
	boolean runtimeIsSecure;
	int runtimeStatus;
	
	public XmppAccount(String userJid, String password) {
		this();
		this.userJid  = userJid;
		this.password = password;
	}

	//TODO: own group holder class
	public int getRuntimeStatus() { return runtimeStatus; }
	public boolean connectionIsSecure() { return runtimeIsSecure; }
	
	public boolean collapsed = false;

	public void toggleCollapsed() {
		collapsed = !collapsed;
	}

}
