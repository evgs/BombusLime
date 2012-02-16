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
              
package org.bombusim.lime.activity;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.data.Contact;
import org.bombusim.lime.service.XmppServiceBinding;
import org.bombusim.xmpp.XmppJid;
import org.bombusim.xmpp.handlers.IqRoster;
import org.bombusim.xmpp.stanza.Presence;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class EditContactActivity extends Activity {
	public static final String JID = "jid";
	public static final String MY_JID = "fromJid";
	
	private String jid;
	private String rJid;
	
	private XmppServiceBinding serviceBinding;
	
	boolean isNewContact;
	
	Contact contact;
	
	TreeMap<String, CheckBox> groups;
	
	EditText editJid;
	EditText editNick;
	
	Button buttonAddGroup;
	Button buttonResolveFromVCard;

	Button buttonSave;
	Button buttonCancel;
	
	LinearLayout groupLayout;
	
	CheckBox askSubscription;
	
	ImageView photo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
        Bundle params = getIntent().getExtras();
        if (params == null) throw new InvalidParameterException("No parameters specified for EditContactActivity");
        jid = params.getString(JID);
        rJid = params.getString(MY_JID);
        
        setContentView(R.layout.contact_editor);

        groupLayout = (LinearLayout) findViewById(R.id.groupLayout);
        
        photo =    (ImageView) findViewById(R.id.photo);
        editJid =  (EditText) findViewById(R.id.jid);
        editNick = (EditText) findViewById(R.id.nick);
        
        buttonResolveFromVCard = (Button) findViewById(R.id.buttonResolveFromVCard);
        buttonAddGroup =         (Button) findViewById(R.id.buttonAddGroup);
        
        buttonSave =             (Button) findViewById(R.id.buttonSave);
        buttonCancel =           (Button) findViewById(R.id.buttonCancel);
        
        askSubscription =        (CheckBox) findViewById(R.id.askSubscr);
        
        isNewContact = (jid == null);

        serviceBinding = new XmppServiceBinding(this);

        if (!isNewContact)
        	contact = Lime.getInstance().getRoster().findContact(jid, rJid);
        
        setTitle((isNewContact)? R.string.newContactTitle : R.string.editContactTitle);

        //if new contact or contact was deleted while we creating this activity
        if (contact==null)
        	contact = new Contact(null, null);

        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(editJid.getWindowToken(), 0);
        mgr.hideSoftInputFromWindow(editNick.getWindowToken(), 0);
        
        if (!isNewContact) {
        	editJid.setText(contact.getJid());
        	editJid.setEnabled(false);
        	editNick.setText(contact.getName());
        	//editNick.requestFocus();
        	
        	Bitmap avatar = contact.getAvatar();
        	if (avatar !=null) photo.setImageBitmap(avatar);
        	
        	askSubscription.setVisibility(View.GONE);
        } else {
        	buttonSave.setText(R.string.add);
        }
        
        groups = new TreeMap<String, CheckBox>();
        
        //populating groups
        ArrayList<Contact> contacts = Lime.getInstance().getRoster().getContacts();
        for (Contact rc : contacts) {
        	//check if contact is from our roster
        	if (!rc.getRosterJid().equals(rJid)) continue;
        	
        	//check if contact is in any group
        	String rcGroupsArray[] = rc.getAllGroupsArray();
        	
        	for (String group : rcGroupsArray) {
        		addGroup(group);
        	}
        }
        
        //select groups contact belongs to
        String[] cGroupArray = contact.getAllGroupsArray();
        for (String group : cGroupArray) {
        	setGroupSelected(group, true);
        }

        
        buttonResolveFromVCard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//disable until VCard arrived
				buttonResolveFromVCard.setEnabled(false);
			}
		});
        
        buttonAddGroup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doAddGroupDialog();
			}
		});
        
        buttonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { finish(); }
		});
        
        buttonSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (saveChanges()) finish();
			}
		});
        
        updateGroups();
	}
	
	protected boolean saveChanges() {
		// 1. updating jid
		XmppJid jid = new XmppJid(editJid.getText().toString());
		if (!jid.isValid()) {
			Toast.makeText(this, R.string.invalidJid, Toast.LENGTH_LONG).show();
			return false;
		}
		
		if (isNewContact) contact = new Contact(jid.getBareJid(), null);
		
		// 2. updating nickname
		String name = editNick.getText().toString();
		if (name.length()==0) name = null;
		contact.setName(name);
		
		//3. updating groups
		contact.setAllGroups(null); //reset groups
		
		for (CheckBox group : groups.values()) {
			if (!group.isChecked()) continue;
			contact.addGroup(group.getText().toString());
		}
		
		//4. set our roster jid
		contact.setRJid(rJid);
		
		//5. publishing changes
		try {
			IqRoster.setContact(contact, serviceBinding.getXmppStream(rJid));
			
			if (isNewContact && askSubscription.isChecked()) {
				IqRoster.setSubscription(
						contact.getJid(), 
						Presence.PRESENCE_SUBSCRIBE, 
						serviceBinding.getXmppStream(rJid));
			}
		} catch (NullPointerException e) {
			Toast.makeText(this, R.string.networkDown, Toast.LENGTH_LONG).show();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}

	protected void doAddGroupDialog() {
		final EditText editName = new EditText(this);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.newGroupName))
			   .setView(editName)
		       .setCancelable(true)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   String ng = editName.getText().toString();
		        	   if (ng.length()>0)
		        		   addGroup(ng);
		        	       setGroupSelected(ng, true);
		        	   updateGroups();
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
		       });
		
		AlertDialog alert = builder.create();
		alert.setOwnerActivity(this);
		alert.show();
	}

	private void updateGroups() {
		groupLayout.setVisibility(View.GONE);
		
		groupLayout.removeAllViews();
		
		for (CheckBox g :groups.values()) {
			groupLayout.addView(g);
		}
		
		groupLayout.setVisibility(View.VISIBLE);
	}

	private void setGroupSelected(String group, boolean checked) {
		groups.get(group).setChecked(checked);
	}

	private boolean addGroup(String group) {
		//1. check group is exists
		if (groups.containsKey(group)) return false;
		
		//2. add new checkbox
		CheckBox ng = new CheckBox(this);
		ng.setText(group);
		groups.put(group, ng);
		return true;
	}

	@Override
	protected void onResume() {
		serviceBinding.doBindService();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		serviceBinding.doUnbindService();
		super.onPause();
	}


}
