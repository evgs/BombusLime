package org.bombusim.lime.widgets;

import org.bombusim.lime.R;
import org.bombusim.lime.data.Contact;
import org.bombusim.xmpp.stanza.XmppPresence;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactViewFactory {

    private LayoutInflater mInflater;

    private Bitmap mIconRobot;
    private Bitmap mIconActiveChat;
    private Bitmap mIconInactiveChat;
    private Bitmap mIconComposing;
    private Bitmap[] mIconStar;

    static class ViewHolder {
        ImageView photo;
        ImageView status;
        TextView jid;
        TextView presence;
        
        ImageView chatIcon;
    }

	
    public ContactViewFactory(Context context, Bitmap[] statusIcons) {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(context);
    	
        // Icons bound to the rows.
        mIconRobot = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
        mIconActiveChat = BitmapFactory.decodeResource(context.getResources(), R.drawable.chat);
        mIconInactiveChat = BitmapFactory.decodeResource(context.getResources(), R.drawable.chat_inactive);
        mIconComposing = BitmapFactory.decodeResource(context.getResources(), R.drawable.chat_inactive);
        
        mIconStar = statusIcons;
    }
    
	public View getView(View convertView, Contact c) {
		// A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.rosteritem2, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.photo = (ImageView) convertView.findViewById(R.id.rit_photo);
            holder.status = (ImageView) convertView.findViewById(R.id.rit_statusIcon);
            holder.jid = (TextView) convertView.findViewById(R.id.rit_jid);
            holder.presence = (TextView) convertView.findViewById(R.id.rit_presence);
            holder.chatIcon = (ImageView) convertView.findViewById(R.id.rit_chatIcon);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        // Bind the data efficiently with the holder.
        
        Bitmap avatar = c.getLazyAvatar(false);
        if (avatar == null) avatar = mIconRobot;
        holder.photo.setImageBitmap(avatar);
        
        //TODO: fix icon ranges
        int presenceIndex = c.getPresence();
        if (presenceIndex < 0 || presenceIndex >= mIconStar.length) {
        	Log.w("ASSERT", "index "+presenceIndex+" out of bounds: [0.." + mIconStar.length + "]");
        	presenceIndex = XmppPresence.PRESENCE_UNKNOWN;
        }
        
        holder.status.setImageBitmap(mIconStar[presenceIndex]);
        holder.jid.setText(c.getScreenName());
        holder.presence.setText(c.getStatusMessage());
        
        if ( c.getUnread() > 0) {
        	holder.chatIcon.setImageBitmap(mIconActiveChat);
        	holder.chatIcon.setVisibility(View.VISIBLE);
        } else if (c.hasActiveChats()) {
        	holder.chatIcon.setImageBitmap(mIconInactiveChat);
        	holder.chatIcon.setVisibility(View.VISIBLE);
        } else { 
        	holder.chatIcon.setVisibility(View.GONE);
        }
        

        return convertView;
	}

}
