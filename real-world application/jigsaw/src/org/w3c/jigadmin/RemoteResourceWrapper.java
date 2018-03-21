// RemoteResourceWrapper.java
// $Id: RemoteResourceWrapper.java,v 1.1 2010/06/15 12:20:47 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin;

import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.jigsaw.admin.RemoteAccessException;

import org.w3c.jigadmin.gui.ServerBrowser;

/**
 * This class is a wrapper for RemoteResource, used to store the
 * resource tree.
 */
public class RemoteResourceWrapper extends org.w3c.jigadm.RemoteResourceWrapper
{
    ServerBrowser sb = null;

    /**
     * Constructor.
     * @param rr The remoteResource.
     * @param sb The ServerBrowser
     */
    public RemoteResourceWrapper(RemoteResource rr, ServerBrowser sb) {
	super(rr);
	this.sb = sb;
    }

    /**
     * Constructor.
     * @param rr The remoteResource.
     * @param rr The father remoteResource.
     * @param sb The ServerBrowser
     */
    public RemoteResourceWrapper(RemoteResource father, 
				 RemoteResource rr, 
				 ServerBrowser sb) 
    {
	super(father, rr);
	this.sb = sb;
    }

    /**
     * Constructor.
     * @param rrwf The father remoteResource wrapper.
     * @param rr The remoteResource.
     */
    public RemoteResourceWrapper(RemoteResourceWrapper rrwf, 
				 RemoteResource rr) 
    {
	super(rrwf, rr);
	this.sb = rrwf.sb;
    }

    /**
     * Constructor.
     * @param rrwf The father remoteResource wrapper.
     * @param rr The remoteResource.
     * @param sb The ServerBrowser
     */
    public RemoteResourceWrapper(RemoteResourceWrapper rrwf, 
				 RemoteResource rr,
				 ServerBrowser sb) 
    {
	super(rrwf, rr);
	this.sb = sb;
    }

    /**
     * Get the associated ServerBrowser
     * @return a ServerBrowser instance
     */
    public ServerBrowser getServerBrowser() {
	return sb;
    }

    /**
     * Get the father RemoteResourceWrapper.
     * @return a RemoteResourceWrapper instance
     */
    public RemoteResourceWrapper getFatherRemoteResourceWrapper() {
	return (RemoteResourceWrapper) getFatherWrapper();
    }

    /**
     * Load and get a child of this wrapped RemoteResource.
     * @param name the child name
     * @return a RemoteResourceWrapper instance
     */
    public RemoteResourceWrapper getChildResource(String name) 
	throws RemoteAccessException
    {
	RemoteResource resource = null;
	resource = getResource().loadResource(name);
	return new RemoteResourceWrapper(this, resource);
    }

}


