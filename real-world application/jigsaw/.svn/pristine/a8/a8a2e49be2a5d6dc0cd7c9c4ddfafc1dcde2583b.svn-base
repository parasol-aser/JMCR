// DirectoryResource.java
// $Id: DirectoryResource.java,v 1.2 2010/06/15 17:53:05 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.resources ;

import java.util.Hashtable;

import java.io.File;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.ContainerResource;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceContext;
import org.w3c.tools.resources.ResourceReference;

import org.w3c.jigsaw.frames.NegotiatedFrame;

public class DirectoryResource 
             extends org.w3c.tools.resources.DirectoryResource 
{

    /**
     * Attribute index - Should this directory support content negotiation.
     */
    protected static int ATTR_NEGOTIABLE = -1 ;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	// Get a pointer to our class.
	try {
	    cls = Class.forName("org.w3c.jigsaw.resources.DirectoryResource") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The negotiate flag
	a = new BooleanAttribute("negotiable"
				 , Boolean.FALSE
				 , Attribute.EDITABLE) ;
	ATTR_NEGOTIABLE = AttributeRegistry.registerAttribute(cls, a) ;
    }

    /**
     * Get the negotiable flag for this directory.
     * When turned to <strong>true</strong>, this flag indicates to the
     * directory resource that it should automatically build negotiated 
     * resources ont op of all existing resources.
     * <p>You should know, at least, that turning this flag on has some
     * not so small cost in terms of the size of the index files, and some
     * not so small costs in CPU time when detecting not found documents. 
     * Otherwise, in all other situations its cost is probably negligible.
     * @return A boolean, <strong>true</strong> if the directory is extensible
     * <strong>false</strong> otherwise.
     */
    public boolean getNegotiableFlag() {
	return getBoolean(ATTR_NEGOTIABLE, false) ;
    }

    private String getUnextendedName(String name) {
	int strlen = name.length() ;
	for (int i = 0 ; i < strlen ; i++) {
	    // FIXME: Should use the system props to get the right sep
	    if ( name.charAt(i) == '.' ) {
		if ( i == 0 )
		    return null ;
		return name.substring(0, i) ;
	    }
	}
	return null ;
    }

    /**
     * Update a negotiable resource.
     * Given the name of a resource that exists, create or update the 
     * attributes of a resource that allows to negotiate its content.
     * <p>I hate this part here: it has nothing to do within the directory
     * resource itself, and the indexer shouldn't know that much about
     * directory resource, so I am stuck.
     * @param name The name of the newly created resource.
     */
    public synchronized void updateNegotiableResource(String name) {
	// Does the maintainer really wants us to perform this ugly hack ?
	if ( ! getNegotiableFlag() )
	    return ;
	// Check for the corresponding negotiable resource:
	String noext = getUnextendedName(name) ;
	if ( noext == null ) {
	    return ;
	} else {
	    ResourceReference rr = lookup(noext);
	    ResourceReference rr_neg_frame = null;

	    if (rr != null) {
		try {
		    FramedResource r = (FramedResource)rr.lock();
		    Class nClass = 
			Class.forName("org.w3c.jigsaw.frames.NegotiatedFrame");
		    //Added by Jeff Huang
		    //TODO: FIXIT
		    rr_neg_frame = r.getFrameReference(nClass);
		    if (rr_neg_frame == null) 
			return;
		} catch (ClassNotFoundException cex) {
		    return;
		} catch (InvalidResourceException ex) {
		    return;
		} finally {
		    rr.unlock();
		}
	    }

	    if (rr_neg_frame == null) {
		// we can't add a NegotiatedFrame to an existing resource.
		if (rr == null) {
		    // create the resource.
		    FramedResource resource = new FramedResource();
		    Hashtable defs = new Hashtable(5) ;
		    defs.put(id, noext);
		    ResourceContext context = 
			updateDefaultChildAttributes(defs);
		    resource.initialize(defs);
		    addResource(resource, defs);
		    // add a NegotiatedFrame.
		    String variants[] = new String[1] ;
		    variants[0] = name ;
		    NegotiatedFrame negotiated = new NegotiatedFrame();
		    Hashtable f_defs = new Hashtable(5) ;
		    f_defs.put("variants".intern(), variants) ;
		    resource.registerFrame(negotiated, f_defs);
		} 
	    } else {
		try {
		    NegotiatedFrame negotiated = 
			(NegotiatedFrame) rr_neg_frame.lock();
		    String variants[]  = negotiated.getVariantNames() ;
		    boolean exists = false;
		    for ( int i=0 ; i < variants.length ; i++ ) {
			if (variants[i].equals(name))
			    exists = true;
		    }
		    if (!exists) {
			String nvariants[] = new String[variants.length+1] ;
			System.arraycopy(variants, 0, nvariants,
					 0, variants.length);
			nvariants[variants.length] = name ;
			negotiated.setVariants(nvariants) ;
		    }
		} catch (InvalidResourceException ex) {
		    //FIXME
		} finally {
		    rr_neg_frame.unlock();
		}
	    }
	}
    }

    /**
     * Lookup the next component of this lookup state in here.
     * @param ls The current lookup state.
     * @param lr The lookup result under construction.
     * @exception ProtocolException If an error occurs.
     * @return A boolean, <strong>true</strong> if lookup has completed, 
     * <strong>false</strong> if it should be continued by the caller.
     */
    public boolean lookup(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	int remain = ls.countRemainingComponents();
	String name = null;
	// save the name if it is the last
	if (remain == 1) {
	    name = ls.peekNextComponent();
	}
	// if it is ok, great!
	if (super.lookup(ls, lr)) {
	    return true;
	}
	// fails: check if we can create a negotiated resource
	if ( (name != null) && getNegotiableFlag() && getExtensibleFlag()) {
	    // try to create the right negotiated resource
	    String rname = name + '.';
	    File directory = getDirectory() ;
	    if ( directory != null ) {
		synchronized(this) {
		    long dirstamp  = directory.lastModified() ;
		    String lst[] = directory.list() ;
		    // FIXME we should perhaps get also the children
		    // FIXME in case specific config has been done
		    if ( lst != null ) {
			for (int i = 0 ; i < lst.length ; i++) {
			    if (lst[i].equals(".") || 
				lst[i].equals(".."))
				continue ;
			    // if it is not matching the name, abort
			    if (!lst[i].startsWith(rname))
				continue;
			    if (lookup(lst[i]) == null) {
				String indexed = getIndexedName(lst[i]);
				if (indexed.equals(lst[i])) {
				    createDefaultResource(lst[i]) ;
				} else if (super.lookup(indexed) == null) {
				    createDefaultResource(lst[i]) ;
				}
			    }
			    updateNegotiableResource(lst[i]);
			}
		    }
		    setLong(ATTR_DIRSTAMP, dirstamp) ;
		}
	    }
	    ResourceReference rr = null;
	    rr = lookup(name);
	    if (rr == null) {
		lr.setTarget(null);
		return false;
	    }
	    try {
		lr.setTarget(rr);
		FramedResource resource = (FramedResource) rr.lock();
		return (resource != null ) ? resource.lookup(ls, lr) : false;
	    } catch (InvalidResourceException ex) {
		return false;
	    } finally {
		rr.unlock();
	    }	    
	}
	return false;
    }
}
