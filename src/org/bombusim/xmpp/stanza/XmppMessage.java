package org.bombusim.xmpp.stanza;

import java.util.ArrayList;

import org.bombusim.xml.Attributes;
import org.bombusim.xmpp.XmppObject;
import org.bombusim.xmpp.XmppError;


/**
 * Title:        Message.java
 * Description:  The class representing a Jabber message object
 */

public final class Message extends XmppObject
{
  /**
   * Constructor. Prepares the message destination and body
   *
   * @param to The destination of the message
   * @param message The message text
   */

  public Message( String to, String message , String subject, boolean groupchat)
  {
    super();

    setAttribute( "to", to );
    if( message != null )
      setBodyText( message );
    if (subject!=null) 
        setSubject(subject);
    setTypeAttribute((groupchat)?"groupchat":"chat");
  }

  /**
   * Constructor. Prepares the message destination
   *
   * @param to The destination of the message
   */

  public Message( String to ) {
      super();
    setAttribute( "to", to );
  }

  /**
   * Constructor for incomming messages
   *
   * @param _parent The parent of this datablock
   * @param _attributes The list of element attributes
   */

  public Message( XmppObject parent, Attributes attributes ) {
    super( parent, attributes );
  }

  /**
   * Method to set the body text. Creates a block with body as it's tag name
   * and inserts the text into it.
   *
   * @param bodyText The string to go in the message body
   */

  public void setBodyText( String text )
  {
    addChild( "body", text );
  }

  /**
   * Method to set the body text written in HTML. Creates a block with html as
   * it's tag name in the xhtml name space and inserts the html into it.
   *
   * @param html The html to go in the message body
   */

  /*
  public void setHTMLBodyText( String html )
  {
    JabberDataBlock body = new JabberDataBlock( "html", null, null );
    body.setNameSpace( "http://www.w3.org/1999/xhtml" );
    body.addText( html );
    addChild( body );
  }
   */

  /**
   * Method to set the message thread. Creates a block with thread as it's tag
   * name and inserts the thread name into it.
   *
   * @param threadName The string to go in the thread block
   */

  /*public void setThread( String text )
  {
    JabberDataBlock thread = new JabberDataBlock( "thread", null, null );
    thread.addText( text );
    addChild( thread );
  }*/

  /**
   * Method to set the subject text. Creates a subject block and inserts the text into it.
   *
   * @param text The string to go in the message subject
   */

  public void setSubject( String text ) { addChild( "subject", text ); }


  /**
   * Method to get the message subject
   *
   * @return A string representing the message subject
   */

  public String getSubject() {  return getChildBlockText( "subject" );  }

  /**
   * Method to get the message body
   *
   * @return The message body as a string
   */

  public String getBody() { 
      String body=getChildBlockText( "body" ); 
      
      XmppObject error=getChildBlock("error");
      if (error==null) return body;
      return body+"Error\n"+XmppError.decodeStanzaError(error).toString();
  }
  
  /*
  public long getMessageTime(){
      try {
          return Time.dateIso8601(
                  findNamespace("x", "jabber:x:delay").getAttribute("stamp")
                  );
      } catch (Exception e) { }
      try {
          return Time.dateIso8601(
                  findNamespace("delay", "urn:xmpp:delay").getAttribute("stamp")
                  );
      } catch (Exception e) { }
      return 0; //0 means no timestamp
  }
  */

  /**
   * Get the tag start marker
   *
   * @return The block start tag
   */

  public String getTagName()
  {
    return "message";
  }

  /**
     * Method to get the message from field
     * @return <B>from</B> field as a string
     */
    public String getXFrom() {
	//try {
	//    // jep-0146
	//    JabberDataBlock fwd=findNamespace("jabber:x:forward"); // DEPRECATED
	//    JabberDataBlock from=fwd.getChildBlock("from");
	//    return from.getAttribute("jid");
	//} catch (Exception ex) { /* normal case if not forwarded message */ };
	
	try {
	    // jep-0033 extended stanza addressing from psi
	    ArrayList<XmppObject> addresses=getChildBlock("addresses").getChildBlocks();
	    
	    for (int index=0; index<addresses.size(); index++) { 
	    	XmppObject adr=addresses.get(index);
	    	if (adr.getTypeAttribute().equals("ofrom")) return adr.getAttribute("jid");
	    }
	} catch (Exception e) { /* normal case if not forwarded message */ };
        return getAttribute("from");
    }
    
    public String getFrom() {
        return getAttribute("from");
    }
}
