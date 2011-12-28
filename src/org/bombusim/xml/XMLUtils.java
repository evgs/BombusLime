/*
 * XMLUtils.java
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

public final class XMLUtils {
    public final static String parsePlainText(StringBuffer sb) throws XMLException {
        //1. output text length will be not greather than source
        //2. sb may be destroyed - all calls to parsePlainText succeeds flushing of sb
        int ipos=0;
        int opos=0;
        while (ipos<sb.length()) {
            char c=sb.charAt(ipos++);
            if (c=='&') { 
                StringBuffer xmlChar=new StringBuffer(6);
                while (true) {
                    c=sb.charAt(ipos++);
                    if (c==';') break;
                    xmlChar.append(c);
                }
                String s=xmlChar.toString();

                if (s.equals("amp")) c='&'; else
                if (s.equals("apos")) c='\''; else
                if (s.equals("quot")) c='\"'; else
                if (s.equals("gt")) c='>'; else
                if (s.equals("lt")) c='<'; else
                if (xmlChar.charAt(0)=='#') {
                    xmlChar.deleteCharAt(0);
                    c=(char)Integer.parseInt(xmlChar.toString());
                }
                sb.setCharAt(opos++, c); 
                continue;
            };
            if (c<0x80) { 
                sb.setCharAt(opos++, c); 
                continue; 
            }

            if (c<0xc0) throw new XMLException("Bad UTF-8 Encoding encountered");

            char c2=sb.charAt(ipos++);
            if (c2<0x80) throw new XMLException("Bad UTF-8 Encoding encountered");

            if (c<0xe0) {
                sb.setCharAt(opos++, (char)(((c & 0x1f)<<6) | (c2 &0x3f)) );
                continue;
            }

            char c3=sb.charAt(ipos++);
            if (c3<0x80) throw new XMLException("Bad UTF-8 Encoding encountered");

            if (c<0xf0) {
                sb.setCharAt(opos++, (char)(((c & 0x0f)<<12) | ((c2 &0x3f) <<6) | (c3 &0x3f)) );
                continue;
            }
            
            char c4=sb.charAt(ipos++);
            if (c4<0x80) throw new XMLException("Bad UTF-8 Encoding encountered");
        
            //return ((chr & 0x07)<<18) | ((chr2 &0x3f) <<12) |((chr3 &0x3f) <<6) | (chr4 &0x3f);
            sb.setCharAt(opos++, '?'); // java char type contains only 16-bit symbols
            continue;
            
        }
        
        sb.setLength(opos);
        return sb.toString();
    }

    
    public final static void appendPlainText(StringBuffer dest, String src){
        if (src==null) return;
        int len=src.length();
        for (int i=0;i<len;i++){
            char ch=src.charAt(i);
            switch (ch) {
                case '&':   dest.append("&amp;"); continue;
                case '"':   dest.append("&quot;"); continue;
                case '<':   dest.append("&lt;"); continue;
                case '>':   dest.append("&gt;"); continue;
                case '\'':  dest.append("&apos;"); continue;
                case 0:     continue; //cutout any zeroes
                default: 
                	//UTF-8 encoding
                    //TODO: ескэйпить коды <0x20
                	if (ch<=0x7f) { 
                		dest.append(ch); 
                		continue; 
                	}
                	
                    if (((ch >= 0x80) && (ch <= 0x7ff)) /*TODO: WTF?  || (ch==0)) */ ) {
                        dest.append((char)(0xc0 | (0x1f & (ch >> 6))))
                            .append((char)(0x80 | (0x3f & ch)));
                    }
                    if ((ch >= 0x800) && (ch <= 0xffff)) {
                        dest.append( (char)(0xe0 | (0x0f & (ch >> 12))) )
                            .append( (char)(0x80 | (0x3f & (ch >>  6))) )
                            .append( (char)(0x80 | (0x3f & (ch)      )) );
                    }
            }
        }
    }

}
