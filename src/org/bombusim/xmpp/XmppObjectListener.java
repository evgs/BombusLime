/*
 * XmppException.java
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


package org.bombusim.xmpp;

import java.io.IOException;

import org.bombusim.xmpp.exception.XmppException;

/**
 *
 * @author evgs
 */
public abstract class XmppObjectListener  implements Comparable<XmppObjectListener>{
   public final static int BLOCK_REJECTED=0;
   public final static int BLOCK_PROCESSED=1;
   public final static int NO_MORE_BLOCKS=2;

   
   public static final int PRIORITY_STREAM             = 50;
   public static final int PRIORITY_AUTH               = 100;
   public static final int PRIORITY_AUTH_FALLBACK      = 500;
   public static final int PRIORITY_MUC                = 1000;
   public static final int PRIORITY_BASIC_IM           = 2000;
   public static final int PRIORITY_DISCO              = 2000;
   public static final int PRIORITY_IQUNKNOWN_FALLBACK = 4000;
   

   
  /**
   * Method to handle an incomming block.
   *
   * @parameter data The incomming block
   */

    abstract public int blockArrived(XmppObject data, XmppStream stream) throws IOException, XmppException;
  
    public String capsXmlns() { return null; }
  
    public int priority() { return PRIORITY_BASIC_IM; }
  
  @Override
	public int compareTo(XmppObjectListener another) {
		return priority() - another.priority();
	}
}
