// DeletedEntry.java
// $Id: DeletedEntry.java,v 1.1 2010/06/15 12:27:53 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.jigedit.filters; 

import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.FileResource;

import org.w3c.jigsaw.http.Request;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DeletedEntry extends PutedEntry {

    private boolean confirmed = false;

    public void confirm() {
	confirmed = true;
    }

    public boolean isConfirmed() {
	return confirmed;
    }

    protected String getKey() {
	return getURL();
    }

    static PutedEntry makeEntry(Request request) {
	ResourceReference rr = request.getTargetResource();
	Resource          r  = null;
	if (rr != null) {
	    try {
		r = rr.lock();
		// Build an entry:
		DeletedEntry e = new DeletedEntry();
		e.setValue(ATTR_URL, request.getURL().toExternalForm());
		if ( r instanceof FileResource )
		    e.setValue(ATTR_FILENAME,
			       ((FileResource) r).getFile().getAbsolutePath());
		// Update other infos:
		e.update(request);
		return e;
	    } catch (InvalidResourceException ex) {
		return null;
	    } finally {
		rr.unlock();
	    }
	}
	return null;
    }

}
