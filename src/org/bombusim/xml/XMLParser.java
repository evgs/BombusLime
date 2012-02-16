/*
 * XMLParser.java
 *
 * Created on 22.03.2008, 1:02
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

package org.bombusim.xml;

import java.io.IOException;

public class XMLParser {
        
    private final static int PLAIN_TEXT=0;
    private final static int TAGNAME=1;
    private final static int ENDTAGNAME=2;
    private final static int ATRNAME=3;
    private final static int ATRVALQS=4;
    private final static int ATRVALQD=5;
    private final static int CDATA=6;

    private int state;
    
    private XMLEventListener eventListener;
    
    private StringBuilder sbuf;
    
    private StringBuilder tagName;
    private Attributes attr;
    private String atrName;
    
    int ibuf;
    int padding;
    /** Creates a new instance of XMLParser */
    
    public XMLParser(XMLEventListener eventListener) {
        this.eventListener=eventListener;
        state=PLAIN_TEXT;
        sbuf=new StringBuilder();
        tagName=new StringBuilder();
    }
 
    public void parse(byte indata[], int size) throws XMLException, IOException{
        int dptr=0;
        while (size>0) {
            size--;
            char c=(char)(indata[dptr++] &0xff);
            switch (state) {
                case PLAIN_TEXT: {
                    //parsing plain text
                    if (c=='<') {
                        state=TAGNAME;
                        
                        if (sbuf.length()>0) 
                            eventListener.plainTextEncountered( XMLUtils.parsePlainText(sbuf) ); 
                        
                        sbuf.setLength(0);
                        tagName.setLength(0);
                        attr=new Attributes();
                        
                        continue;
                    }
                    sbuf.append(c); continue;
                }

        case ATRNAME:
            {
                if (c=='?') continue;
                if (c==' ') continue;
                if (c=='=') continue;
                if (c=='\'') { state=ATRVALQS; atrName=sbuf.toString(); sbuf.setLength(0); continue; }
                if (c=='\"') { state=ATRVALQD; atrName=sbuf.toString(); sbuf.setLength(0); continue; }

                if (c!='>' && c!='/') { 
                    sbuf.append(c);
                    continue;
                } else {
                    state=TAGNAME;
                    sbuf.setLength(0);
                }
            }

        case TAGNAME:
            {
                if (c=='?') continue;
                if (c=='/') { 
                    state=ENDTAGNAME; 
                    sbuf.setLength(0);
                    if (tagName.length()>0) {
                        String tn=tagName.toString();
                        eventListener.tagStart(tn, attr); 
                        sbuf.append(tn);
                    }
                    continue; 
                }
                if (c==' ') { state=ATRNAME; continue; }
                if (c=='>') { 
                    state=PLAIN_TEXT; 
                    eventListener.tagStart(tagName.toString(), attr);
                    
                    continue; 
                }
                tagName.append(c);

                if (c=='[') {
                    if (tagName.toString().equals("![CDATA[")) 
                        state=CDATA;
                    continue;
                }

                continue;
            }

        case CDATA:
            {
                sbuf.append(c);
                if (c=='>') {
                    int e3=sbuf.length()-3;
                    if (e3 < 0) continue;
                    if (sbuf.charAt(e3) != ']') continue;
                    if (sbuf.charAt(e3+1) != ']') continue;
                    //if (sbuf[e3] != '>') continue;
                    sbuf.setLength(e3);
                    state=PLAIN_TEXT;
                    continue;
                }
                continue;
            }
        case ENDTAGNAME:
            {
                if (c==' ') continue;
                if (c=='>') {
                    state=PLAIN_TEXT;
                    eventListener.tagEnd(sbuf.toString());
                    sbuf.setLength(0);
                    continue;
                }
                sbuf.append(c);
                continue;
            }
            
        case ATRVALQS: 
            {
                if (c=='\'') { 
                    state=ATRNAME; 
                    attr.putValue(atrName, XMLUtils.parsePlainText(sbuf));
                    sbuf.setLength(0); 
                    continue; 
                }
                sbuf.append(c);
                continue;
            }
        case ATRVALQD: 
            {
                if (c=='\"') { 
                    state=ATRNAME; 
                    attr.putValue(atrName, XMLUtils.parsePlainText(sbuf));
                    sbuf.setLength(0); 
                    continue; 
                }
                sbuf.append(c);
                continue;
            }
        
        }
    }
        
    };
    
/*    public void pushOutPlainText() throws XMLException {
        if (state==PLAIN_TEXT) {
            if (sbuf.length()>0)
                eventListener.plainTextEncountered( parsePlainText(sbuf) );
            
            sbuf.setLength(0);
        }
    }
    */
    

/*
    public final static String extractAttribute(String attributeName, ArrayList<String> attributes) {
      if (attributes==null) return null;
      int index=0;
      while (index<attributes.size()) {
          if ( attributes.get(index).equals(attributeName) )
              return attributes.get(index);
          
          index+=2;
      }
      
      return null;
    }
    */
}
