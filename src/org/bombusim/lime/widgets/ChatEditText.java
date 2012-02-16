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

package org.bombusim.lime.widgets;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

public class ChatEditText extends EditText {

	private static final int SMILES_MENU_ID = R.string.addSmileMenuItem;

	public ChatEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public ChatEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreateContextMenu(ContextMenu menu) {
		super.onCreateContextMenu(menu);
		menu.add(R.string.addSmileMenuItem).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				showAddSmileDialog();
				return true;
			}
		});
		
		menu.add(R.string.addMeMenuItem).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				addMe();
				return true;
			}
		});
	}

	private Activity dialogHostActivity;
	
	public void setDialogHostActivity(Activity activity) {
		this.dialogHostActivity = activity;
	}
	
	public void showAddSmileDialog() {
		
		View gridLayout = View.inflate(getContext(), R.layout.grid_smiles, null);
	
		final GridView smiles = (GridView) gridLayout.findViewById(R.id.smilesGrid);
		
		
		smiles.setAdapter(new BaseAdapter() {
	        public View getView(int position, View convertView, ViewGroup parent) {
	            ImageView i;

	            if (convertView == null) {
	                i = new ImageView(smiles.getContext());
	                i.setScaleType(ImageView.ScaleType.CENTER);
	                
	                //TODO: button size
	                i.setLayoutParams(new GridView.LayoutParams(50, 50));
	            } else {
	                i = (ImageView) convertView;
	            }

	            i.setImageDrawable( (Drawable) getItem(position) );

	            return i;
	        }


	        public final int getCount() {
	            return Lime.getInstance().getSmilify().getCount();
	        }

	        public final Object getItem(int position) {
	            return Lime.getInstance().getSmilify().getSmileDrawable(position);
	        }

	        public final long getItemId(int position) { return position; }
			
		});
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.addSmileMenuItem);
		builder.setView(gridLayout);
		
		builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
		});
			   
		final AlertDialog alert = builder.create();
		
		smiles.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				
				String smile = Lime.getInstance().getSmilify().getSmileText(position)+ ' ';
				
				pasteText(smile);
				alert.cancel();
			}

		});

		alert.setOwnerActivity(dialogHostActivity);
		alert.show();
	}

	public void addMe() { pasteText("/me "); }
		
	private void pasteText(String paste) {
		int st = getSelectionStart();
		int en = getSelectionEnd();
		
		getText().replace(st, en, paste);
	}
}
