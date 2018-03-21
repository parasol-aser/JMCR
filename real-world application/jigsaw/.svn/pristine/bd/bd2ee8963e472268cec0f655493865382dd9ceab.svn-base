// DAVManager.java
// $Id: DAVManager.java,v 1.1 2010/06/15 12:28:23 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.protocol.webdav;

import java.util.Hashtable;
import java.util.Properties;

import org.w3c.www.mime.MimeHeaderHolder;
import org.w3c.www.mime.MimeParser;
import org.w3c.www.mime.MimeParserFactory;

import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.HttpException;

class DAVReplyFactory implements MimeParserFactory {

    public MimeHeaderHolder createHeaderHolder(MimeParser parser) {
	return new DAVReply(parser);
    }

}

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVManager extends HttpManager {

    private static Hashtable davmanagers = new Hashtable();

    protected static HttpManager getNewInstance() {
	return new DAVManager();
    }

    /**
     * Get an instance of the WEBDAV manager.
     * This method returns an actual instance of the WEBDAV manager. It may
     * return different managers, if it decides to distribute the load on
     * different managers (avoid the HttpManager being a bottleneck).
     * @return An application wide instance of the WEBDAV manager.
     */

    public static synchronized DAVManager getDAVManager(Properties p) {
	return (DAVManager)getManager(DAVManager.class, p);
    }

    public static DAVManager getDAVManager() {
	return getDAVManager(System.getProperties());
    }

    MimeParserFactory factory = null ;

    public MimeParserFactory getReplyFactory() {
	if (factory == null) {
	    factory = new DAVReplyFactory();
	}
	return factory;
    }

    /**
     * Create a new default outgoing request.
     * This method should <em>always</em> be used to create outgoing requests.
     * It will initialize the request with appropriate default values for 
     * the various headers, and make sure that the request is enhanced by
     * the registered request filters.
     * @return An instance of DAVRequest, suitable to be launched.
     */

    public DAVRequest createDAVRequest() {
	return (DAVRequest) createRequest();
    }
    /**
     * Run the given request, in synchronous mode.
     * This method will launch the given request, and block the calling thread
     * until the response headers are available.
     * @param request The request to run.
     * @return An instance of Reply, containing all the reply 
     * informations.
     * @exception HttpException If something failed during request processing.
     */

    public DAVReply runDAVRequest(DAVRequest request)
	throws HttpException
    {
	return (DAVReply)runRequest(request);
    }

    public DAVManager() {
	super();
	this.template = new DAVRequest(this);
    }

    public static void main(String args[]) {
	try {
	    DAVManager manager = DAVManager.getDAVManager();
	    manager.setGlobalHeader("User-Agent", "Jigsaw/2.1.2 WEBDAV");
	    manager.setGlobalHeader("Accept", "*/*;q=1.0");
	    manager.setGlobalHeader("Accept-Encoding", "gzip");
	    org.w3c.www.protocol.http.PropRequestFilter filter = 
	      new org.w3c.www.protocol.http.cookies.CookieFilter();
	    filter.initialize(manager);
	    org.w3c.www.protocol.http.PropRequestFilter pdebug = 
	      new org.w3c.www.protocol.http.DebugFilter();
	    pdebug.initialize(manager);
	    DAVRequest request = manager.createDAVRequest();
	    request.setURL(new java.net.URL(args[0]));
	    request.setMethod("GET");
	    DAVReply reply = manager.runDAVRequest(request);
	    //Display some infos:
	    System.out.println("last-modified: "+reply.getLastModified());
	    System.out.println("length       : "+reply.getContentLength());
	    // Display the returned body:
	    java.io.InputStream in = reply.getInputStream();
	    byte buf[] = new byte[4096];
	    int  cnt   = 0;
	    while ((cnt = in.read(buf)) > 0) 
	      System.out.print(new String(buf, 0, cnt));
	    System.out.println("-");
	    in.close();
	    manager.sync();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	System.exit(1);
	
    }
    
}
