// CvsRootResource.java
// $Id: CvsRootPassDirectory.java,v 1.2 2010/06/15 17:53:13 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigedit.cvs2;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.jigsaw.resources.PassDirectory;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class CvsRootPassDirectory
    extends org.w3c.jigsaw.resources.PassDirectory
{

    /**
     * Attribute index, The cvs root.
     */
    public static int ATTR_CVSROOT = -1;

    static {
	Attribute   a = null ;
	Class     cls = null;

	try {
	    cls = 
	      Class.forName("org.w3c.jigedit.cvs.CvsRootPassDirectory") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The Cvs Root
	a = new StringAttribute("cvs-root",
				null,
				Attribute.EDITABLE);
	ATTR_CVSROOT = AttributeRegistry.registerAttribute(cls, a) ;
    }

    protected String getCvsRoot() {
	return getString(ATTR_CVSROOT, null);
    }

    public void setValue(int idx, Object value) {
	super.setValue(idx, value);
	if ( idx == ATTR_CVSROOT ) {
	    //propagate it
	    CvsModule.setValue(getContext(), CvsModule.CVSROOT, value);
	}
    }

    /**
     * Initialize this resource with the given set of attributes.
     * @param values The attribute values.
     */
    public void initialize(Object values[]) {
	super.initialize(values) ;
	disableEvent();
	//propagate the cvs root value (if any)
	String cvsroot = getCvsRoot();
	if (cvsroot != null)
	    CvsModule.setValue(getContext(), CvsModule.CVSROOT, cvsroot);
	enableEvent();
    }
}
