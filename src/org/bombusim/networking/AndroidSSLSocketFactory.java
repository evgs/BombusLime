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
import java.security.KeyManagementException; 
import java.security.KeyStore; 
import java.security.KeyStoreException; 
import java.security.NoSuchAlgorithmException; 
import java.security.UnrecoverableKeyException; 
import java.security.cert.CertificateException; 
import java.security.cert.X509Certificate; 
import javax.net.ssl.SSLContext; 
import javax.net.ssl.TrustManager; 
import javax.net.ssl.X509TrustManager; 
import org.apache.http.conn.ssl.SSLSocketFactory; 
public class AndroidSSLSocketFactory extends SSLSocketFactory { 
	SSLContext sslContext = SSLContext.getInstance("TLS"); 
	public AndroidSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
		super(truststore);
		TrustManager tm = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
			public X509Certificate[] getAcceptedIssuers() { return null; }
		}; 
		sslContext.init(null, new TrustManager[] { tm }, null);
	}
	
	@Override
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
		return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	}
	
	@Override 
	public Socket createSocket() throws IOException { 
		return sslContext.getSocketFactory().createSocket(); 
	} 
}   