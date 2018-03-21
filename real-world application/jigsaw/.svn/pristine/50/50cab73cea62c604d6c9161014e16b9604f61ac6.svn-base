// AttRes.java
// $Id: AttRes.java,v 1.2 2010/06/15 17:52:56 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.tests;

import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;
import org.w3c.jigsaw.frames.MimeTypeArrayAttribute;
import org.w3c.www.mime.MimeType;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class AttRes extends FramedResource {

    /**
     * Attribute index - The MTA attribute
     */
    protected static int ATTR_MTA = -1;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	// Get a pointer to our class:
	try {
	    cls = Class.forName("org.w3c.jigsaw.tests.AttRes") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The object identifier, *should* be uniq (see below)
	a = new MimeTypeArrayAttribute("mime-type-array",
				       null,
				       Attribute.EDITABLE);
	ATTR_MTA = AttributeRegistry.registerAttribute(cls, a);
    }

    public MimeType[] getMimeTypeArray() {
	return (MimeType[]) getValue(ATTR_MTA, null);
    }

    public ReplyInterface perform(RequestInterface request) 
	throws ProtocolException, ResourceException
    {
	MimeType mimes[] = getMimeTypeArray();
	System.out.println(mimes);
	for (int i = 0 ; i < mimes.length ; i++)
	    System.out.println("=> "+mimes[i]);
	return super.perform(request);
    }

}
