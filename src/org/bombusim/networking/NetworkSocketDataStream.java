package org.bombusim.networking;

import java.io.IOException;

import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import javax.net.ssl.SSLSocketFactory;
import android.net.SSLCertificateSocketFactory;

//import org.apache.http.conn.ssl.SSLSocketFactory;

import com.jcraft.jzlib.DeflaterOutputStream;
import com.jcraft.jzlib.InflaterInputStream;

import android.util.Log;

public class NetworkSocketDataStream extends NetworkDataStream{

	private boolean zlib = false;
	private boolean ssl = false;
	
	protected Socket socket;
	protected String host;
	protected int port;
	
	
	public NetworkSocketDataStream(String server, int port) throws UnknownHostException, IOException {
		
		this.host=server;
		this.port=port;
		
		Log.i("Socket", "Connecting to "+host+":"+port);
		socket = new Socket(server, port);
		
		istream = socket.getInputStream();
		ostream = socket.getOutputStream();
		
	}
	
	public void setCompression() throws IOException{
		Log.i("Socket", "Binding ZLIB");
		
		istream = new InflaterInputStream(istream);

		DeflaterOutputStream dos;
		dos = new DeflaterOutputStream(ostream);
		dos.setSyncFlush(true);
		ostream = dos; 

		zlib = true;
		
	}
	
	public void setTLS() throws IOException{
		Log.i("Socket", "STARTTLS");
		
		//TODO: check on different devices:
		// !!! ENSURE TLS enabled in account settings before test
		// 1. emulator/2.2 - SSLPeerUnverifiedException (jabber.ru, google.com) - bug in 2.2?
		// 2. cyanogen/2.3 - works (all hosts)
		// 3. emulator/ics - works
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
		SSLSession sess=ssls.getSession();
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
}

