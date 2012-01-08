package org.bombusim.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class NetworkDataStream {
	protected InputStream istream;
	protected OutputStream ostream;
	
	public InputStream getInputStream() {
		return istream;
	}
	
	public OutputStream getOutputStream() {
		return ostream;
	}

	public void closeConnection() throws IOException {
		ostream.close();
		
		istream.close();
	}

	public int read(byte[] cbuf) throws IOException {
		//int avail = istream.available();
		//if (avail == 0) return 0;
		
		/*if (avail>cbuf.length) avail = cbuf.length;
		
		int offs=0;
		
		while (avail>0) {
			int r=istream.read(cbuf, offs, avail);
			if (r==0) throw new IOException("Socket returns no data while non-zero available()");
			offs+=r;
			avail-=r;
		}
		
		return offs; //is equal to actual bytes count*/
		return istream.read(cbuf, 0, cbuf.length);
	}

	public void write(byte[] bytes, int length) throws IOException {
		ostream.write(bytes, 0, length);
		ostream.flush();
	}
	
}
