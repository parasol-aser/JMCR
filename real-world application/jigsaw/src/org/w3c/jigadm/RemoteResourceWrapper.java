// RemoteResourceWrapper.java
// $Id: RemoteResourceWrapper.java,v 1.1 2010/06/15 12:26:36 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm;

import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.jigadm.gui.ServerBrowser;

public class RemoteResourceWrapper  {
    RemoteResourceWrapper rrwf    = null;
    RemoteResource        father  = null;
    RemoteResource        self    = null;
    ServerBrowser         browser = null; // this is a temporary hack

    public RemoteResourceWrapper(RemoteResource rr) {
	self = rr;
    }

    public RemoteResourceWrapper(RemoteResource rr, ServerBrowser sb) {
	self = rr;
	browser = sb;
    }

    public RemoteResourceWrapper(RemoteResource father, RemoteResource rr) {
	self = rr;
	this.father = father;
    }

    public RemoteResourceWrapper(RemoteResource father, RemoteResource rr,
				 ServerBrowser sb) {
	self = rr;
	this.father = father;
	browser = sb;
    } 

    public RemoteResourceWrapper(RemoteResourceWrapper rrwf, 
				 RemoteResource rr) {
	self = rr;
	this.rrwf = rrwf;
    }

    public RemoteResourceWrapper(RemoteResourceWrapper rrwf, 
				 RemoteResource rr, ServerBrowser sb) {
	self = rr;
	this.rrwf = rrwf;
	browser = sb;
    }

    public RemoteResource getFatherResource() {
	if(father != null)
	    return father;
	if(rrwf != null)
	    return rrwf.getResource();
	return null;
    }

    public ServerBrowser getBrowser() {
	return browser;
    }

    public RemoteResourceWrapper getFatherWrapper() {
	return rrwf;
    }

    public RemoteResource getResource() {
	return self;
    }
}


