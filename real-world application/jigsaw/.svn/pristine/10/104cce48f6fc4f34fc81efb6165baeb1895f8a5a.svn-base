// AdminContext.java
// $Id: AdminContext.java,v 1.1 2010/06/15 12:24:27 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.admin;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import java.util.Hashtable;

import java.util.zip.GZIPInputStream;

import java.net.URL;

import org.w3c.www.mime.MimeType;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpCredential;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;

/**
 * The client side Admin context.
 */

public class AdminContext {
    public static final boolean debug = false;

    public static MimeType conftype = null;
    static {
	try {
	    conftype = 
	     new MimeType("application/xml;type=jigsaw-config;charset=UTF-8");
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
    }

    /**
     * Cached attribute descriptions:
     */
    protected Hashtable cachedattrs = new Hashtable();
    /**
     * The root URL for the target admin serv(l)er
     */
    protected URL server = null;
    /**
     * The RemoteRoot resource for all administered servers.
     */
    protected RemoteResource root = null;
    /**
     * The HTTP manager used to access the remote admin server.
     */
    protected HttpManager http = null;
    /**
     * The Admin protocol decoder.
     */
    protected AdminReader reader = null;
    /**
     * The Admin protocol encoder.
     */
    protected AdminWriter writer = null;
    /**
     * The credential used for authentification
     */
    protected HttpCredential credential = null;

    protected InputStream getInputStream(Reply reply) 
	throws IOException
    {
	if (reply.hasTransferEncoding("gzip"))
	    return new GZIPInputStream(reply.getInputStream());
	else
	    return reply.getInputStream();
    }

    protected String getContent(Reply reply) {
	try {
	    InputStream is = getInputStream(reply);
	    if (is == null)
		return null;
	    BufferedInputStream bis = new BufferedInputStream( is );
	    byte b[] = new byte[256];
	    StringBuffer buffer = new StringBuffer();
	    while ( (bis.read(b, 0, 256)) != -1)
		buffer.append( new String(b) );
	    bis.close();
	    return new String(buffer);
	} catch (IOException ex) {
	    return null;
	}
    }

    /**
     * Run the given (tunneling) HTTP request.
     * This method will check that the appropriate MIME types are used.
     * @param request The request ti run.
     * @return A Reply instance.
     * @exception RemoteAccessException If some network error occurs.
     */

    protected Reply runRequest(Request request) 
	throws RemoteAccessException
    {
	if(credential != null) {
	    request.setAuthorization(credential);
	}
	try {
	    Reply    reply = http.runRequest(request);
	    MimeType type  = reply.getContentType();
	    if( reply.getStatus() == HTTP.UNAUTHORIZED) {
		    getInputStream(reply).close();
		throw new RemoteAccessException("Unauthorized");
	    }
	    if ((type == null) || (type.match(conftype) < 0)) {
	      String content = getContent(reply);
	      if (content != null) 
		throw new RemoteAccessException(content);
	      throw new RemoteAccessException("invalid content type");
	    }
	    return reply;
	} catch (RemoteAccessException ex) {
	    throw ex;
	} catch (Exception ex) {
	    throw new RemoteAccessException(ex.getMessage());
	}
    }

    /**
     * Query the admin server for its main root resource.
     * @exception RemoteAccessException If some network error occurs.
     */

    protected synchronized void loadRoot()
	throws RemoteAccessException
    {
	try {
	    // Prepare the HTTP request:
	    Request req = http.createRequest();
	    req.setMethod("LOAD-ROOT");
	    req.setURL(server);
	    req.setValue("TE","gzip");
	    // Run that request:
	    Reply rep = runRequest(req);
	    // Decode the reply:
	    root = reader.readResource(server, null, getInputStream(rep));
	} catch (RemoteAccessException ex) {
	    if (debug)
		ex.printStackTrace();
	    throw ex;
	} catch (Exception ex) {
	    root = null;
	    if (debug)
		ex.printStackTrace();
	    throw new RemoteAccessException(ex.getMessage());
	}
    }

    /**
     * Get the root admin for all servers managed by target admin server.
     * @return A RemoteResource instance.
     * @exception RemoteAccessException If target server is unreachable.
     */

    public synchronized RemoteResource getAdminResource() 
	throws RemoteAccessException
    {
	if ( root == null )
	    loadRoot();
	return root;
    }

    /**
     * sets the credential to be used for authentification
     */

    public void setCredential(HttpCredential cr) {
	credential = cr;
    }

    /**
     * initialize the context
     * @exception RemoteAccessException if a remote access error occurs.
     */ 
    public void initialize() 
	throws RemoteAccessException
    {
	loadRoot();
    }

    /**
     * Connect to the given admin server.
     * @exception RemoteAccessException if a remote access error occurs.
     */

    public AdminContext(URL server) 
	throws RemoteAccessException
    {
	this.server = server;
	this.http   = HttpManager.getManager();
	this.reader = new AdminReader(this);
	this.writer = new AdminWriter();
    }

    public static void main(String args[]) {
	try {
	    AdminContext adm = new AdminContext(new URL(args[0]));
	    adm.initialize();
	    String       cmd = args[1];
	    // Get the root resource:
	    PlainRemoteResource root = (PlainRemoteResource) adm.root;
	    // Lookup some child:
	    RemoteResource child = root;
	    for (int i = 2; i < args.length ; i++) 
		child = child.loadResource(args[i]);
	    // Perform a command:
	    if ( cmd.equals("dump") ) {
		((PlainRemoteResource) child).dump(System.out);
	    } else if ( cmd.equals("list") ) {
		if ( ! child.isContainer() ) {
		    System.out.println("\tnot a container !");
		} else {
		    String e[] = child.enumerateResourceIdentifiers();
		    for (int i = 0 ; i < e.length ; i++)
			System.out.println("\t"+e[i]);
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
