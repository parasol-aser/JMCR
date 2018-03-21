// DNSEntry.java
// $Id: DNSEntry.java,v 1.1 2010/06/15 12:28:37 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1996-1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.log;

import java.io.Serializable;

/**
 * This class implements a very small DNS entry, aka a host
 * the number of time someone tried to resolve it and a resolved flag
 */

public class DNSEntry implements Serializable {
    String  host     = null;
    boolean resolved = false;
    int     tries    = 0;

    boolean isResolved() {
	return resolved;
    }

    /**
     * when a resolution fails, calling notFound increments the
     * number of tries, if ever the number of tries is high enough
     * the entry is considered to be numeric forever
     */
    synchronized void notFound() {
	tries++;
	if (tries > 4) // enough is enough ;)
	    resolved = true;
    }

    /**
     * set the host of this entry, after a successful resolution
     */
    void setHost(String host) {
	// has been resolved
	this.host = host;
	resolved  = true;
    }

    public DNSEntry(String host, boolean resolved) {
	this.host     = host;
	this.resolved = resolved;
	this.tries    = 0;
    }

    public DNSEntry(String host) {
        this.host     = host;
        this.resolved = true;
    }
}


