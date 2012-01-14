/*
 * XmppParser.java
 * Created on 1.06.2008
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
import java.util.ArrayList;

import org.bombusim.xml.Attributes;
import org.bombusim.xml.XMLEventListener;
import org.bombusim.xml.XMLException;
import org.bombusim.xmpp.exception.XmppException;
import org.bombusim.xmpp.stanza.Iq;
import org.bombusim.xmpp.stanza.XmppMessage;
import org.bombusim.xmpp.stanza.Presence;

/**
 *
 * @author evgs
 */
public abstract class XmppParser implements XMLEventListener {
	
    protected XmppObject currentBlock;
    
    
    /**
     * The method called when a tag is ended in the stream comming from the
     * server.
     *
     * @param name The name of the tag that has just ended.
     * @throws XMLException 
     * @throws IOException 
     * @throws XmppException 
     */
    
    public void tagEnd(String name) throws XMLException {
        
        ArrayList<XmppObject> childs=currentBlock.getChildBlocks();
        if (childs!=null) childs.trimToSize();

        XmppObject parent = currentBlock.getParent();
        if (parent == null) {
            dispatchXmppStanza(currentBlock);
        }  else
            parent.addChild( currentBlock );
        currentBlock = parent;
    }

    
    /**
     * Method called when an XML tag is started in the stream comming from the
     * server.
     *
     * @param name Tag name.
     * @param attributes The tags attributes.
     */
    
    public boolean tagStart(String name, Attributes attributes) {
        if (currentBlock != null)
        	
            currentBlock = new XmppObject(name, currentBlock, attributes);
            
        else  if (name.equals( "message" ) )
            currentBlock = new XmppMessage(currentBlock, attributes);
        else    if (name.equals("iq") ) 
            currentBlock = new Iq(currentBlock, attributes); 
        else    if (name.equals("presence") ) 
            currentBlock = new Presence(currentBlock, attributes); 
        else    if (name.equals("xml") )
            return false; 
        else
        	//TODO: stub for incorrect stanza
        	currentBlock = new XmppObject(name, null, attributes);
        
        return false;
    }

    protected abstract void dispatchXmppStanza(XmppObject currentBlock) ;
    /**
     * The current class being constructed.
     */
    
   
    /**
     * Method called when some plain text is encountered in the XML stream
     * coming from the server.
     *
     * @param text The plain text in question
     */
    
    public void plainTextEncountered(String text) {
        if( currentBlock != null ) {
            currentBlock.setText( text );
        }
    }
}
