// RemoteResourceFactory.java 
// $Id: RemoteResourceFactory.java,v 1.1 2010/06/15 12:24:28 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.admin;

import java.net.URL;

import org.w3c.tools.resources.serialization.ResourceDescription;

public class RemoteResourceFactory {
    AdminContext admin = null;

    public RemoteResource createRemoteResource(URL parent, 
					       String identifier,
					       ResourceDescription descr)
    {
	return new PlainRemoteResource(admin, parent, identifier, descr);
    }

    RemoteResourceFactory(AdminContext admin) {
	this.admin = admin;
    }

}
