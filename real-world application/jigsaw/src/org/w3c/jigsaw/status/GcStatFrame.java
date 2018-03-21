// GcStatFrame.java
// $Id: GcStatFrame.java,v 1.2 2010/06/15 17:53:08 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.status ;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpMessage;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.IntegerAttribute;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.frames.HTTPFrame;

import org.w3c.jigsaw.html.HtmlGenerator ;

/**
 * This class implements a GC counter.
 * It counts the number of GC that has occured since the system was brought up.
 */

class GcCounter {
    private static int count= 0 ;

    private static synchronized void incrCounter() {
	count++ ;
    }

    private static synchronized int getCount() {
	return count ;
    }

    public static int getGcCount() {
	System.runFinalization() ;
	return getCount() ;
    }

    public void finalize() {
	incrCounter() ;
	new GcCounter() ;
    }

    static {
	new GcCounter() ;
    }
}

/**
 * Each time you get this resource, it fill run the GC.
 */

public class GcStatFrame extends HTTPFrame {
    private static int REFRESH_DEFAULT = 30;

    /**
     * Attribute index - Our refresh interval.
     */
    protected static int ATTR_REFRESH = -1 ;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	try {
	    cls = Class.forName("org.w3c.jigsaw.status.GcStatFrame");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The refresh interval attribute:
	a = new IntegerAttribute("refresh"
				 , new Integer(5)
				 , Attribute.EDITABLE) ;
	ATTR_REFRESH = AttributeRegistry.registerAttribute(cls, a) ;
    }

    protected Runtime runtime = null ;

    public void registerResource(FramedResource resource) {
	super.registerOtherResource(resource);
    }

    /**
     * Dump the current memory status.
     * @param request The request we are to reply to.
     */

    protected Reply dumpMemoryStatus (Request request) {
	HtmlGenerator g = new HtmlGenerator ("Memory Status") ;
	int refresh = getInt(ATTR_REFRESH, REFRESH_DEFAULT);
	if (refresh > 0) {
	    g.addMeta("Refresh", Integer.toString(refresh));
	}
	addStyleSheet(g);
	g.append("<h1>Memory status</h1>") ;
	long bytes = runtime.freeMemory();
	long kbytes = bytes / 1024;
	long mbytes = kbytes / 1024;
	if (mbytes != 0) {
	    g.append("<p>Free Memory: " +mbytes+ "Mb, " +kbytes % 1024+ "Kb, " 
		     + bytes % 1024 + " (" + Long.toString(bytes) +")");
	} else if (kbytes != 0) {
	    g.append("<p>Free Memory: " + kbytes + "Kb, " + bytes % 1024 +
		     " (" + Long.toString(bytes) +")");
	} else {
	    g.append("<p>Free Memory:" + Long.toString(bytes));
	}
	bytes = runtime.totalMemory();
	kbytes = bytes / 1024;
	mbytes = kbytes / 1024;
	if (mbytes != 0) {
	    g.append("<p>Total Memory: "+mbytes+"Mb, " +kbytes % 1024+ "Kb, " 
		     + bytes % 1024 + " (" + Long.toString(bytes) +")");
	} else if (kbytes != 0) {
	    g.append("<p>Total Memory: " + kbytes + "Kb, " + bytes % 1024 
		     + " (" + Long.toString(bytes) +")");
	} else {
	    g.append("<p>Total Memory:", Long.toString(bytes));
	}
	g.append("<p>GC count: "+GcCounter.getGcCount()) ;
	g.append("<hr>") ;
	// Reply back:
	Reply  reply = request.makeReply(HTTP.OK) ;
	reply.setNoCache();
	reply.setStream (g) ;
	reply.setDynamic(true);
	return reply ;
    }

    /**
     * Perform a GC and display memory status.
     * @param request The request to handle.
     */

    public Reply get (Request request) {
	return dumpMemoryStatus(request) ;
    }

    /**
     * Initialize the thread lister.
     * Just get a pointer to our runtime object.
     * @param values The default attribute values.
     */

    public void initialize(Object values[]) {
	super.initialize(values) ;
	runtime = Runtime.getRuntime() ;
    }    
}
