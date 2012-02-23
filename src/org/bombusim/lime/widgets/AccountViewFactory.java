package org.bombusim.lime.widgets;

import org.bombusim.lime.R;
import org.bombusim.xmpp.XmppAccount;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AccountViewFactory {
    private LayoutInflater mInflater;

    private Bitmap mIconExpanded;
    private Bitmap mIconCollapsed;
    
    private Bitmap[] mIconStar;

    static class ViewHolder {
        ImageView expander;
        ImageView status;
        ImageView ssl;
        TextView account;
    }

	
    public AccountViewFactory(Context context, Bitmap[] statusIcons) {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(context);
    	
        // Icons bound to the rows.
        mIconExpanded = BitmapFactory.decodeResource(context.getResources(), R.drawable.exp_maximized);
        mIconCollapsed = BitmapFactory.decodeResource(context.getResources(), R.drawable.exp_minimized);
        
        mIconStar = statusIcons;
        
    }

	public View getView(View convertView, XmppAccount a) {
		// A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.rosteraccount, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.expander = (ImageView) convertView.findViewById(R.id.expander);
            holder.status = (ImageView) convertView.findViewById(R.id.statusIcon);
            holder.ssl =    (ImageView) convertView.findViewById(R.id.imageSSL);
            holder.account = (TextView) convertView.findViewById(R.id.accountName);
            

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        // Bind the data efficiently with the holder.
        
        holder.account.setText(a.userJid);
        holder.expander.setImageBitmap( (a.collapsed)? mIconCollapsed : mIconExpanded);
        holder.status.setImageBitmap(mIconStar[a.getRuntimeStatus()]);
        holder.ssl.setVisibility( a.connectionIsSecure()? View.VISIBLE : View.GONE );

        return convertView;
	}

}
