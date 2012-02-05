package org.bombusim.lime.activity;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class About {
	private About(){};
	
	public static void showAboutDialog(Activity hostActivity) {
		Context context = hostActivity.getBaseContext();
		
		StringBuilder sb=new StringBuilder();

		//TODO: simplify reading
		try {
			InputStreamReader isr = new InputStreamReader( context.getResources().openRawResource(R.raw.about) );
			while (true) {
				int c = isr.read();
				if (c==-1) break;
				sb.append((char)c);
			}
			isr.close();
		} catch (Exception e) {e.printStackTrace(); }
		
		String about = String.format(sb.toString(), 
				context.getText(R.string.app_name), 
				Lime.getInstance().getVersion());
		CharSequence aboutDecorated = Html.fromHtml(about);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(hostActivity);
		builder.setTitle(context.getText(R.string.aboutBombusLime))
			   .setIcon(R.drawable.ic_launcher)
			   .setMessage(aboutDecorated)
		       .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) { dialog.dismiss(); }
		       });
		AlertDialog alert = builder.create();
		alert.setOwnerActivity(hostActivity);
		alert.show();
		
		// Make the textview clickable. Must be called after show()   
	    ((TextView)alert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

	}
}
