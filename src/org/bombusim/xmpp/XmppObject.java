/*
 * XmppException.java
 * Created on 2009
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

import java.util.ArrayList;

import org.bombusim.xml.Attributes;
import org.bombusim.xml.XMLUtils;

/**
 * Title:        JabberDataBlock.java
 * Description:  The base class for Jabber datablocks objects in the datablock sub package
 */

public class XmppObject
{
    private final static int MAX_CHILDS=400;

  private String tagName;

  protected ArrayList<XmppObject> childBlocks;

  protected String textData = null;

  protected XmppObject parent;

  protected Attributes attributes;

  /**
   * Default Constructor
   *
   */
  public XmppObject( )   {
    this( "", null, null );
  }

  //public JabberDataBlock( JabberDataBlock _parent )
  //{
  //  this( "unknown", _parent, null );
  //}

  /**
   * Constructor including an Attribute list
   *
   * @param _parent The parent of this datablock
   * @param _attributes The list of element attributes
   */

  public XmppObject( XmppObject parent, Attributes attr )  {
    this( "", parent, attr );
  }

  /**
   * Constructor including an Attribute list
   *
   * @param _tagName The name of the block
   * @param _parent The parent of this datablock
   * @param _attributes The list of element attributes
   */

  public XmppObject( String tagName, XmppObject parent, Attributes attr )  {
    this.parent = parent;
    this.attributes = attr;
    this.tagName = tagName;
  }

  public XmppObject( XmppObject parent, String tagName, String body  ) {
    this( tagName, parent, null );
    setText(body);
  }
  /**
   * Method to add a child to the list of child blocks
   *
   * @param newData The child block to add
   */

  public void addChild( XmppObject newData ) {
      if( childBlocks == null )
	  childBlocks = new ArrayList<XmppObject>();
      if (childBlocks.size()<MAX_CHILDS)
	  childBlocks.add( newData );
  }
  
  /**
   * Method to add a simple child to the list of child blocks
   *
   * @param name The child block name to add
   * @param text The child block text body to add
   */
  public XmppObject addChild(String name, String text){
      XmppObject child=new XmppObject(name,this,null);
      if (text!=null) child.setText(text);
      addChild(child);
      return child;
  }

  /**
   * Method to add a child with namespace
   *
   * @param name The child block name to add
   * @param xmlns Child's namespace
   */
  public XmppObject addChildNs(String name, String xmlns) {
      XmppObject child=addChild(name, null);
      child.setNameSpace(xmlns);
      return child;
  }

  /**
   * Method to add some text to the text buffer for this block
   *
   * @param text The text to add
   */

  public void setText( String text ) { textData=text; }

  /**
   * Method to get the parent of this block
   *
   * @return This blocks parent
   */

  public XmppObject getParent() { return parent; }


  /**
   * Method to return the data as a byte stream ready to send over
   * the wire
   *
   * @return The data to send as a byte array
   */

  public byte[] getBytes()
  {
    String data = toString();
    return data.getBytes();
  }

  /**
   * Method to get the text element of this block
   *
   * @return The text contained in this block
   */

  public String getText()   {
    return (textData==null)?"":textData.toString();
  }

  /**
   * Method to get an attribute
   *
   * @param attributeName The name of the attribute to get
   * @return The value of the attribute
   */

  public String getAttribute( String name ) {
      return attributes.getValue(name);
  }
  
  public String getTypeAttribute(){
      return getAttribute("type");
  }
  
  public boolean compareNameSpace(String xmlns){
      String xmlnsatr=getAttribute("xmlns");
      if (xmlnsatr==null) return false;
      return xmlnsatr.equals(xmlns);
  } 

  public void setNameSpace(String xmlns){
      setAttribute("xmlns", xmlns);
  }
  /**
   * Method to set an attribute value
   *
   * @param attributeName The name of the attribute to set
   * @param value The value of the attribute
   */

  public void setAttribute( String attributeName, String value )  {
      if( attributeName == null )
          return;
      
      if( attributes == null )
          attributes = new Attributes();
      
      attributes.putValue(attributeName, value);
  }

  public void setTypeAttribute( String value ) {
      setAttribute("type",value);
  }
  
  /**
   * Returns a vector holding all of the children of this block
   *
   * @param Vector holding all the children
   */

  public ArrayList<XmppObject> getChildBlocks() {
    return childBlocks;
  }

  /**
   * Returns a child block by  the tagName
   *
   */

  public XmppObject getChildBlock(String byTagName) {
    if (childBlocks==null) return null;
    for (int e=0; e<childBlocks.size(); e++){
        XmppObject d=childBlocks.get(e);
        if (d.getTagName().equals(byTagName)) return d;
    }
    return null;
  }

  /**
   * Returns a child block by text
   *
   */

  public XmppObject getChildBlockByText(String text)
  {
    if (childBlocks==null) return null;
    for (int e=0; e<childBlocks.size(); e++){
        XmppObject d=childBlocks.get(e);
        if ( d.getText().equals(text) ) return d;
    }
    return null;
  }


  public XmppObject findNamespace(String tagName, String xmlns) {
      if (childBlocks==null) return null;
      for (int e=0; e<childBlocks.size(); e++){
          XmppObject d=childBlocks.get(e);
          
          if (tagName!=null) 
        	  if (! tagName.equals(d.tagName)) continue;
          
          if (d.compareNameSpace(xmlns)) return d;
          
      }
      return null;
  }

  /**
   * Method to return the text for a given child block
   */

  public String getChildBlockText( String blockname )
  {
      XmppObject child=getChildBlock(blockname);
      return (child==null)?"":child.getText();
  }
  
  /**
   * Method to convert this into a String
   *
   * @return The element as an XML string
   */

  public String toString()
  {
    StringBuffer data = new StringBuffer();
    constructXML(data);
    return data.toString();
  }

  public void constructXML(StringBuffer data) {
      data.append('<').append( getTagName() );
      
      if( attributes != null )
          attributes.appendAttributes(data);
      
      // short xml
      if (textData==null && childBlocks ==null ) {
          data.append("/>");
          return;
      }
      
      data.append( '>' );
      
      
      XMLUtils.appendPlainText(data, textData);
      
      if( childBlocks != null ) {
          int e = 0;
          while( e<childBlocks.size() ) {
              XmppObject thisBlock = childBlocks.get(e);
              thisBlock.constructXML(data);
              e++;
          }
      }
      
      // end tag
      data.append( "</" ).append( getTagName() ).append( '>' );
  }
  
  /**
   * Method to return the tag name
   *
   * @return The tag name
   */

  public String getTagName()
  {
    return tagName;
  }

    void setTagName(String tagName) { this.tagName=tagName; }
}
