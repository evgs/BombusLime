package org.bombusim.lime.activity;

import java.util.ArrayList;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.lime.data.Contact;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RosterAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Bitmap mIconRobot;
    private Bitmap[] mIconStar;

    public RosterAdapter(Context context) {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(context);

        // Icons bound to the rows.
        mIconRobot = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
        mIconStar = new Bitmap[] { 
        		BitmapFactory.decodeResource(context.getResources(), R.drawable.status_offline),
        		BitmapFactory.decodeResource(context.getResources(), R.drawable.status_online),
        		BitmapFactory.decodeResource(context.getResources(), R.drawable.status_chat),
        		BitmapFactory.decodeResource(context.getResources(), R.drawable.status_away),
        		BitmapFactory.decodeResource(context.getResources(), R.drawable.status_xa),
        		BitmapFactory.decodeResource(context.getResources(), R.drawable.status_dnd),
        		BitmapFactory.decodeResource(context.getResources(), R.drawable.status_ask),
        		BitmapFactory.decodeResource(context.getResources(), R.drawable.status_unknown),
        		BitmapFactory.decodeResource(context.getResources(), R.drawable.status_invisible)
        		};
    }
	
	private ArrayList<Contact> getContacts() {
		return Lime.getInstance().getRoster().getContacts();
	}
    
	@Override
	public int getCount() {
		int c=getContacts().size();
		return c;
	}

	@Override
	public Object getItem(int position) {
		return getContacts().get(position);
	}

	@Override
	public long getItemId(int position) {
		return getContacts().get(position).hashCode();
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
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
        
        Contact c=getContacts().get(position);
        Bitmap avatar = c.getLazyAvatar(false);
        if (avatar == null) avatar = mIconRobot;
        holder.photo.setImageBitmap(avatar);
        holder.status.setImageBitmap(mIconStar[c.getPresence()]);
        holder.jid.setText(c.getScreenName());
        holder.presence.setText(c.getStatusMessage());
        holder.chatIcon.setVisibility(c.hasActiveChats() ? View.VISIBLE : View.GONE);

        return convertView;
    }

    static class ViewHolder {
        ImageView photo;
        ImageView status;
        TextView jid;
        TextView presence;
        
        ImageView chatIcon;
    }
	
}
