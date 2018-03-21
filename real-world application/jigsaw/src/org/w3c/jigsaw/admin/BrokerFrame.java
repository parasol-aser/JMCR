// BrokerFrame.java
// $Id: BrokerFrame.java,v 1.2 2010/06/15 17:53:08 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.admin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Hashtable;
import java.util.StringTokenizer;

import java.util.zip.GZIPInputStream;

import org.w3c.jigsaw.daemon.ServerHandler;
import org.w3c.jigsaw.daemon.ServerHandlerManager;

import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.ContainerInterface;
import org.w3c.tools.resources.DirectoryResource;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.MultipleLockException;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceReference;

import org.w3c.www.mime.MimeType;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.frames.HTTPFrame;

import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.serialization.ResourceDescription;
import org.w3c.tools.resources.serialization.AttributeDescription;

public class BrokerFrame extends HTTPFrame {

    class LookupFrameState {
	private int     index ;
	private String  components[] ;

	void parseQuery(String query) {
	    StringTokenizer st = new StringTokenizer(query, "?");
	    int nbTokens = st.countTokens();
	    components = new String[nbTokens];
	    for (int i = 0 ; i < nbTokens ; i++)
		components[i] = st.nextToken();
	    index = 0;
	}

	public boolean hasMoreComponents() {
	    return index < components.length ;
	}

	public final String getNextComponent() {
	    return components[index++] ;
	}

	LookupFrameState (String query) {
	    parseQuery(query);
	}
    }

    protected ResourceBroker broker = null;

    public void registerResource(FramedResource resource) {
	super.registerResource(resource);
	if (resource instanceof ResourceBroker)
	    broker = (ResourceBroker) resource;
    }

 
    /**
     * The object that knows how to write the admin protocol.
     */
    protected AdminWriter writer = null;

    /**
     * The ServerHandlerManager we export.
     */
    protected ServerHandlerManager shm = null;
    /**
     * The controlling ServerHandler.
     */
    protected AdminServer admin = null;

    /**
     * Trigger an HTTP exception.
     * @param request The request we couldn't fulfill.
     * @param msg The error message.
     * @exception ProtocolException Always thrown.
     */

    protected void error(Request request, String msg) 
	throws ProtocolException
    {
	Reply reply = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
	reply.setContent(msg);
	throw new HTTPException(reply);
    }

    protected Reply okReply(Request request, byte bits[]) {
	Reply reply = request.makeReply(HTTP.OK);
	reply.setContentType(AdminContext.conftype);
	if ( bits != null ) {
	    ByteArrayInputStream in = new ByteArrayInputStream(bits);
	    reply.setContentLength(bits.length);
	    reply.setStream(in);
	}
	return reply;
    }

    protected Reply okReply(Request request) {
	return okReply(request, null);
    }

    /**
     * Check that request incomming content type.
     * @param request The request to check.
     * @exception ProtocolException If the request type doesn't match admin.
     */

    protected void checkContentType(Request request) 
	throws ProtocolException
    {
	if ( request.getContentType().match(AdminContext.conftype) < 0 ) 
	    error(request, "invalid MIME type: "+request.getContentType());
    }

    /**
     * Get a data input stream out of that request input stream
     * @param request The request to get data from.
     * @exception ProtocolException If we couldn't get the request's content.
     * @return A DataInputStream instance to read the request's content.
     */

    protected InputStream getInputStream(Request request) 
	throws ProtocolException
    {
	// How fun HTTP/1.1 is, allowing us to double the network traffic :-(
	// If this is a 1.1 request, send a 100 continue:
	Client client = request.getClient();
	if ( client != null ) {
	    try {
		client.sendContinue();
	    } catch (IOException ex) {
		throw new HTTPException(ex.getMessage());
	    }
	}
	// Now, only, get the data:
	try {
	    if (request.hasTransferEncoding("gzip"))
		return new GZIPInputStream(request.getInputStream());
	    else
		return request.getInputStream();
	} catch (IOException ex) {
	    error(request, "invalid request");
	}
	// not reached:
	return null;
    }

    /**
     * Lookup the target of the given request.
     * @param request The request whose target is to be fetched.
     * @return A Resource instance.
     * @exception ProtocolException If the resource couldn't be located.
     */

    public ResourceReference lookup(Request request) 
	throws ProtocolException
    {
	// Create lookup state and get rid of root resource requests:
	LookupState   ls = null;
	try {
	    ls = new LookupState(request);
	} catch (org.w3c.tools.resources.ProtocolException ex) {
	    ex.printStackTrace();
	    throw new HTTPException(ex);
	}
	LookupResult  lr = new LookupResult(null);
	ResourceReference rr = null; // cr
	ls.markInternal();
	if ( ! ls.hasMoreComponents() ) 
	    return admin.getRootReference();
	// Lookup the target resource:
	String name = ls.getNextComponent();
	ServerHandler sh = shm.lookupServerHandler(name);
	if ( sh == null ) {
	    if(name.equals("realms")) {
		rr = admin.getRealmCatalogResource();
	    } else if (name.equals("control")) {
		rr = admin.getControlResource();
	    } else {
		error(request, "unknown server handler");
	    }
	} else {
	    // Lookup that resource, from the config resource of that server:
	    rr = sh.getConfigResource();
	}
	if ( rr != null ) {
	    ResourceReference rr_temp = null;
	    while ( ls.hasMoreComponents() ) {
		try {
		    if (rr == null)
			error(request, "url too long");
		    Resource r = rr.lock();
		    if ( ! ( r instanceof ContainerInterface) )
			error(request, "url too long");
		    rr_temp = ((ContainerInterface) r).lookup(
						       ls.getNextComponent());
		} catch (InvalidResourceException ex) {
		    error(request, "unable to restore resource");
		} finally {
		    rr.unlock();
		    rr = rr_temp;
		}
	    }
	    if ( rr == null )
		error(request, "unknown resource");
	    String query = request.getQueryString();
	    if ( query != null ) {
		try {
		    Resource r = rr.lock();
		    // Querying a frame !
		    if ( ! (r instanceof FramedResource) )
			error(request, "not a framed resource");
		} catch (InvalidResourceException ex) {
		    error(request, "unable to restore resource");
		} finally {
		    rr.unlock();
		}

		//search the right frame
		LookupFrameState lfs = new LookupFrameState(query);
	
		String frameName = null;
		ResourceReference the_rrf   = rr; 
		ResourceReference[] rra = null;	  

		while (lfs.hasMoreComponents()) {

		    try {
			rra = ((FramedResource) 
			       the_rrf.lock()).getFramesReference();
			if (rra == null)
			    error(request, "unknown frame");
		    } catch (InvalidResourceException ex) {
			error(request, ex.getMessage());
		    } finally {
			the_rrf.unlock();
		    }
		    the_rrf = null;
		    frameName = lfs.getNextComponent();
		    ResourceReference rrf   = null; 
		    ResourceFrame     frame = null;

		    for (int i = 0 ; i < rra.length ; i++) {
			rrf = rra[i];
			try {
			    frame = (ResourceFrame) rrf.lock();
			    if (frame.getIdentifier().equals(frameName)) {
				the_rrf = rrf;
				break;
			    }
			} catch (InvalidResourceException ex) {
			    error(request, ex.getMessage());
			} finally {
			    rrf.unlock();
			}
		    }
		    if (the_rrf == null)
			error(request,"unknown frame");
		}
	
		return the_rrf;
	    } else {
		// Emit back this resource (after a check):
		return rr;
	    }
	}
	error(request, "unknown resource");
	// not reached
	return null;
    }

    /**
     * Set a set of attribute values for the target resource.
     * @param request The request to handle.
     * @return A Reply instance.
     * @exception ProtocolException If some error occurs.
     */

    public Reply remoteSetValues(Request request) 
	throws ProtocolException
    {
	InputStream in = getInputStream(request);
	//Get the target resource to act on:
	ResourceReference   rr    = lookup(request);
	try {
	    Resource r = rr.lock();
	    ResourceDescription descr = 
		AdminReader.readResourceDescription(in);
	    AttributeDescription attrs[] = descr.getAttributeDescriptions();
	    for (int i = 0 ; i < attrs.length ; i++) {
		AttributeDescription ad = attrs[i];
		r.setValue(ad.getName(), ad.getValue());
	    }
	} catch (InvalidResourceException irex) {
	    irex.printStackTrace();
	    error(request, "Invalid resource");
	} catch (IOException ioex) {
	    error(request, "bad request");
	} catch (AdminProtocolException apex) {
	    error(request, apex.getMessage());
	} finally {
	    rr.unlock();
	}
	// All the changes done, return OK:
	return okReply(request);
    }

    /**
     * Return a resource back to the client.
     * @param request The request to handle.
     * @return A Reply instance.
     * @exception ProtocolException If some error occurs.
     */

    public Reply remoteLoadResource(Request request) 
	throws ProtocolException
    {
	ResourceReference rr = lookup(request);

	// This request has no content
	ByteArrayOutputStream bout   = new ByteArrayOutputStream();

	try {
	    Resource r = rr.lock();
	    writer.writeResource(r, bout);
	} catch (IOException ex) {
	    error(request, "bad request");
	} catch (InvalidResourceException ex) {
	    error(request, "Invalid resource");
	} catch (AdminProtocolException ex) {
	    error(request, ex.getMessage());
	} finally {
	    rr.unlock();
	}
	// Setup the reply:
	return okReply(request, bout.toByteArray());
    }

    public Reply remoteRegisterFrame(Request request) 
	throws ProtocolException
    {
 	ResourceReference rr = lookup(request);
 	try {
 	    Resource r = rr.lock();
 	    if ( ! ( r instanceof FramedResource) )
 		error(request, "can't add frame to non-framed resource");
 	    // Handle the request:
 	    try {
		InputStream in  = getInputStream(request);
		ResourceDescription rd = 
		    AdminReader.readResourceDescription(in);
		String cls = rd.getClassName();
		String id  = rd.getIdentifier();
		// Create the frame:
		ResourceFrame frame = null;
 		try {
 		    frame = (ResourceFrame) Class.forName(cls).newInstance();
 		    //Added by Jeff Huang
 		    //TODO: FIXIT
 		} catch (Exception ex) {
 		    error(request, "invalid frame class "+cls);
 		}
		// Register the frame:
 		Hashtable defs = new Hashtable(3);
 		if ((id != null) && ! id.equals("") ) {
 		    defs.put("identifier", id);
 		}
 		((FramedResource) r).registerFrame(frame, defs);
 		// Send back the whole resource (inclding new frame):
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		writer.writeResource(frame, bout);
		return okReply(request, bout.toByteArray());
	    } catch (IOException ioex) {
		error(request, "bad request");
	    } catch (AdminProtocolException apex) {
		error(request, apex.getMessage());
	    }
	} catch (InvalidResourceException irex) {
	    error(request, "invalid resource");
	} finally {
	    rr.unlock();
	}
	// not reached:
	return null;
    }

    public Reply remoteUnregisterFrame(Request request) 
	throws ProtocolException
    {
 	ResourceReference rr = lookup(request);
 	try{
 	    Resource r = rr.lock();
 	    if(!(r instanceof FramedResource)) {
 		error(request, "Can't unregister frames from a non-framed" +
 		      " resource");
 	    }
 	    try {
		InputStream in = getInputStream(request);
		ResourceDescription rd = 
		    AdminReader.readResourceDescription(in);
		String identifier  = rd.getIdentifier();		
 		// find the indentifier
 		ResourceFrame f[] = ((FramedResource) r).getFrames();
 		for (int i = 0 ; i < f.length ; i++)
 		    if (f[i].getIdentifier().equals(identifier)) {
 			((FramedResource) r).unregisterFrame(f[i]);
 			return okReply(request);
 		    }
 		error(request, "Frame " + identifier + " not registered");
 	    } catch (IOException ex) {
 		error(request, "bad request");
 	    } catch (AdminProtocolException apex) {
		error(request, apex.getMessage());
	    }
 	} catch (InvalidResourceException ex2) {
 	    error(request, "invalid resource");
 	} finally {
 	    rr.unlock();
 	}
	// Not reached
	return null;
    }

    public Reply remoteRegisterResource(Request request) 
	throws ProtocolException
    {
 	// Check target resource class:
 	ResourceReference rr = lookup(request);
 	try {
 	    Resource r = rr.lock();
 	    if ( ! ( r instanceof ContainerInterface) )
 		error(request, "can't add child in non-container");
 	    // Handle request:
 	    try {
		InputStream in = getInputStream(request);
		ResourceDescription rd = 
		    AdminReader.readResourceDescription(in);
		String cls = rd.getClassName();
		String id  = rd.getIdentifier();
 		// Create the resource:
 		Resource child = null;
 		try {
 		    child = (Resource) Class.forName(cls).newInstance();
 		    //Added by Jeff Huang
 		    //TODO: FIXIT
 		} catch (Exception ex) {
 		    error(request, "invalid resource class "+cls);
 		}
 		// Add it to the container:
 		try {
 		    ((ContainerInterface) r).registerResource(id, child, null);
 		} catch (InvalidResourceException ex) {
 		    error(request, ex.getMessage());
 		}
 		// Write back the new resource:
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		writer.writeResource(child, bout);
		return okReply(request, bout.toByteArray());
 	    } catch (IOException ex) {
 		error(request, "bad request");
 	    } catch (AdminProtocolException apex) {
		error(request, apex.getMessage());
	    }
 	} catch (InvalidResourceException ex) {
 	    error(request, "Invalid resource");
 	} finally {
 	    rr.unlock();
 	}
	// not reached:
	return null;
    }

    public Reply remoteReindexResource(Request request, boolean rec) 
	throws ProtocolException
    {
	// Check target resource class:
	ResourceReference rr = lookup(request);
	// Handle request:
	try {
	    Resource r = rr.lock();
	    if(r != null) {
		if (r instanceof org.w3c.tools.resources.DirectoryResource) {
		    org.w3c.tools.resources.DirectoryResource dir = 
			(org.w3c.tools.resources.DirectoryResource) r;
		    dir.reindex(rec);
		    return okReply(request);
		} else {
		    error(request, "Can't reindex this resource"+
			  "(not a DirectoryResource)");
		}
	    } else {
		error(request, "Bad request");
	    }
	} catch (InvalidResourceException ex) {
	    error(request, "Invalid resource");
	} finally {
	    rr.unlock();
	}
	// not reached
	return null;
    }

    public Reply remoteDeleteResource(Request request) 
	throws ProtocolException
    {
	// Check target resource class:
	ResourceReference rr = lookup(request);
	// Handle request:
	try {
	    Resource r = rr.lock();
	    if(r != null) {
		try {
		    r.delete();
		} catch (MultipleLockException ex) {
		    error(request, ex.getMessage());
		}
		return okReply(request);
	    } else {
		error(request, "Bad request");
	    }
	} catch (InvalidResourceException ex) {
	    error(request, "Invalid resource");
	} finally {
	    rr.unlock();
	}
	// not reached
	return null;
    }

    /**
     * Perform an extended request
     * @param request the incomming request.
     * @exception ProtocolException if a protocol error occurs 
     * @exception ResourceException if a server error occurs
     */
    public Reply extended(Request request) 
	throws ProtocolException, ResourceException
    {
	String mth = request.getMethod();
	if ( mth.equals("SET-VALUES") ) {
	    checkContentType(request);
	    return remoteSetValues(request);
	} else if (mth.equals("LOAD-RESOURCE")) {
	    return remoteLoadResource(request);
	} else if (mth.equals("REGISTER-RESOURCE") ) {
	    checkContentType(request);
	    return remoteRegisterResource(request);
	} else if (mth.equals("DELETE-RESOURCE") ) {
	    return remoteDeleteResource(request);
	} else if (mth.equals("REINDEX-RESOURCE") ) {
	    return remoteReindexResource(request, true);
	} else if (mth.equals("REINDEX-LOCALLY") ) {
	    return remoteReindexResource(request, false);
	} else if (mth.equals("UNREGISTER-FRAME")) {
	    checkContentType(request);
	    return remoteUnregisterFrame(request);
	} else if (mth.equals("REGISTER-FRAME")) {
	    checkContentType(request);
	    return remoteRegisterFrame(request);
	} else {
	    return super.extended(request);
	}
    }

    /**
     * The default GET method for other king of associated resource
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply getOtherResource(Request request) 
	throws ProtocolException, ResourceException
    {
	// we don't manage this kind of resource
	Reply error = request.makeReply(HTTP.NOT_IMPLEMENTED) ;
	error.setContent("Method GET not implemented.<br><br>"+
			 "The administration server does not use plain "+
			 "HTTP but a variant of it. The only tool available "+
			 "for now is an application called <b>JigAdmin</b>. "+
			 "Please read the documentation.");
	throw new HTTPException (error) ;
    }

    public BrokerFrame(ServerHandlerManager shm,
		       AdminServer admin,
		       AdminWriter writer)
    {
	super();
	this.shm    = shm;
	this.admin  = admin;
	this.writer = writer;
    }

}
