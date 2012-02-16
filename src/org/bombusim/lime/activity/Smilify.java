/*
 * Smilify.java
 *
 * Created on 6.02.2005, 19:38
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

/**
 *
 * @author Eugene Stahov
 */

package org.bombusim.lime.activity;

import java.util.ArrayList;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;


public class Smilify {
	private TypedArray smileTA;
	//We should not store Drawable objects in this way because of memory leaks
	//private Drawable[] smileBitmaps;
	private int smileIndexes[];
	private String smileTags[];
	
	private SmileNode rootSmile;
	
    private final static int NOSMILE=-1;

    private class SmileNode {
        public int smile=NOSMILE;   // has no smile in node
        public String smileChars;     // символы смайликов
        public ArrayList<SmileNode> childNodes;

        public SmileNode() {
            childNodes=new ArrayList<SmileNode>();
            smileChars=new String();
        }
        
        public SmileNode findChild(char c){
            int index=smileChars.indexOf(c);
            return (index==-1)? null : childNodes.get(index);
        }

        private void addChild(char c, SmileNode child){
            childNodes.add(child);
            smileChars=smileChars+c;
        }
    }
    
    private void addSmile(String smile, int index) {
		SmileNode p=rootSmile;   // этой ссылкой будем ходить по дереву
		SmileNode p1;
		
		int len=smile.length();
		for (int i=0; i<len; i++) {
		    char c=smile.charAt(i);
		    p1=p.findChild(c);
		    if (p1==null) {
		    	p1=new SmileNode();
		    	p.addChild((char)c,p1);
		    }
		    p=p1;
		}
		p.smile=index;
    }
    
    public Smilify() {
    	smileTags = Lime.getInstance().getResources().getStringArray(R.array.smileTags);
    	smileIndexes = Lime.getInstance().getResources().getIntArray(R.array.smileIds);
    	
    	
    	//smileBitmaps = new Drawable[smileIndexes.length];
    	
        rootSmile=new SmileNode();

        for (int strnumber = 0; strnumber < smileIndexes.length; strnumber++) {
            
           
        	String smileTag=smileTags[strnumber];
        	String synonims[] = smileTag.split(" ");
        	
        	for (String s: synonims) {
        		addSmile(s, strnumber);
        	}
        	
        	smileTags[strnumber] = synonims[0];
        }
    }

    public Drawable getSmileDrawable(int index) {
    	    	
		Drawable d = getSmileTA().getDrawable(index);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());

        return d;
    }

	private TypedArray getSmileTA() {
		if(smileTA == null) {
    		smileTA = Lime.getInstance().getResources().obtainTypedArray(R.array.smileIds);
    	}
		
		return smileTA;
	}
    
    public int getCount(){ return getSmileTA().length(); } 
    
    public String getSmileText(int index) {
    	return smileTags[index];
    }
    
    
    //TODO: cache smiles in activity context
	public void addSmiles(Spannable s) {

		ClickableSpan existingSpans[] = s.getSpans(0, s.length(), ClickableSpan.class);

		int pos=0;
        while (pos<s.length()) {
            SmileNode smileLeaf=rootSmile;
            int smileIndex=NOSMILE;
            int smileStartPos=pos;
            int smileEndPos=pos;
            
            while (pos<s.length()) { // inner loop
                char c=s.charAt(pos);
                
                smileLeaf=smileLeaf.findChild(c);
                if (smileLeaf==null) {
                    break;    //этот символ c не попал в смайл
                }
                if (smileLeaf.smile!=NOSMILE) {
                    // нашли смайл
                    smileIndex=smileLeaf.smile;
                    smileEndPos=pos;
                }
                pos++; // продолжаем поиск смайла
                
            } //while (pos<s.length()) // inner loop
            
            //check spans collisions
            if (smileIndex!=NOSMILE) {
            	for (ClickableSpan existing : existingSpans) {
            		if (s.getSpanStart(existing)>smileEndPos) continue;
            		if (s.getSpanEnd  (existing)<smileStartPos) continue;
            		smileIndex = NOSMILE;
            	}
            }
            
            if (smileIndex!=NOSMILE) {
                // got a smile
            	
            	
        		s.setSpan(
        				new ImageSpan(getSmileDrawable(smileIndex), ImageSpan.ALIGN_BOTTOM),
						smileStartPos, smileEndPos+1, 
						Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            } else {
            	pos++;
            }
		
        }
	}
}
