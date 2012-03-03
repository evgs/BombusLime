/*
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

package org.bombusim.lime.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter.BigDecimalLayoutForm;

import org.bombusim.lime.Lime;
import org.bombusim.lime.logger.LimeLog;
import org.bombusim.util.strconv;
import org.bombusim.xmpp.XmppObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Base64;

public final class Vcard {
	private String base64Photo;
	private Bitmap avatar;
	private String jid;
	private String photoHash;
	
	private XmppObject vcard;

	public static final String AVATAR_PENDING = "pending";
	public static final String AVATAR_MISSING = "default";
	
	
	public Vcard(String jid, String hash) {
		this.jid = jid;
		this.photoHash = hash;
		loadCachedAvatar();
	}
	
	public Vcard(String jid, XmppObject vcard) {
		
		this.jid = jid;
		
		if (vcard == null) return;
		
		this.vcard = vcard;
		
		try {
			XmppObject photo = vcard.getChildBlock("PHOTO");
			base64Photo = photo.getChildBlockText("BINVAL");
			
			decodeAvatar();
			saveAvatar();
		
		//TODO: parse fields
		} catch (Exception e) {
			// TODO: handle exception
			photoHash = AVATAR_MISSING;
			e.printStackTrace();
		}
		
	}

	private void decodeAvatar() {
	    //TODO: split by two methods: decodeLargeAvatar (for VCard viewer) and scaleRosterAvatar 
	    
	    Bitmap avatarTmp;
	    
	    int h; int w;
	    
		byte[] photobin = Base64.decode(base64Photo, Base64.DEFAULT);
	
		//1. decode image size
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true; 
		BitmapFactory.decodeByteArray(photobin, 0, photobin.length, opts);
		
		h = opts.outHeight;
		w = opts.outWidth;

		//2. calculate scale factor
		int scaleFactor=1;
		int sz = Lime.getInstance().avatarSize;
		
		int szt=sz*2; // temporary size
		
		while (h>szt && w>szt) {
		    w/=2; h/=2;
		    scaleFactor*=2;
		}
		
		//3. decoding scaled down image
		
		opts = new BitmapFactory.Options();
		opts.inSampleSize = scaleFactor;
		
		avatarTmp = BitmapFactory.decodeByteArray(photobin, 0, photobin.length, opts);
		
		h=avatarTmp.getHeight();
		w=avatarTmp.getWidth();
		
		if (h==0 || w==0) {
			photoHash = AVATAR_MISSING;
			return;
		}
		
		//keep proportions
		if (h>w) {  w = (w*sz)/h; 	h=sz; 	} 
		else     {  h = (h*sz)/w;   w=sz;   }

		Bitmap scaled = Bitmap.createScaledBitmap( avatarTmp, w, h, true);
		
		avatarTmp.recycle();
		
		if (h==w) {
			avatar = scaled;
		} else {
			avatar = Bitmap.createBitmap(sz, sz, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(avatar);
			//TODO: color from theme
			c.drawColor(Color.DKGRAY);
			c.drawBitmap(scaled, (sz-w)/2, (sz-h)/2, null);
		}
		
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			photoHash = strconv.byteArrayToHexString(sha1.digest(photobin));
			LimeLog.d("Img SHA-1", jid, photoHash);
		} catch (NoSuchAlgorithmException e) {
			photoHash = AVATAR_MISSING;
		}
		
	}
	
	
	private void saveAvatar() {
		//TODO: remove stub
		if (photoHash.equals(AVATAR_MISSING)) return;
		if (photoHash.equals(AVATAR_PENDING)) return;
		
		Context context = Lime.getInstance().getApplicationContext();
		String fn = "avatar"+photoHash+".png";
		OutputStream os = null;
		try {
			File cache = context.getCacheDir();
			File f = new File(cache, fn);
			if (!f.createNewFile()) return; //do not rewrite existing file by identical value
			
			os = new BufferedOutputStream(new FileOutputStream(f));
			
			avatar.compress(CompressFormat.PNG, 0, os); //PNG ignores quality parameter
			
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (os!=null) try { os.close(); } catch (Exception ex) { ex.printStackTrace(); }
		}
	}
	
	private boolean loadCachedAvatar() {
		Context context = Lime.getInstance().getApplicationContext();
		String fn = context.getCacheDir() + "/avatar"+photoHash+".png";
		avatar = BitmapFactory.decodeFile(fn);
		
		return avatar !=null;
	}
	
	public Bitmap getAvatar() { return avatar; }

	public String getJid() { return jid; }

	public String getAvatarId() { return photoHash; }
	
	
	public String getField(String field) {
        if (vcard == null) return null;

        String[] fields = field.split("/");
	    XmppObject node = vcard;
	    
	    for (String name : fields) {
	        node = node.getChildBlock(name);
	        if (node == null) return null;
	    }
	    
	    return node.getText();
	}
}
