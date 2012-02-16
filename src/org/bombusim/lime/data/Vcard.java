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

import org.bombusim.lime.Lime;
import org.bombusim.lime.logger.LimeLog;
import org.bombusim.util.strconv;
import org.bombusim.xmpp.XmppObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Base64;

public final class Vcard {
	private String base64Photo;
	private Bitmap avatar;
	private String jid;
	private String photoHash;

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
		byte[] photobin = Base64.decode(base64Photo, Base64.DEFAULT);
	
		int sz = Lime.getInstance().avatarSize;
		avatar = BitmapFactory.decodeByteArray(photobin, 0, photobin.length);
		avatar = Bitmap.createScaledBitmap( avatar, sz, sz, true);
		
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
}
