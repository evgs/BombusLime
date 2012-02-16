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

package org.bombusim.networking;

import java.io.IOException;

import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Principal;
import java.text.DateFormat;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;

import javax.net.ssl.SSLSocketFactory;
import javax.security.cert.X509Certificate;

import org.bombusim.lime.logger.LimeLog;

import android.net.SSLCertificateSocketFactory;
import android.util.Log;

//import org.apache.http.conn.ssl.SSLSocketFactory;

import com.jcraft.jzlib.DeflaterOutputStream;
import com.jcraft.jzlib.InflaterInputStream;

public class NetworkSocketDataStream extends NetworkDataStream{

	private boolean zlib = false;
	private String certificateInfo;
	
	protected Socket socket;
	protected String host;
	protected int port;
	
	
	public NetworkSocketDataStream(String server, int port) throws UnknownHostException, IOException {
		
		this.host=server;
		this.port=port;
		
		LimeLog.i("Socket", "Connecting to "+host+":"+port, null);
		socket = new Socket(server, port);
		
		//keep-alive packets every 2 hours (by default)
		socket.setKeepAlive(true);
		
		istream = socket.getInputStream();
		ostream = socket.getOutputStream();
		
	}
	
	public void setCompression() throws IOException{
		LimeLog.i("Socket", "Binding ZLIB", null);
		
		istream = new InflaterInputStream(istream);

		DeflaterOutputStream dos;
		dos = new DeflaterOutputStream(ostream);
		dos.setSyncFlush(true);
		ostream = dos; 

		zlib = true;
		
	}
	
	public void setTLS() throws IOException{
		LimeLog.i("Socket", "Switching to secure socket layer", null);
		
		//TODO: check on different devices:
		// !!! ENSURE TLS enabled in account settings before test
		// 1. emulator/2.2 - SSLPeerUnverifiedException (jabber.ru, google.com) - bug in emulator v2.2
		// 2. cyanogen/2.3 - works (all hosts)
		// 3. emulator/ics - works
		// 4. Gratia/2.2 - works
		SSLSocketFactory sf = 
						//SSLCertificateSocketFactory.getDefault(20000, null);
						SSLCertificateSocketFactory.getInsecure(20000, null);
			 
		//TODO: check on different devices:
		// 1. emulator/2.2 - works
		// 2. cyanogen/2.3 - works
			//KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType()); 
			//trustStore.load(null, null); 
			//SSLSocketFactory sf = new AndroidSSLSocketFactory(trustStore); 
			//sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); 

			
		final SSLSocket ssls=(SSLSocket) sf.createSocket(socket, host, port, true);
		
		ssls.addHandshakeCompletedListener(new HandshakeCompletedListener() {
			@Override
			public void handshakeCompleted(HandshakeCompletedEvent event) {
				X509Certificate[] certs;
				try {
					certs = ssls.getSession().getPeerCertificateChain();
				} catch (SSLPeerUnverifiedException e) { return; }
				
				StringBuilder so = new StringBuilder();
				
				for (X509Certificate cert : certs) {
					so.append("X509 Certificate:\n").append(" Subject:");
					appendPrincipal(so, cert.getSubjectDN());
					so.append("\n Issued by:");
					appendPrincipal(so, cert.getIssuerDN());
					so.append("\n Valid from:    ").append( DateFormat.getInstance().format(cert.getNotBefore()) );
					so.append("\n Expired after: ").append( DateFormat.getInstance().format(cert.getNotAfter()) );
					so.append("\n\n");
				}

				certificateInfo = so.toString();
				LimeLog.i("Socket", "Certificate chain verified", certificateInfo);
			}

			private void appendPrincipal(StringBuilder so, Principal p) {
				String name = p.getName();
				if (name==null) { so.append("<null>\n"); return; }
				
				String elements[] = name.split(",");
				for (String e: elements) {
					so.append("\n   ").append(e);
				}
				
				so.append("\n");
			}
		});
		
		ssls.startHandshake();
	    socket = ssls;
		    
		    
		istream = socket.getInputStream();
		ostream = socket.getOutputStream();

	}
	
	@Override
	public void closeConnection() throws IOException {
		try {
			super.closeConnection();
		} finally {
			socket.close();
		}
	}

	public boolean isSecure() { return certificateInfo != null; }
	public boolean isCompressed() { return zlib; }

	public String getCertificateInfo() {
		// TODO Auto-generated method stub
		return certificateInfo;
	}
}

