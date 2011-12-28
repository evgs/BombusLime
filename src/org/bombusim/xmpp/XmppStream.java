/*
 * XmppStream.java
 * Created on 23.12.2011
 *
 * Copyright (c) 2005-2011, Eugene Stahov (evgs@bombus-im.org), 
 * http://bombus-im.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * You can also redistribute and/or modify this program under the
 * terms of the Psi License, specified in the accompanied COPYING
 * file, as published by the Psi Project; either dated January 1st,
 * 2005, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */


package org.bombusim.xmpp;

import java.io.IOException;
import java.util.ArrayList;

import org.bombusim.networking.NetworkDataStream;
import org.bombusim.networking.NetworkSocketDataStream;
import org.bombusim.xml.Attributes;
import org.bombusim.xml.XMLException;
import org.bombusim.xml.XMLParser;
import org.bombusim.xmpp.handlers.IqRoster;
import org.bombusim.xmpp.handlers.IqVersionReply;
import org.bombusim.xmpp.stanza.Iq;
import org.bombusim.xmpp.stanza.Presence;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * The stream to a jabber server.
 */

public class XmppStream extends XmppParser implements Runnable {
    
    String sessionId;
    
    final XmppAccount account;
    
    public String jid; //binded JID, should be used instead of account data 
    
    private String server;
    private String host;  //evaluated from SRV record or specified manually in account
    private int port;	  //evaluated from SRV record or specified manually in account

    //TODO: evaluate from system settings or simply from string resources :)
    private String lang;
    
    public boolean pingSent;
    
    //TODO: state machine:{offline, connecting, logged in} 
    public boolean loggedIn;
    
    private NetworkDataStream dataStream;

	private boolean xmppV1;
    
    //TODO: synchronized
	private ArrayList<XmppObjectListener> dispatcherQueue;

	private ArrayList<XmppObject> incomingQueue;

	
    /**
     * Constructor. Connects to the server and sends the jabber welcome message.
     *
     */
    
	
    public XmppStream( XmppAccount account) {
    	
    	this.account = account;

		server = new XmppJid(account.userJid).getServer();
		
		jid = account.userJid + '/' + account.resource;
    	
    	dispatcherQueue = new ArrayList<XmppObjectListener>();
    }

    public void connect() {
    	Thread thread=new Thread( this );
    	thread.setName("XmppStream->"+server);
    	thread.start();
    }
    

    
    public void initiateStream() throws IOException {
        
        //sendQueue=new Vector();
        
        StringBuffer header=new StringBuffer("<stream:stream to='" )
            .append( server )
            .append( "' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' version='1.0'");
        
        if (lang!=null) {
            header.append(" xml:lang='").append(lang).append("'");
        }
        header.append( '>' );
        send(header.toString());
    }

    @Override
    public boolean tagStart(String name, Attributes attributes) {
        if (name.equals( "stream:stream" ) ) {
            sessionId = attributes.getValue("id");
            String version= attributes.getValue("version");
            xmppV1 = ("1.0".equals(version));
            
            //dispatcher.broadcastBeginConversation();
            return false;
        }
        
        return super.tagStart(name, attributes);
    }

    
    public void tagEnd(String name) throws XMLException {
        if (currentBlock == null) {
            if (name.equals( "stream:stream" ) ) {
                //dataStream.closeConnection();
                throw new XMLException("Normal stream shutdown");
            }
            return;
        }
        
        if (currentBlock.getParent() == null) {

            if (currentBlock.getTagName().equals("stream:error")) {
                XmppError xe = XmppError.decodeStreamError(currentBlock);

                //dataStream.closeConnection();
                throw new XMLException(xe.toString());
                
            }
        }
        
        super.tagEnd(name);
    }

    protected void dispatchXmppStanza(XmppObject currentBlock) {
    	incomingQueue.add(currentBlock);
    }

    private void dispatchIncomingQueue() throws IOException, XmppException {
    	for (int index = 0; index < incomingQueue.size(); index++) {
    		
    		XmppObject currentBlock=incomingQueue.get(index);
    		
    		for (int i=0; i<dispatcherQueue.size(); i++) {
        		int result=dispatcherQueue.get(i).blockArrived(currentBlock, this);
        		switch (result) {
    			case XmppObjectListener.BLOCK_PROCESSED: 
    				break;
    			case XmppObjectListener.NO_MORE_BLOCKS:
    				dispatcherQueue.remove(i);
    				break;
    			}
        	}
    		
    	}
    	
    	incomingQueue.clear();
    }
    
    /*public void startKeepAliveTask(){
        Account account=StaticData.getInstance().account;
        if (account.keepAliveType==0) return;
        keepAlive=new TimerTaskKeepAlive(account.keepAlivePeriod, account.keepAliveType);
    }*/
    
    /**
     * The threads run method. Handles the parsing of incomming data in its
     * own thread.
     */
    public void run() {
        loggedIn = false;

		addBlockListener(new SASLAuth());
		
		incomingQueue = new ArrayList<XmppObject>();
        
        try {
        	
        	if (host == null) {
        		
        		if (account.specificHostPort) {
        			host = account.xmppHost;
        			port = account.xmppPort;
        		} else {
        		
            		// workaround for org.xbill.DNS
            		// see http://stackoverflow.com/questions/2879455/android-2-2-and-bad-address-family-on-socket-connect
            		// TODO: try other API levels
        			
        			java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
        			java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
	        	
        			Lookup l = new Lookup("_xmpp-client._tcp." + server, Type.SRV);
	        	
        			//TODO: caching SRV requests
        			//l.setCache(null);
        			//l.setResolver(new SimpleResolver("8.8.8.8"));
	        	
        			Record [] records = l.run();
	        	
        			if (records == null) {
        				Log.i("SRV", server + " has no SRV record");
        				host = server;
        				port = 5222;
        			} else {
        				host = ((SRVRecord)records[0]).getTarget().toString();
        				port = ((SRVRecord)records[0]).getPort();
        			}
	        	
        		}
        		
        		if (host==null) {
    				Log.i("SRV", "Assuming host = "+ server);
   			
        			host = server;
        		}
        		
        		if (port == 0) port = 5222;
        		if (port == 5222  && account.secureConnection == XmppAccount.SECURE_CONNECTION_LEGACY_SSL) {
        			port = 5223;
        		}
        	}
        	
        	dataStream = new NetworkSocketDataStream(host, port);
        	
        	if (account.secureConnection == XmppAccount.SECURE_CONNECTION_LEGACY_SSL) {
        		((NetworkSocketDataStream)dataStream).setTLS();
        	}
        	
            XMLParser parser = new XMLParser( this );
            
            initiateStream();
            
            byte cbuf[]=new byte[4096];
            
            while (true) {

            	//blocking operation
            	int length=dataStream.read(cbuf);
                
                if (length==0) {
                    try { Thread.sleep(100); } catch (Exception e) {};
                    continue;
                }

                StringBuffer dbg=new StringBuffer(4096);
                for (int i=0; i<length; i++) {
                	dbg.append((char)cbuf[i]);
                }
            	Log.d("lime<< ", dbg.toString());
                
                parser.parse(cbuf, length);
                
                dispatchIncomingQueue();
            }
            
            //dispatcher.broadcastTerminatedConnection( null );
        } catch( Exception e ) {
        	//TODO: handle exceptions
        	
            System.out.println("Exception in parser:");
            e.printStackTrace();
            broadcastTerminatedConnection(e);
        };
        
        loggedIn = false;
    }
    
    /**
     * Method to close the connection to the server and tell the listener
     * that the connection has been terminated.
     */
    
    public void close() {
        //if (keepAlive!=null) keepAlive.destroyTask();
        
        dispatcherQueue.clear();
        
        try {
            try {  Thread.sleep(500); } catch (Exception e) {};
            send( "</stream:stream>" );
        } catch( IOException e ) {
            // Ignore an IO Exceptions because they mean that the stream is
            // unavailable, which is irrelevant.
        } 
        
        try {
            dataStream.closeConnection();
        } catch (IOException e) {
			// TODO: handle exception
        	e.printStackTrace();
		}
        broadcastTerminatedConnection(new Exception("Connection closed"));
    }
    
    private void broadcastTerminatedConnection(Exception exception) {
		// TODO Auto-generated method stub
    	System.out.println("Broadcasted exception <Close>");
		exception.printStackTrace();
	}

	/**
     * Method of sending data to the server.
     *
     * @param data The data to send.
     */
    
 /* public void send( byte[] data ) throws IOException
  {
    outStream.write( data );
    outStream.flush();
  }
  */
    
    /**
     * Method of sending data to the server.
     *
     * @param The data to send to the server.
     */
    public void sendKeepAlive(int type) throws IOException {
        switch (type){
            case 3:
                if (pingSent) {
                    broadcastTerminatedConnection(new Exception("Ping Timeout"));
                } else {
                    //System.out.println("Ping myself");
                    ping();
                }
                break;
            case 2:
                send("<iq/>");
                break;
            case 1:
                send(" ");
        }
    }
    
    private void ping() {
        XmppObject ping=new Iq(server, Iq.TYPE_GET, "ping");
        //ping.addChildNs("query", "jabber:iq:version");
        ping.addChildNs("ping", "urn:xmpp:ping");
        pingSent=true;
        send(ping);
    }

    
    public void send( String data ) throws IOException {
    	Log.d("lime>> ", data);
    	dataStream.write(data.getBytes());
    }
    
    public void sendBuf( StringBuffer data ) throws IOException {
    	dataStream.write(data);
        //System.out.println(data);
    }
    
    /**
     * Method of sending a Jabber datablock to the server.
     *
     * @param block The data block to send to the server.
     */
    
    public void send( XmppObject block )  {
    	Log.d("lime>> ", block.toString());
    	
    	
    	StringBuffer data=new StringBuffer(4096);
    	block.constructXML(data);
    	try {
    		sendBuf(data);
    	} catch (Exception e) {
    		//TODO: verify action on error
    		e.printStackTrace();
    		close();
		}
    }
    
    /**
     * Set the listener to this stream.
     */
    
    public void addBlockListener(XmppObjectListener listener) { 
        dispatcherQueue.add(listener);
    }
    public void cancelBlockListener(XmppObjectListener listener) {
    	dispatcherQueue.remove(listener);
    }
    
    public void cancelBlockListenerByClass(Class removeClass) {
    	int i=0;
    	while (i<dispatcherQueue.size()) {
    		if (dispatcherQueue.get(i).getClass().equals(removeClass)) {
    			dispatcherQueue.remove(i);
    			continue;
    		}
    		i++;
    	}
    }
    
    public boolean isXmppV1() { return xmppV1; }

    public String getSessionId() { return sessionId; }


    public void setZlibCompression() throws IOException {
        ((NetworkSocketDataStream)dataStream).setCompression();
    }
    public void setTLS() throws IOException {
        ((NetworkSocketDataStream)dataStream).setTLS();
    }
    
    public boolean isSecured() {
    	return ((NetworkSocketDataStream)dataStream).isSecure();
    }

    void loginSuccess() {
    	loggedIn=true;
    	
    	IqRoster iqroster=new IqRoster();
    	addBlockListener(iqroster);

    	addBlockListener(new IqVersionReply());

    	iqroster.queryRoster(this);

    	
    	Presence online = new Presence(Presence.PRESENCE_ONLINE, -1, "hello, jabber world!", "evgs");
    	send(online);
    }
    
    private Context serviceContext;
    
	public void setContext(Context context) {
		// TODO Auto-generated method stub
		serviceContext = context;
	}

	public void sendBroadcast(String message) {
		serviceContext.sendBroadcast(new Intent(message));
	}
	
}
