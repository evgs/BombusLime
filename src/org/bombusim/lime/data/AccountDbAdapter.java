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

import org.bombusim.xmpp.XmppAccount;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AccountDbAdapter  {

	protected final static String DATABASE_TABLE = "accounts";
	protected final static String DATABASE_NAME = "accounts.db";
	protected final static int    DATABASE_VERSION = 3;
	
	// v1
	public final static String KEY_ID =       "_id";
	public final static String KEY_JID =      "jid";
	public final static String KEY_PASSWORD = "password";
	public final static String KEY_PWDSAVED = "pwdsaved";
	public final static String KEY_RESOURCE = "resource";
	public final static String KEY_HOSTPORT = "hostport";
	public final static String KEY_XMPPHOST = "xmpphost";
	public final static String KEY_XMPPPORT = "xmppport";
	public final static String KEY_SECURITY = "security";
	public final static String KEY_PLAINPWD = "pwdplain";
	public final static String KEY_ZLIB =     "zlib";
	// v2
	public final static String KEY_PRIORITY = "priority";
	public final static String KEY_AUTOLOGIN = "autologin";

	// v3
	public final static String KEY_ACTIVE =    "active";

	private AccountDbHelper dbHelper;
	private SQLiteDatabase db;
	
	public AccountDbAdapter(Context context) {
		dbHelper = new AccountDbHelper(context);
	}
	
	public void open() {
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLException ex) {
			db = dbHelper.getReadableDatabase();
		}
	}
	
	public void close() { db.close(); }
	
	public long putAccount(XmppAccount account, long position) {
		ContentValues v = new ContentValues();
		v.put(KEY_JID,      account.userJid);
		v.put(KEY_PASSWORD, account.password);
		v.put(KEY_PWDSAVED, 1);
		v.put(KEY_RESOURCE, account.resource);
		v.put(KEY_PRIORITY, account.priority);
		v.put(KEY_HOSTPORT, account.specificHostPort? 1:0);
		v.put(KEY_XMPPHOST, account.xmppHost);
		v.put(KEY_XMPPPORT, account.xmppPort);
		v.put(KEY_SECURITY, account.secureConnection);
		v.put(KEY_PLAINPWD, account.enablePlainAuth);
		v.put(KEY_ZLIB, account.trafficCompression);
		v.put(KEY_AUTOLOGIN, account.autoLogin? 1:0);
		v.put(KEY_ACTIVE,   account.active? 1:0);
		
		if (position<0) {
			return db.insert(DATABASE_TABLE, null, v);
		} else {
			return db.update(DATABASE_TABLE, v, KEY_ID+"="+position, null);
		}
	}

	public long removeAccount(long position) {
		return db.delete(DATABASE_TABLE, KEY_ID+"="+position, null);
	}
	
	public long[] getAccountIndexes() {
		Cursor ind = db.query(DATABASE_TABLE, new String[] {KEY_ID}, null, null, null, null, null);
		int count = ind.getCount(); 
		if (count == 0 || !ind.moveToFirst()) { ind.close(); return null; }
		long[] result = new long[count];
		
		int id = ind.getColumnIndex(KEY_ID);
		
		for (int i=0; i<count; i++) {
			result[i]=ind.getLong(id);
			ind.moveToNext();
		}
		
		ind.close();
		return result;
	}
	
	public XmppAccount getAccount(long position) {
		Cursor cursor = db.query(DATABASE_TABLE, null, KEY_ID+"="+position, null, null, null, null);
		
		if (cursor.getCount() == 0 || !cursor.moveToFirst()) return null;
		
		XmppAccount a=getAccountFromCursor(cursor);
		cursor.close();
		
		return a; 
	}
	
	public XmppAccount getAccountFromCursor(Cursor cursor) {
		XmppAccount a = new XmppAccount();
		a.userJid            = cursor.getString(cursor.getColumnIndex(KEY_JID));
		a.password           = cursor.getString(cursor.getColumnIndex(KEY_PASSWORD));
		a.savedPassword      = cursor.getInt(cursor.getColumnIndex(KEY_JID)) !=0;
		a.resource           = cursor.getString(cursor.getColumnIndex(KEY_RESOURCE));
		a.priority           = cursor.getInt(cursor.getColumnIndex(KEY_PRIORITY));
		a.specificHostPort   = cursor.getInt(cursor.getColumnIndex(KEY_HOSTPORT)) !=0;
		a.xmppHost           = cursor.getString(cursor.getColumnIndex(KEY_XMPPHOST));
		a.xmppPort           = cursor.getInt(cursor.getColumnIndex(KEY_XMPPPORT));
		a.secureConnection   = cursor.getInt(cursor.getColumnIndex(KEY_SECURITY));
		a.enablePlainAuth    = cursor.getInt(cursor.getColumnIndex(KEY_PLAINPWD));
		a.trafficCompression = cursor.getInt(cursor.getColumnIndex(KEY_ZLIB)) !=0;
		a.autoLogin          = cursor.getInt(cursor.getColumnIndex(KEY_AUTOLOGIN)) !=0;
		a.active             = cursor.getInt(cursor.getColumnIndex(KEY_ACTIVE)) !=0;

		return a;
	}
		
	private class AccountDbHelper extends SQLiteOpenHelper { 
		private static final String DATABASE_CREATE = 
				"CREATE TABLE " + DATABASE_TABLE + " ("  
				        + KEY_ID  +      " INTEGER PRIMARY KEY AUTOINCREMENT, " 
				        + KEY_JID +      " TEXT NOT NULL, " 
				        + KEY_PASSWORD + " TEXT, " 
				        + KEY_RESOURCE + " TEXT, " 
				        + KEY_PRIORITY + " INTEGER, " 
				        + KEY_HOSTPORT + " INTEGER, " 
				        + KEY_XMPPHOST + " TEXT, " 
				        + KEY_XMPPPORT + " INTEGER, " 
				        + KEY_SECURITY + " INTEGER, " 
				        + KEY_PLAINPWD + " INTEGER, " 
				        + KEY_PWDSAVED + " INTEGER, " 
	                    + KEY_AUTOLOGIN + " INTEGER, "
	                    + KEY_ACTIVE   + " INTEGER, "
				        + KEY_ZLIB     + " INTEGER);";
		
		
		public AccountDbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
	
	
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// until 0.5 we wouldn't do any specific upgrade, just table drop
			
			
			// upgrade 2 to 3
			if (oldVersion  == 2) {
				db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN " + KEY_ACTIVE + " INTEGER");
				return;
			}
			
			
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			
			onCreate(db);
		}
		
	} // AccountDbHelper
}
