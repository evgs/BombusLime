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

    public abstract boolean isClosed();
	
}
