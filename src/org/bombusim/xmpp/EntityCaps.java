package org.bombusim.xmpp;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;

import org.bombusim.lime.Lime;
import org.bombusim.lime.R;
import org.bombusim.util.strconv;
import org.bombusim.xmpp.exception.XmppException;
import org.bombusim.xmpp.stanza.Iq;

import android.util.Log;

public class EntityCaps  extends XmppObjectListener {
	private static final String DISCO_INFO = "http://jabber.org/protocol/disco#info";
	private static final String XMLNS_CAPS = "http://jabber.org/protocol/caps";
	private static final String BOMBUS_NS = "http://bombus-im.org/android";
	private static final String BOMBUS_CATEGORY = "client";
	private static final String BOMBUS_TYPE = "mobile";

	private ArrayList<String> features;
	
	private String ver;
	
	public XmppObject getresenceCaps() {
		String ver = calculateVer();

		if (ver==null) return null;

		XmppObject c = new XmppObject("c", null, null)
			.setNameSpace(XMLNS_CAPS)
			.setAttribute("hash", "sha-1")
			.setAttribute("ver", ver)
			.setAttribute("node", BOMBUS_NS);
		
		return c;
	}

	private String calculateVer() {
		if (ver!=null) return ver;
		
		Collections.sort(features);
		
		StringBuilder sb = new StringBuilder(BOMBUS_CATEGORY)
			.append('/')
			.append(BOMBUS_TYPE)
			.append("//")
			.append(getVersionName())
			.append('<');
		
		for (String feature : features) {
			sb.append(feature).append('<');
		}
		
		
		MessageDigest sha1;
		
		try {
			sha1 = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) { e.printStackTrace(); return null; }	

		String caps = sb.toString();
		
		Log.d("CAPS", caps);
		
		sha1.update(caps.getBytes());
		
		ver = strconv.byteArrayToHexString(sha1.digest());
		
		return ver;
	}

	private String getVersionName() {
		return Lime.getInstance().getString(R.id.appName)
				+' '
				+Lime.getInstance().getVersion();
	}
	
	public void updateFeatures(ArrayList<String> features) {
		this.features = features;  
		ver = null;
	}

	@Override
	public int blockArrived(XmppObject data, XmppStream stream)
			throws IOException, XmppException {
		try {
			Iq disco = (Iq) data;
			if (!disco.getTypeAttribute().equals("get"))
				return BLOCK_REJECTED;
			
			XmppObject query = disco.findNamespace("query", DISCO_INFO);
			String node = query.getAttribute("node");
			
			//no more exceptions should be
			Iq result = new Iq(disco.getAttribute("from"), Iq.TYPE_RESULT, disco.getAttribute("id"));
			XmppObject qr = result.addChildNs("query", DISCO_INFO);
			
			if (node!=null) {
				if ( !node.equals(BOMBUS_NS+'#'+calculateVer()) )  return BLOCK_REJECTED;
				//TODO: should return some another error, not FEATURE_NOT_IMPLEMENTED
			}
			
			qr.setAttribute("node", node);
			
			qr.addChild("identity", null)
			  .setAttribute("category", BOMBUS_CATEGORY)
			  .setAttribute("type", BOMBUS_TYPE)
			  .setAttribute("name", getVersionName());
			
			for (String feature : features) {
				qr.addChild("feature", null).setAttribute("val", feature);
			}
			
			stream.send(result);
			
			return BLOCK_PROCESSED;
			
		} catch (Exception e) {}
		return BLOCK_REJECTED;
	}
	
	@Override
	public String capsXmlns() { return XMLNS_CAPS; }
	
	@Override
	public int priority() { return PRIORITY_DISCO; }
}
