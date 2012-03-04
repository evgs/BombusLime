package org.bombusim.lime.widgets;

import org.bombusim.lime.R;
import org.bombusim.lime.data.Contact;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ContactBar extends RelativeLayout{

    public ContactBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.contact_bar, this);    
    }
    
    public void bindContact(Contact contact, boolean composing) {
        
        ImageView vAvatar = (ImageView) findViewById(R.id.rit_photo);        
        
        Bitmap avatar = contact.getLazyAvatar(true);
        if (avatar != null) {
            vAvatar.setImageBitmap(avatar);
        } else {
            vAvatar.setImageResource(R.drawable.ic_contact_picture);
        }
        
        ((TextView) findViewById(R.id.rit_jid))
                    .setText(contact.getScreenName());
        
        ((TextView) findViewById(R.id.rit_presence))
                    .setText(contact.getStatusMessage());
        
        TypedArray si = getResources().obtainTypedArray(R.array.statusIcons);
        Drawable std = si.getDrawable(contact.getPresence());
        
        ((ImageView) findViewById(R.id.rit_statusIcon))
                    .setImageDrawable(std);
        
        ((ImageView) findViewById(R.id.composing))
                    .setVisibility( (composing) ? View.VISIBLE : View.GONE );

    }
    
}
