// PlainRemoteResource.java
// $Id: PlainRemoteResource.java,v 1.1 2010/06/15 12:24:26 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.admin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.SimpleAttribute;
import org.w3c.tools.resources.ArrayAttribute;

import org.w3c.tools.resources.serialization.AttributeDescription;
import org.w3c.tools.resources.serialization.EmptyDescription;
import org.w3c.tools.resources.serialization.ResourceDescription;

import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;

public class PlainRemoteResource implements RemoteResource {
    private static final boolean debug = false;
    /**
     * The client side admin context
     */
    protected AdminContext admin = null;
    /**
     * The remote resource set of attributes.
     */
    protected AttributeDescription attributes[];
    /**
     * The remote resource attribute values.
     */
    protected Object values[] = null;
    /**
     * Is that resource a container resource ?
     */
    protected boolean iscontainer = false;
    /**
     * Is that resource a indexers catalog ?
     */
    protected boolean isindexerscatalog = false;
    /**
     * Is that resource a directory resource ?
     */
    protected boolean isDirectoryResource = false;
    /**
     * Is that resource a framed resource ?
     */
    protected boolean isframed = false;
    /**
     * The name of that resource (ie it's identifier attribute).
     */
    protected String identifier = null;
    /**
     * The name of the parent of that resource, as an URL.
     */
    protected URL parent = null;
    /**
     * The admin URL for the wrapped resource.
     */
    protected URL url = null;
    /**
     * Set of attached frames.
     */
    protected RemoteResource frames[] = null;
    /**
     * Our description
     */
    protected ResourceDescription description = null;

    protected Request createRequest() {
	Request request = admin.http.createRequest();
	request.setURL(url);
	if (!debug) {
	    request.setValue("TE", "gzip");
	}
	return request;
    }

    protected InputStream getInputStream(Reply reply) 
	throws IOException
    {
	if (reply.hasTransferEncoding("gzip"))
	    return new GZIPInputStream(reply.getInputStream());
	else
	    return reply.getInputStream();	
    }

    protected void setFrames(RemoteResource frames[]) {
	this.isframed  = true;
	this.frames    = frames;
    }

    /**
     * Get the target resource class hierarchy.
     * This method will return the class hierarchy as an array of String. The
     * first string in the array is the name of the resource class itself, the
     * last string will always be <em>java.lang.Object</em>.
     * @return A String array givimg the target resource's class description.
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public String[] getClassHierarchy()
	throws RemoteAccessException
    {
	return description.getClassHierarchy();
    }

    /**
     * Reindex the resource's children if this resource is a DirectoryResource.
     * @exception RemoteAccessException If it's not a DirectoryResource
     */
    public void reindex(boolean rec)
	throws RemoteAccessException
    {
	if (isDirectoryResource()) {
	    try {
		Request req = createRequest();
		// Prepare the request:
		if (rec) {
		    req.setMethod("REINDEX-RESOURCE");
		} else {
		    req.setMethod("REINDEX-LOCALLY");
		}
		// Run it:
		Reply rep = admin.runRequest(req);
	    } catch (RemoteAccessException rae) {
		throw rae;
	    } catch (Exception ex) {
		ex.printStackTrace();
		throw new RemoteAccessException(ex.getMessage());
	    }
	} else {
	    throw new RemoteAccessException("Error, can't reindex! This is "+
					    "not a DirectoryResource.");
	}
    }

    /**
     * Delete that resource, and detach it from its container.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public void delete()
	throws RemoteAccessException
    {
	try {
	    Request req = createRequest();
	    // Prepare the request:
	    req.setMethod("DELETE-RESOURCE");
	    // Run it:
	    Reply rep = admin.runRequest(req);
	} catch (RemoteAccessException rae) {
	    throw rae;
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RemoteAccessException(ex.getMessage());
	}
    }

    /**
     * Get the target resource list of attributes.
     * This method returns the target resource attributes description. The
     * resulting array contains instances of the Attribute class, one item
     * per described attributes.
     * <p>Even though this returns all the attribute resources, only the
     * ones that are advertized as being editable can be set through this
     * interface.
     * @return An array of Attribute.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public synchronized AttributeDescription[] getAttributes()
	throws RemoteAccessException
    {
	return description.getAttributeDescriptions();
    }

    /**
     * @param name The attribute whose value is to be fetched, encoded as
     * its name.
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public Object getValue(String attr)
	throws RemoteAccessException
    {
	if (attr.equals("identifier")) {
	    return identifier;
	}
	String attrs[] = new String[1];
	attrs[0] = attr;
	return getValues(attrs)[0];
    }

    protected AttributeDescription lookupAttribute(String name) {
	AttributeDescription attds[] = description.getAttributeDescriptions();
	for (int i = 0 ; i < attds.length ; i++) {
	    AttributeDescription ad = attds[i];
	    if (ad.getName().equals(name))
		return ad;
	}
	return null;
    }

    /**
     * @param attrs The (ordered) set of attributes whose value is to be
     * fetched.
     * @return An (ordered) set of values, one per queried attribute.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public Object[] getValues(String attrs[])
	throws RemoteAccessException
    {
	Object values[] = new Object[attrs.length];
	for (int i = 0 ; i < attrs.length ; i++) {
	    AttributeDescription ad = lookupAttribute(attrs[i]);
	    if (ad != null) {
		values[i] = ad.getValue();
	    } else {
		values[i] = null;
	    }
	}
	return values;
    }

    /**
     * @param attr The attribute to set, encoded as it's name.
     * @param value The new value for that attribute.
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public void setValue(String attr, Object value)
	throws RemoteAccessException
    {
	String attrs[] = new String[1];
	Object vals[] = new Object[1];
	attrs[0] = attr;
	vals[0] = value;
	setValues(attrs, vals);
    }

    /**
     * Set a set of attribute values in one shot.
     * This method guarantees that either all setting is done, or none of
     * them are.
     * @param attrs The (ordered) list of attribute to set, encoded as their
     * names.
     * @param values The (ordered) list of values, for each of the above
     * attributes.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public void setValues(String names[], Object values[])
	throws RemoteAccessException
    {
	String  newId  = null;
	boolean change = false;

	AttributeDescription attrs[] = new AttributeDescription[names.length];

	for (int i = 0 ; i < names.length ; i++) {
	    AttributeDescription ad = lookupAttribute(names[i]);
	    if (ad != null) {
		ad.setValue(values[i]);
		attrs[i] = ad;
	    }
	    if (names[i].equals("identifier")) {
		change = true;
		newId = (String) values[i];
	    }
	}

	ResourceDescription descr = description.getClone(attrs);

	try {
	    ByteArrayOutputStream bout = new ByteArrayOutputStream();
	    OutputStream out;
	    if (debug) {
		out = bout;
	    } else {
		out = new GZIPOutputStream(bout);
	    }
	    admin.writer.writeResourceDescription(descr, out);
	    byte bits[] = bout.toByteArray();

	    Request req = createRequest();
	    req.setMethod("SET-VALUES");
	    req.setContentType(admin.conftype);
	    req.setContentLength(bits.length);
	    if (!debug) {
		req.addTransferEncoding("gzip");
	    }
	    req.setOutputStream(new ByteArrayInputStream(bits));
	
	    // Run that request:
	    Reply rep = admin.runRequest(req);
	} catch (RemoteAccessException rae) {
	    throw rae;
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RemoteAccessException("exception "+ex.getMessage());
	}

	if(change) {
	    identifier = new String(newId);
	    try {
		//if (parent != null) {
		if (! isFrame()) {
		    if (iscontainer)
			url = new URL(parent.toString()+identifier+"/");
		    else 
			url = new URL(parent.toString()+identifier);
		    // update frames url
		    updateURL(new URL(parent.toString()+identifier));
		} else {
		    String oldFile = url.getFile();
		    int index = oldFile.lastIndexOf('?');
		    String newFile = oldFile.substring(0, index);
		    updateURL(new URL(url, newFile));
		}
	    } catch (MalformedURLException ex) {
		ex.printStackTrace();
	    }
	}
	return;
    }

    public void updateURL(URL parentURL) {
	if ( isFrame() ) {
	    try {
		url = new URL(parentURL, parentURL.getFile()+"?"+identifier);
	    } catch (MalformedURLException ex) {
		return;
	    }
	}
	//update frames URLs
	if (frames != null) {
	    for(int i=0 ; i < frames.length ; i++) {
		frames[i].updateURL(url);
	    }
	}
    }

    /**
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public boolean isContainer()
	throws RemoteAccessException
    {
	// a Hack to avoid multiple sub-trees under the main root resource
	if(identifier != null) {
	    if(identifier.equals("root"))
		return false;
	    if(identifier.equals("control")) {
		String classname = getClassHierarchy()[0];
		if(classname.equals("org.w3c.jigsaw.http.ControlResource"))
		    return false;
	    }
	}
	return iscontainer;
    }

    /**
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public boolean isIndexersCatalog()
	throws RemoteAccessException
    {
	return isindexerscatalog;
    }

    /**
     * Is is a DirectoryResource
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public boolean isDirectoryResource()
	throws RemoteAccessException
    {
	if(identifier != null) {
	    if(identifier.equals("root"))
		return false;
	}
	return isDirectoryResource;
    }

    /**
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public String[] enumerateResourceIdentifiers()
	throws RemoteAccessException
    {
	if ( ! iscontainer )
	    throw new RuntimeException("not a container");
	try {
	    update();
	    return description.getChildren();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RemoteAccessException("http "+ex.getMessage());
	}
    }

    /**
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public RemoteResource loadResource(String identifier)
	throws RemoteAccessException
    {
	try {
	    // Prepare the request:
	    Request req = createRequest();
	    req.setMethod("LOAD-RESOURCE");
	    req.setURL(new URL(url.toString()+
			       URLEncoder.encode(identifier)));
	    // Run it:
	    Reply rep = admin.runRequest(req);
	    // Decode the reply:
	    InputStream in = getInputStream(rep);
	    RemoteResource  ret = 
		admin.reader.readResource(url,identifier,in);
	    in.close();
	    return ret;
	} catch (RemoteAccessException rae) {
	    throw rae;
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RemoteAccessException(ex.getMessage());
	}
    }

    /**
     * Register a new resource within this container.
     * @param id The identifier of the resource to be created.
     * @param classname The name of the class of the resource to be added.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public RemoteResource registerResource(String id, String classname) 
	throws RemoteAccessException
    {
	ResourceDescription   rd   = new EmptyDescription(classname, id);
	try {
	    Request req = createRequest();
	    // Prepare the request:
	    req.setMethod("REGISTER-RESOURCE");
	    req.setContentType(admin.conftype);
	    req.setURL(url);
	    ByteArrayOutputStream bout = new ByteArrayOutputStream();
	    OutputStream out;
	    if (debug) {
		out = bout;
	    } else { 
		out = new GZIPOutputStream(bout);
	    }
	    admin.writer.writeResourceDescription(rd, out);
	    byte bits[] = bout.toByteArray();
	    req.setContentLength(bits.length);
	    if (!debug) {
		req.addTransferEncoding("gzip");
	    }
	    req.setOutputStream(new ByteArrayInputStream(bits));
	    // Run it:
	    Reply rep = admin.runRequest(req);

	    // Decode the result:
	    rd = admin.reader.readResourceDescription(getInputStream(rep));
	    RemoteResource ret = 
		new PlainRemoteResource(admin, url, rd.getIdentifier(), rd);
	    
	    return ret;
	} catch (RemoteAccessException rae) {
	    throw rae;
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RemoteAccessException(ex.getMessage());
	}
    }

    /**
     * Is this resource a framed resource ?
     * @return A boolean, <strong>true</strong> if the resource is framed
     * and it currently has some frames attached, <strong>false</strong>
     * otherwise.
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public boolean isFramed() 
	throws RemoteAccessException
    {
	return isframed;
    }

    /**
     * Get the frames attached to that resource.
     * Each frame is itself a resource, so it is returned as an instance of
     * a remote resource.
     * @return A (posssibly <strong>null</strong>) array of frames attached
     * to that resource.
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public RemoteResource[] getFrames()
	throws RemoteAccessException
    {
	if ( ! isframed )
	    throw new RuntimeException("not a framed resource");
	return frames;
    }

    /**
     * Unregister a given frame from that resource.
     * @param frame The frame to unregister.
     * @exception RemoteAccessException If somenetwork failure occured.
     */
    public void unregisterFrame(RemoteResource frame)
	throws RemoteAccessException
    {
	if ( ! isframed )
	    throw new RuntimeException("not a framed resource");
	if ( frames == null )
	    throw new RuntimeException("this resource has no frames");
	// Remove it:
	String id = null;
	try {
	    id = ((PlainRemoteResource)frame).identifier;
	    Request req = createRequest();
	    // Prepare the request:
	    req.setMethod("UNREGISTER-FRAME");
	    req.setContentType(admin.conftype);
	    req.setURL(url);

	    ResourceDescription dframe = new EmptyDescription("", id);
	    ByteArrayOutputStream bout = new ByteArrayOutputStream();
	    OutputStream out;
	    if (debug) {
		out = bout;
	    } else {
		out = new GZIPOutputStream(bout);
	    }
	    admin.writer.writeResourceDescription(dframe, out);
	    byte bits[] = bout.toByteArray();
	    req.setContentLength(bits.length);
	    if (!debug) {
		req.addTransferEncoding("gzip");
	    }
	    req.setOutputStream(new ByteArrayInputStream(bits));

	    // Run it:
	    Reply rep = admin.runRequest(req);	
	} catch (RemoteAccessException rae) {
	    throw rae;
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RemoteAccessException(ex.getMessage());
	}
	RemoteResource f[] = new RemoteResource[frames.length-1];
	int j = 0;
	for (int i = 0; i < frames.length ; i++) {
	    if ( ((PlainRemoteResource)frames[i]).identifier.equals(id)) {
		// got it, copy the end of the array
		System.arraycopy(frames, i+1, f, j, frames.length-i-1);
		frames = f;
		return;
	    } else {
		try {
		    f[j++] = frames[i];
		} catch (ArrayIndexOutOfBoundsException ex) {
		    return; // no modifications, return
		}
	    }
	}
    }

    public boolean isFrame() {
	return isFrameURL(url);
    }

    protected boolean isFrameURL(URL furl) {
	return (furl.toString().lastIndexOf('?') != -1);
    }

    /**
     * Attach a new frame to that resource.
     * @param identifier The name for this frame (if any).
     * @param clsname The name of the frame's class.
     * @return A remote handle to the (remotely) created frame instance.
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public RemoteResource registerFrame(String id, String classname)
	throws RemoteAccessException
    {
	// Can we add new resources ?
	if ( ! isframed )
	    throw new RuntimeException("not a framed resource");
	try {
	    Request req = createRequest();
	    // Prepare the request:
	    req.setMethod("REGISTER-FRAME");
	    req.setContentType(admin.conftype);

	    ResourceDescription dframe = new EmptyDescription(classname, id);
	    ByteArrayOutputStream bout = new ByteArrayOutputStream();
	    OutputStream out;
	    if (debug) {
		out = bout;
	    } else {
		out = new GZIPOutputStream(bout);
	    }
	    admin.writer.writeResourceDescription(dframe, out);
	    byte bits[] = bout.toByteArray();
	    req.setContentLength(bits.length);
	    if (!debug) {
		req.addTransferEncoding("gzip");
	    }
	    req.setOutputStream(new ByteArrayInputStream(bits));

	    // Run it:
	    Reply rep = admin.runRequest(req);
	    dframe = 
		admin.reader.readResourceDescription(getInputStream(rep));
	    id = dframe.getIdentifier();
	    URL url = null;
	    if (isFrame()) {
		url = new URL(this.url, this.url.getFile()+"?" + id);
	    } else {
		url = new URL(parent.toString() + 
			      identifier+"?"+id);
	    }
	    PlainRemoteResource frame  = 
		new PlainRemoteResource(admin, parent, url, id, dframe);
	    //insert it in the frame array
	    if ( frames != null ) {
		RemoteResource nf[] = new RemoteResource[frames.length+1];
		System.arraycopy(frames, 0, nf, 0, frames.length);
		nf[frames.length] = frame;
		frames = nf;
	    } else {
		frames    = new RemoteResource[1];
		frames[0] = frame;
	    }
	    return frame;
	} catch (RemoteAccessException rae) {
	    throw rae;
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RemoteAccessException(ex.getMessage());
	}
    }

    protected void createRemoteFrames() {
	ResourceDescription dframes[] = description.getFrameDescriptions();
	int                 len       = dframes.length;
	this.frames                   = new RemoteResource[len];
	for (int i = 0 ; i < len ; i++) {
	    ResourceDescription dframe     = dframes[i];
	    String              frameid    = dframe.getIdentifier();
	    URL url = null;
	    try {
		if (isFrame()) {
		    url = new URL(this.url, this.url.getFile()+"?" + frameid);
		} else {
		    url = new URL(parent.toString() + 
				  identifier+"?"+frameid);
		}
	    } catch (MalformedURLException ex) {
		ex.printStackTrace();
		url = null;
	    }
	    PlainRemoteResource frame  = 
		new PlainRemoteResource(admin, parent, url, frameid, dframe);
	    frames[i] = frame;
	}
    }

    /**
     * Dump that resource to the given output stream.
     * @param prt A print stream to dump to.
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public void dump(PrintStream prt)
	throws RemoteAccessException
    {
	// Dump the class hierarchy:
	System.out.println("+ classes: ");
	String classes[] = getClassHierarchy();
	for (int i = 0 ; i < classes.length ; i++) 
	    System.out.println("\t"+classes[i]);
	// Any frames available ?
	if ( isframed && (frames != null) ) {
	    System.out.println("+ "+frames.length+" frames.");
	    for (int i = 0 ; i < frames.length ; i++) {
		prt.println("\t"+((PlainRemoteResource)frames[i]).identifier);
		((PlainRemoteResource) frames[i]).dump(prt);
	    }
	}
	// Run the query, and display results:
	System.out.println("+ attributes: ");
	AttributeDescription attrs[]   = getAttributes();
	for (int i = 0 ; i < attrs.length ; i++) {
	    Attribute att = attrs[i].getAttribute();
	    if (att.checkFlag(Attribute.EDITABLE)) {
		Object value = attrs[i].getValue();
		if (value != null) {
		    if (att instanceof SimpleAttribute) {
			SimpleAttribute sa = (SimpleAttribute) att;
			prt.println("\t"+att.getName()+"="+
				    sa.pickle(attrs[i].getValue()));
		    } else if (att instanceof ArrayAttribute) {
			ArrayAttribute aa = (ArrayAttribute) att;
			String values[] = aa.pickle(attrs[i].getValue());
			prt.print("\t"+att.getName()+"=");
			for (int j = 0 ; j < values.length ; j++) {
			    if (j != 0)
				prt.print(" | ");
			    prt.print(values[j]);
			}
		    }
		}
		else
		    prt.println("\t"+att.getName()+" <undef>");
	    }
	}
    }

    /**
     * reload the RemoteResource.
     */
    protected void update() 
	throws RemoteAccessException
    {
	try {
	    Request req = createRequest();
	    // Prepare the request:
	    req.setMethod("LOAD-RESOURCE");
	    // Run it:
	    Reply rep = admin.runRequest(req);
	    InputStream in = getInputStream(rep);
	    this.description = 
		admin.reader.readResourceDescription(in);
	    createRemoteFrames();
	} catch (RemoteAccessException rae) {
	    throw rae;
	} catch (Exception ex) {
	    throw new RemoteAccessException(ex.getMessage());
	}
    }

    PlainRemoteResource(AdminContext admin, 
			URL parent, 
			String identifier,
			ResourceDescription description)
    {
	this(admin, parent, null, identifier, description);
    }

    PlainRemoteResource(AdminContext admin, 
			URL parent,
			URL url,
			String identifier,
			ResourceDescription description)
    {
	this.admin       = admin;
	this.parent      = parent;
	this.identifier  = identifier;
	this.description = description;

	String classes[] = description.getClassesAndInterfaces();
	for (int i = 0 ; i < classes.length ; i++) {
	    if (classes[i].equals(
			       "org.w3c.tools.resources.ContainerInterface")) 
		iscontainer = true;
	    if (classes[i].equals("org.w3c.tools.resources.FramedResource"))
		isframed = true;
	    if (classes[i].equals("org.w3c.tools.resources.DirectoryResource"))
		isDirectoryResource = true;
	    if (classes[i].equals(
			    "org.w3c.tools.resources.indexer.IndexersCatalog"))
		isindexerscatalog = true;
	}

	if (url == null) {
	    if (parent != null) {
		String encoded = ((identifier == null) ? identifier :
				  URLEncoder.encode(identifier));
		String urlpart = iscontainer ? encoded+"/" : encoded;
		try {
		    this.url = ((identifier != null)
				? new URL(parent.toString() + urlpart)
				: parent);
		} catch (MalformedURLException ex) {
		    ex.printStackTrace();
		    this.url = null;
		}
	    } else {
		this.url = null;
	    }
	} else {
	    this.url = url;
	}
	createRemoteFrames();
    }

}


