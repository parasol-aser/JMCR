// AdminReader.java 
// $Id: AdminReader.java,v 1.1 2010/06/15 12:24:27 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.admin;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.net.URL;

import org.w3c.tools.resources.Resource;

import org.w3c.tools.resources.serialization.ResourceDescription;
import org.w3c.tools.resources.serialization.SerializationException;
import org.w3c.tools.resources.serialization.Serializer;

class AdminReader implements AdminProtocol {
    /**
     * The RemoteResource factory to create remote resource instances.
     */
    RemoteResourceFactory factory = null;
    /**
     * The client side admin object we are attached to.
     */
    protected AdminContext admin = null;

    /**
     * Our serializer.
     */
    protected static Serializer serializer = null;

    static {
	serializer = 
	    new org.w3c.tools.resources.serialization.xml.XMLSerializer();
    }

    protected RemoteResource readResource(URL parent,
					  String identifier,
					  InputStream in)
	throws IOException, AdminProtocolException
    {
	try {
	    Reader reader = new BufferedReader(new InputStreamReader(in));
	    ResourceDescription descriptions[] = 
		serializer.readResourceDescriptions(reader);
	    if (descriptions.length < 1)
		throw new AdminProtocolException("Unknown resource");
	    return factory.createRemoteResource(parent, 
						identifier,
						descriptions[0]);
	} catch (SerializationException ex) {
	    throw new AdminProtocolException("Error in serialized resource :"+
					     ex.getMessage());
	}
    }

    public static Resource readResource(InputStream in)
	throws IOException, AdminProtocolException
    {
	try {
	    Reader reader = new BufferedReader(new InputStreamReader(in));
	    Resource resources[] =
		serializer.readResources(reader);
	    if (resources.length < 1)
		throw new AdminProtocolException("No resource found.");
	    return resources[0];
	} catch (SerializationException ex) {
	    throw new AdminProtocolException("Error in serialized resource :"+
					     ex.getMessage());
	}
    }

    public static ResourceDescription readResourceDescription(InputStream in)
	throws IOException, AdminProtocolException
    {
	try {
	    Reader reader = new BufferedReader(new InputStreamReader(in));
	    ResourceDescription descriptions[] = 
		serializer.readResourceDescriptions(reader);
	    if (descriptions.length < 1) {
		throw new AdminProtocolException("No resource found.");
	    }
	    return descriptions[0];
	} catch (SerializationException ex) {
	    throw new AdminProtocolException("Error in serialized resource :"+
					     ex.getMessage());
	}
    }

    AdminReader(AdminContext admin) {
	this.admin      = admin;
	this.factory    = new RemoteResourceFactory(admin);
    }

}


