package org.bombusim.lime.data;

import java.util.ArrayList;

import org.bombusim.xmpp.XmppAccount;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RosterDbAdapter  {

	protected final static String DATABASE_TABLE = "contacts";
	protected final static String DATABASE_NAME = "contacts.db";
	protected final static int    DATABASE_VERSION = 1;
	
	public final static String KEY_ID =       "_id";
	public final static String KEY_RJID =     "rosterjid";
	
	public final static String KEY_JID =      "jid";
	public final static String KEY_NAME =     "name";
	public final static String KEY_GROUP =    "groups";
	public final static String KEY_SUBSCR =   "subscr";
	public final static String KEY_AVATAR =   "avatar";

	private Context context;
	private ContactDbHelper dbHelper;
	private SQLiteDatabase db;
	
	public RosterDbAdapter(Context context) {
		this.context = context;
		dbHelper = new ContactDbHelper(context);
	}
	
	public void open() {
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLException ex) {
			db = dbHelper.getReadableDatabase();
		}
	}
	
	public void close() { db.close(); }
	
	public long putContact(Contact contact, long position) {
		ContentValues v = new ContentValues();
		v.put(KEY_RJID,     contact.getRosterJid());
		v.put(KEY_JID,      contact.getJid());
		v.put(KEY_NAME,     contact.getName());
		v.put(KEY_GROUP,    contact.getAllGroups());
		v.put(KEY_SUBSCR,   contact.getSubscription());
		v.put(KEY_AVATAR,   contact.getAvatarId());
		
		long id=-1;
		if (position<0) {
			id = db.insert(DATABASE_TABLE, null, v);
		} else {
			id = db.update(DATABASE_TABLE, v, KEY_ID+"="+position, null);
		}
		contact.setId(id);
		return id;
	}

	public long removeContact(long position) {
		return db.delete(DATABASE_TABLE, KEY_ID+"="+position, null);
	}
	
	public long[] getContactIndexes(String rosterJid) {
		String select = (rosterJid !=null )? KEY_RJID+"="+rosterJid : null;
		
		Cursor ind = db.query(DATABASE_TABLE, new String[] {KEY_ID}, select, null, null, null, null);
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
	
	public Contact getContact(long position) {
		Cursor cursor = db.query(DATABASE_TABLE, null, KEY_ID+"="+position, null, null, null, null);
		
		if (cursor.getCount() == 0 || !cursor.moveToFirst()) return null;
		
		Contact c=getContactFromCursor(cursor);
		cursor.close();
		
		return c; 
	}
	
	public Contact getContact(String jid) {
		Cursor cursor = db.query(DATABASE_TABLE, null, KEY_JID+"="+jid, null, null, null, null);
		
		if (cursor.getCount() == 0 || !cursor.moveToFirst()) return null;
		
		Contact c=getContactFromCursor(cursor);
		cursor.close();
		
		return c; 
	}

	
	public Contact getContactFromCursor(Cursor cursor) {
		String jid = cursor.getString(cursor.getColumnIndex(KEY_JID));
		String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
		long id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
		
		Contact c = new Contact(jid, name, id);
		
		c.setRJid(cursor.getString(cursor.getColumnIndex(KEY_RJID)));
		c.setAllGroups(cursor.getString(cursor.getColumnIndex(KEY_GROUP)));
		c.setSubscription(cursor.getInt(cursor.getColumnIndex(KEY_SUBSCR)));
		c.setAvatar(null, cursor.getString(cursor.getColumnIndex(KEY_AVATAR)) );
		
		c.setUpdate(Contact.UPDATE_NONE);

		return c;
	}
		
	private class ContactDbHelper extends SQLiteOpenHelper { 
		private static final String DATABASE_CREATE = 
				"CREATE TABLE " + DATABASE_TABLE + " ("  
				        + KEY_ID  +      " INTEGER PRIMARY KEY AUTOINCREMENT, " 
				        + KEY_RJID +     " TEXT NOT NULL, " 
				        + KEY_JID +      " TEXT NOT NULL, " 
				        + KEY_NAME +     " TEXT, " 
				        + KEY_GROUP +    " TEXT, " 
				        + KEY_AVATAR +   " TEXT, " 
				        + KEY_SUBSCR +   " INTEGER);";
		
		
		public ContactDbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
	
	
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE IF EXISTS" + DATABASE_TABLE);
			
			onCreate(db);
		}
		
	} // AccountDbHelper
}
