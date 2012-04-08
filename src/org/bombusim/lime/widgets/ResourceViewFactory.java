package org.bombusim.lime.widgets;

import org.bombusim.lime.R;
import org.bombusim.lime.data.Contact;
import org.bombusim.lime.data.Resource;
import org.bombusim.xmpp.stanza.XmppPresence;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ResourceViewFactory {

    private LayoutInflater mInflater;

    private Bitmap[] mIconStar;

    static class ViewHolder {
        ImageView status;
        
        TextView resource;
        TextView presence;
        
    }

	
    public ResourceViewFactory(Context context, Bitmap[] statusIcons) {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(context);
        
        mIconStar = statusIcons;
    }
    
	public View getView(View convertView, Resource resource) {
		// A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.resourceitem, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.status   = (ImageView) convertView.findViewById(R.id.rit_statusIcon);
            holder.resource = (TextView) convertView.findViewById(R.id.rit_resource);
            holder.presence = (TextView) convertView.findViewById(R.id.rit_presence);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        // Bind the data efficiently with the holder.
        
        //TODO: fix icon ranges
        int presenceIndex = resource.presence;
        if (presenceIndex < 0 || presenceIndex >= mIconStar.length) {
        	Log.w("ASSERT", "index "+presenceIndex+" out of bounds: [0.." + mIconStar.length + "]");
        	presenceIndex = XmppPresence.PRESENCE_UNKNOWN;
        }
        
        holder.status.setImageBitmap(mIconStar[presenceIndex]);
        
        holder.resource.setText(resource.toString());
        holder.presence.setText(resource.statusMessage);

        return convertView;
	}

}
