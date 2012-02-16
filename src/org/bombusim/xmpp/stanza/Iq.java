/*
 * Iq.java
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
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 */

package org.bombusim.xmpp.stanza;

import org.bombusim.xml.Attributes;
import org.bombusim.xmpp.XmppObject;


/**
 * Class representing the iq message block
 */

public final class Iq extends XmppObject
{
    public final static int TYPE_SET=0;
    public final static int TYPE_GET=1;
    public final static int TYPE_RESULT=2;
    public final static int TYPE_ERROR=3;
    
  /**
   * Constructor including an Attribute list
   *
   * @param _parent The parent of this datablock
   * @param _attributes The list of element attributes
   */

  public Iq( XmppObject parent, Attributes attributes )
  {
    super( parent, attributes );
  }
  
  public Iq( String to, int typeSet, String id) {
      super();
      setAttribute("to", to);
      String type;
      switch (typeSet) {
          case TYPE_SET: type="set"; break;
          case TYPE_GET: type="get"; break;
          case TYPE_ERROR: type="error";
          default: type="result";
      }
      setAttribute("type", type);
      setAttribute("id", id);
  }


  /**
   * Method to return the tag name
   *
   * @return Always the string "iq".
   */
  public String getTagName()
  {
    return "iq";
  }
}
