package org.bombusim.networking;

import java.io.IOException;

import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocket;

import javax.net.ssl.SSLSocketFactory;

import org.bombusim.lime.logger.LimeLog;

import android.net.SSLCertificateSocketFactory;

//import org.apache.http.conn.ssl.SSLSocketFactory;

import com.jcraft.jzlib.DeflaterOutputStream;
import com.jcraft.jzlib.InflaterInputStream;

public class NetworkSocketDataStream extends NetworkDataStream{

	private boolean zlib = false;
	private boolean ssl = false;
	
	protected Socket socket;
	protected String host;
	protected int port;
	
	
	public NetworkSocketDataStream(String server, int port) throws UnknownHostException, IOException {
		
		this.host=server;
		this.port=port;
		
		LimeLog.i("Socket", "Connecting to "+host+":"+port, null);
		socket = new Socket(server, port);
		
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
		LimeLog.i("Socket", "STARTTLS", null);
		
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

			
		SSLSocket ssls=(SSLSocket) sf.createSocket(socket, host, port, true);
		ssls.startHandshake();
	    socket = ssls;
		    
		    
		istream = socket.getInputStream();
		ostream = socket.getOutputStream();

		ssl=true;
	}
	
	@Override
	public void closeConnection() throws IOException {
		try {
			super.closeConnection();
		} finally {
			socket.close();
		}
	}

	public boolean isSecure() { return ssl; }
	public boolean isCompressed() { return zlib; }
}

