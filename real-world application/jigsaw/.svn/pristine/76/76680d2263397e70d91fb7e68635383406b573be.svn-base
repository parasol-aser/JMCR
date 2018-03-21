// AdminServer.java
// $Id: AdminServer.java,v 1.1 2010/06/15 12:24:28 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.admin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.util.ObservableProperties;

import org.w3c.tools.resources.AbstractContainer;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.DummyResourceReference;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceContext;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ServerInterface;

import org.w3c.www.mime.MimeType;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.jigsaw.daemon.ServerHandler;
import org.w3c.jigsaw.daemon.ServerHandlerInitException;
import org.w3c.jigsaw.daemon.ServerHandlerManager;

import org.w3c.jigsaw.http.ControlResource;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.httpd;

import org.w3c.jigsaw.filters.TEFilter;

import org.w3c.jigsaw.auth.GenericAuthFilter;
import org.w3c.jigsaw.auth.RealmsCatalog;

import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.serialization.Serializer;

class ServerHandlerManagerResource extends AbstractContainer {
    ServerHandlerManager shm = null;
    AdminServer as = null;

    public ServerInterface getServer() {
	return as;
    }

    public ResourceReference lookup(String name) {
	ServerHandler handler = shm.lookupServerHandler(name);
	if (handler != null) {
	    return handler.getConfigResource();
	} 
	return null;
    }

    /**
     * Enumerates all the servers present, as advertised by the 
     * ServerHandlerManager.
     * It adds "realms" and "control", fake resources of the admin server
     */
    public Enumeration enumerateResourceIdentifiers(boolean all) {
	String name;
	Vector v = new Vector();
	Enumeration e = shm.enumerateServerHandlers();
	while(e.hasMoreElements()) {
	    name = (String) e.nextElement();
	    if( !(shm.lookupServerHandler(name) instanceof AdminServer)) {
		v.addElement(name);
	    }
	}
	v.addElement("realms");
	v.addElement("control");
	return v.elements(); 
    }

    public ResourceReference createDefaultResource(String name) {
	// This should in fact create a new server handler
	return null;
    }

    public void registerResource(String name,
				 Resource child,
				 Hashtable defs) 
    {
	throw new RuntimeException("Can't register resource there !");
    }

    public void delete(String name) {
	throw new RuntimeException("no delete supported");
    }

    ServerHandlerManagerResource(AdminServer as, ServerHandlerManager shm) {
	this.as = as;
	this.shm = shm;
    }
}

public class AdminServer extends httpd {
    protected ResourceBroker       broker           = null;
    protected ServerHandlerManager shm              = null;
    protected AdminWriter          writer           = null;
    protected AbstractContainer    root             = null;
    protected ResourceReference    rr_root          = null;
    protected ControlResource      controlConfig    = null;
    protected ResourceReference    rr_controlConfig = null;
    protected GenericAuthFilter    auth             = null;
    protected Serializer           serializer       = null;
    protected ResourceFilter       filters[]        = null;

    /**
     * The realm catalog of the admin server, hardcoded as "adminRealms.db"
     */

    protected RealmsCatalog realms = null ;
    protected ResourceReference rr_realms = null;

    private void initializeRealmsCatalog() {
	this.realms = new RealmsCatalog(
	    new ResourceContext(getDefaultContext()),
	    "adminRealms.db");
    }

    public RealmsCatalog getRealmsCatalog() {
	if(realms == null) {
	    initializeRealmsCatalog();
	} 
	return realms;
    } 

    protected String getBanner() {
	return "JigAdmin[2.2.6]";
    } 

    public ResourceReference getConfigResource() {
	return getRootReference();
    }

    public ResourceReference getRealmCatalogResource() {
	if (rr_realms == null)
	    rr_realms = new DummyResourceReference(getRealmsCatalog());
	return rr_realms;
    }

    public ResourceReference getControlResource() {
	if (rr_controlConfig == null) {
	    controlConfig = new ControlResource(this);
	    rr_controlConfig = new DummyResourceReference(controlConfig);
	}
	return rr_controlConfig;
    }

    /**
     * Load the remote Root.
     * @param request The incomming request.
     * @exception org.w3c.tools.resources.ProtocolException if a protocol 
     * error occurs.
     */
    public Reply remoteLoadRoot(Request request) 
	throws ProtocolException
    {
	// Dump the root resource:
	ByteArrayOutputStream bout = new ByteArrayOutputStream();
	try {
	    writer.writeResource(root, bout);
	} catch (IOException ex) {
	    System.out.println(ex.getMessage());
	    ex.printStackTrace();
	    throw new HTTPException("unable to dump root");
	} catch (Exception ex) {
	    System.out.println(ex.getMessage());
	    ex.printStackTrace();
	    throw new HTTPException("unable to dump root: "+ex.getMessage());
	}
	byte bits[] = bout.toByteArray();
	// Setup the reply:
	Reply reply = request.makeReply(HTTP.OK);
	reply.setStream(new ByteArrayInputStream(bits));
	reply.setContentLength(bits.length);
	reply.setContentType(AdminContext.conftype);
	return reply;
    }

    /**
     * Perform the request
     * @param req the request.
     * @exception ProtocolException if a protocol error occurs
     * @exception ResourceException if a server error occurs
     */
    public ReplyInterface perform(RequestInterface req) 
	throws ProtocolException, ResourceException
    {
	Request request = (Request) req;
	Reply   reply   = null;
	auth.authenticate(request);
	//ingoing filters
	for (int i = 0 ; i < filters.length ; i++) {
	    reply = (Reply) filters[i].ingoingFilter(req);
	    if (reply != null)
		return reply;
	}
	// Maybe that's one of the methods we handle straight ?
	String mth = request.getMethod();
	if ( mth.equals("LOAD-ROOT") ) {
	    reply = remoteLoadRoot(request);
	} else {
	    // Ok, that's targeted toward a specific target resource:
	    try {
		reply = (Reply)broker.perform(request);
	    } catch (org.w3c.tools.resources.ProtocolException ex) {
		throw new HTTPException(ex);
	    }
	}
	//outgoing filters
	for (int i = filters.length-1 ; i > -1 ; i--) {
	    Reply rep = (Reply) filters[i].outgoingFilter(req, reply);
	    if (rep != null)
		return rep;
	}
	return reply;
    }

    public FramedResource getRoot() {
	return root;
    }

    public ResourceReference getRootReference() {
	if (rr_root == null)
	    rr_root = new DummyResourceReference( root );
	return rr_root;
    }

    protected void initializeAuth() {
	Hashtable defs = null;
	defs = new Hashtable(3);
	defs.put("identifier", "auth-frame");
	root.registerFrame(auth, defs);
	auth.setValue("realm", "JigAdmin");
    }

    protected void addFilter(ResourceFilter filter, Hashtable defs) {
	filter.initialize(defs);
	if (filters == null) {
	    filters = new ResourceFilter[1];
	    filters[0] = filter;
	} else {
	    int len = filters.length;
	    ResourceFilter nfilters[] = new ResourceFilter[len+1];
	    System.arraycopy(filters, 0, nfilters, 0, len);
	    nfilters[len] = filter;
	    filters = nfilters;
	}
    }

    /**
     * Initialize the Server
     * @exception ServerHandlerInitException if it can't be initialized
     */
    public void initialize(ServerHandlerManager shm
			   , String identifier
			   , ObservableProperties props)
	throws ServerHandlerInitException
    {
	super.initialize(shm, identifier, props);
	// Create our only resource:
	this.shm   = shm;
	writer     = new AdminWriter();
	broker     = new ResourceBroker(shm, this, writer);
	root       = new ServerHandlerManagerResource(this, shm);
	auth       = new GenericAuthFilter();
	serializer = 
	    new org.w3c.tools.resources.serialization.xml.XMLSerializer();
	initializeAuth();
	TEFilter tefilter = new TEFilter();
	Hashtable defs = new Hashtable(2);
	defs.put("identifier", "tefilter");
	String mimes[] = { AdminContext.conftype.toString() };
	defs.put("mime-types", mimes );
	// FIXME, we should be able to set/change the way filters are added
	// for better security and/or flexibility
	addFilter(tefilter, defs);
    }

    public AdminServer() {
    }
}
