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

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.data.Contact;
import org.bombusim.lime.data.Vcard;
import org.bombusim.lime.service.XmppService;
import org.bombusim.lime.service.XmppServiceBinding;
import org.bombusim.lime.widgets.ContactBar;
import org.bombusim.lime.widgets.OkCancelBar;
import org.bombusim.xmpp.XmppStream;
import org.bombusim.xmpp.handlers.IqVcard;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VCardActivity extends Activity {
	public static final String JID = "jid";
	public static final String MY_JID = "fromJid";
	
	private String mJid;
	private String mRJid;

    private Vcard mVcard;
	
	private XmppServiceBinding mSb;
	
	private ProgressDialog mPgsDialog;
    private ContactBar mContactBar;
    private Contact mContact;
    private OkCancelBar mOkCancelBar;
    
    private LinearLayout mVcardLayout;
    private LayoutInflater mInflater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
        Bundle params = getIntent().getExtras();
        if (params == null) throw new InvalidParameterException("No parameters specified for VCardActivity");
        mJid = params.getString(JID);
        mRJid = params.getString(MY_JID);

        setContentView(R.layout.vcard);
        
        mContact = Lime.getInstance().getRoster().findContact(mJid, mRJid);
        
        mSb = new XmppServiceBinding(this);

        mInflater = LayoutInflater.from(this);
        mVcardLayout = (LinearLayout) findViewById(R.id.vcardItems);
        mContactBar = (ContactBar) findViewById(R.id.contact_head);
        mOkCancelBar = (OkCancelBar) findViewById(R.id.okCancel);
        
        mOkCancelBar.setOnButtonActionListener(new OkCancelBar.OnButtonActionListener() {
            
            @Override
            public void onPositive() {
                // TODO publish own vcard
                finish();
            }
            
            @Override
            public void onNegatiove() { finish(); }
        });
	}
	
    @Override
	protected void onResume() {
        mContactBar.bindContact(mContact, false);
        
        //TODO: show for our vcard
        mOkCancelBar.setVisibility(View.GONE);
        
        mSb.setBindListener(new XmppServiceBinding.BindListener() {
            @Override
            public void onBindService(XmppService service) { queryVCard(); }
        });
        
		mSb.doBindService();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		mSb.doUnbindService();
		super.onPause();
	}

	private Handler mHandler = new Handler();
	
	private void showVcardContent() {
	    
        mVcardLayout.setVisibility(View.GONE);
	    if (mVcard == null) return;
	    
        mVcardLayout.removeAllViewsInLayout();
        
	    Resources res = getResources();
        String[] fields = res.getStringArray(R.array.vcardFields);
        String[] labels = res.getStringArray(R.array.vcardLabels);
        
        if (fields.length != labels.length) 
            throw new RuntimeException("Inconsistent vcardFields and vcardLabels item count");
        
        LinearLayout va = new LinearLayout(this);
        ImageView bigAvatar = new ImageView(this);
        
        //TODO: remove hardcoded size
        Bitmap b = mVcard.getBigAvatar(256);
        if (b!=null) {
            bigAvatar.setImageBitmap(b);
        } else {
            //TODO: big picture
            bigAvatar.setImageResource(R.drawable.ic_contact_picture);
        }
        
        mVcardLayout.addView(bigAvatar);
        
        for (int index = 0; index<fields.length; index++) {
            String value = mVcard.getField(fields[index]);
            
            if (value==null) continue;
            
            View vcardField = mInflater.inflate(R.layout.vcard_field_view, null);

            ((TextView) vcardField.findViewById(R.id.vcardField)).setText(labels[index]);
            ((TextView) vcardField.findViewById(R.id.vcardValue)).setText(value);
           
            mVcardLayout.addView(vcardField);
        }
        
        mVcardLayout.setVisibility(View.VISIBLE);
	}
	
	private void queryVCard() {
        mPgsDialog = ProgressDialog.show(this, "", 
                getString(R.string.loadingVcardProgress), true, true, new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        //Cancelling VCard fetching 
                        onBackPressed();
                    }
                });

        IqVcard vq = new IqVcard();
        vq.setVcardListener(new IqVcard.VCardListener() {
            
            @Override
            public void onVcardArrived(String from, Vcard result) {
                mVcard = result;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mPgsDialog.dismiss();
                        showVcardContent();
                    }
                });
            }
        });
        
        XmppStream s = mSb.getXmppStream(mRJid);
        
        vq.vcardRequest(mJid, s);
    }

}
