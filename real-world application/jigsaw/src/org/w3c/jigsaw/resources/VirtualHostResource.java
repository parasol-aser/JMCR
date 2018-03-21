// VirtualHostResource.java
// $Id: VirtualHostResource.java,v 1.1 2010/06/15 12:20:39 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html 

package org.w3c.jigsaw.resources; 

import java.util.Hashtable;
import org.w3c.tools.resources.ContainerResource ; 
import org.w3c.tools.resources.ResourceContext ; 

public class VirtualHostResource extends ContainerResource {

    /**
     * Update default child attributes.
     * A parent can often pass default attribute values to its children,
     * such as a pointer to itself (the <em>parent</em> attribute).
     * <p>This is the method to overide when you want your container
     * to provide these kinds of attributes. By default this method will set
     * the following attributes:
     * <dl><dt>name<dd>The name of the child (it's identifier) - 
     * String instance.
     * <dt>parent<dd>The parent of the child (ie ourself here) - 
     * a ContainerResource instance.
     * <dt>url<dd>If a <em>identifier</em> attribute is defined, that
     * attribute is set to the full URL path of the children.
     * </dl>
     */

    protected ResourceContext updateDefaultChildAttributes(Hashtable attrs) {
	ResourceContext context = super.updateDefaultChildAttributes(attrs);
	if (context == null) {
	    context = new ResourceContext(getContext());
	    attrs.put(co, context) ;
	}
	attrs.put(ur, "/");
	return context;
    }
}


