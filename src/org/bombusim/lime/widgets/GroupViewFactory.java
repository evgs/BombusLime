package org.bombusim.lime.widgets;

import org.bombusim.lime.R;
import org.bombusim.lime.data.RosterGroup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class GroupViewFactory {
    private LayoutInflater mInflater;

    private Bitmap mIconExpanded;
    private Bitmap mIconCollapsed;

    static class ViewHolder {
        ImageView expander;
        TextView group;
        TextView count;
    }

	
    public GroupViewFactory(Context context) {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(context);
    	
        // Icons bound to the rows.
        mIconExpanded = BitmapFactory.decodeResource(context.getResources(), R.drawable.exp_maximized);
        mIconCollapsed = BitmapFactory.decodeResource(context.getResources(), R.drawable.exp_minimized);
        
    }

	public View getView(View convertView, RosterGroup g) {
		// A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.rostergroup, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.group = (TextView) convertView.findViewById(R.id.grouplabel);
            holder.count = (TextView) convertView.findViewById(R.id.count);
            holder.expander = (ImageView) convertView.findViewById(R.id.expander);
            

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        // Bind the data efficiently with the holder.
        
        holder.group.setText(g.groupName);
        holder.expander.setImageBitmap((g.collapsed)? mIconCollapsed : mIconExpanded);
        //TODO online/size
        holder.count.setText("("+g.onlineCount+"/"+g.contacts.size()+")");

        return convertView;
	}

}
