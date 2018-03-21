// HttpClient.java
// $Id: HttpClient.java,v 1.1 2010/06/15 12:24:57 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.mux.tests;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import java.net.Socket;
import java.net.URL;

import java.util.Vector;

import org.w3c.www.mux.MuxSession;
import org.w3c.www.mux.MuxStream;

class MuxFetcher extends Thread {
    MuxSession       ses = null;
    DataOutputStream out = null;
    InputStream      in  = null;
    URL              u   = null;
    OutputStream     dst = null;

    public void request(URL u) 
	throws IOException
    {
	out.writeBytes("GET "+u+" HTTP/1.1\r\n");
	out.writeBytes("Connection: close\r\n\r\n");
	out.flush();
    }

    public void copy(URL u, OutputStream dst) 
	throws IOException
    {
	request(u);
	byte buffer[] = new byte[1024];
	int  got      = -1;
	while((got = in.read(buffer, 0, buffer.length)) > 0)
	    dst.write(buffer, 0, got);
    }

    public void dump(URL u) 
	throws IOException
    {
	request(u);
	byte buffer[] = new byte[1024];
	int  got      = -1;
	while ((got = in.read(buffer, 0, buffer.length)) > 0)
	    System.out.print(new String(buffer, 0, 0, got));
	System.out.println("Dumped all available data !");
    }

    public void run() {
	try {
	    // Get the streams:
	    out = (new DataOutputStream
		   (new BufferedOutputStream(ses.getOutputStream())));
	    in  = ses.getInputStream();
	    // Run the command:
	    if ( dst != null )
		copy(u, dst);
	    else
		dump(u);
	    ses.shutdown();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    MuxFetcher(MuxStream stream, URL u) 
	throws IOException
    {
	this(stream, u, null);
    }

    MuxFetcher(MuxStream stream, URL u, OutputStream dst) 
	throws IOException
    {
	this.ses = stream.connect(80);
	this.dst = dst;
	this.u   = u;
	start();
    }

}

public class HttpClient {
    public static MuxStream stream = null;

    public static void main(String args[]) {
	String host = "www43.inria.fr";
	int    port = 8007;
	Vector urls = new Vector();

	// Parse command line:
	for (int i = 0 ; i < args.length ; i++) {
	    if (args[i].equals("-h")) {
		host = args[++i];
	    } else if (args[i].equals("-p")) {
		try {
		    port = Integer.parseInt(args[++i]);
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    } else {
		urls.addElement(args[i]);
	    }
	}
	// Connect to target host:
	try {
	    Socket     socket     = new Socket(host, port);
	    MuxStream  stream     = new MuxStream(false, null, socket);
	    MuxFetcher fetchers[] = new MuxFetcher[urls.size()];
	    // Fork the fetchers:
	    for (int i = 0 ; i < urls.size() ; i++) {
		URL u = new URL((String) urls.elementAt(i));
		fetchers[i] = new MuxFetcher(stream, u);
	    }
	    // Wait for completion:
	    for (int i = 0 ; i < fetchers.length ; i++) {
		fetchers[i].join();
	    }
	    // Close mux stream:
	    stream.shutdown(true);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}

    }

}
