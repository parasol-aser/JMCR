// CCPPFrame.java
// $Id: CCPPFrame.java,v 1.1 2010/06/15 12:28:21 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ccpp;

import java.io.InputStream;

import org.w3c.jigsaw.frames.HTTPExtFrame;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class CCPPFrame extends HTTPExtFrame {

    static {
	String classname = "org.w3c.jigsaw.ccpp.CCPPFrame";
	String methods[] = { "GET", "POST", "HEAD" }; // FIXME PUT? ...
	registerExtension(CCPP.HTTP_EXT_ID, methods, classname);
    }

    /**
     * Get the CC/PP Request
     * @param request the HTTP Request
     * @return a CCPPRequest instance
     */
    public CCPPRequest getCCPPRequest(Request request) {
	return CCPPRequest.getCCPPRequest(request);
    }

    /**
     * Set the Ext and/or C-Ext Header if necessary.
     * @param request the incomming request.
     * @param reply the reply
     * @return the acknowledged reply instance
     */
    protected Reply acknowledgeExtension(Request request, Reply reply) {
	CCPPRequest ccpp = getCCPPRequest(request);
	return ccpp.acknowledge(reply);
    }

    /**
     * The default GET method.
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */

    public Reply get(Request request)
	throws ProtocolException, ResourceException
    {
	return test_get(request);
    }

    /**
     * For testing only
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply test_get(Request request)
	throws ProtocolException, ResourceException
    {
	CCPPRequest ccpp  = getCCPPRequest(request);
	Reply       reply = super.get(request);

	ProfileRef refs[] = ccpp.getProfileReferences();
	for (int i = 0 ; i < refs.length ; i++) {
	    try {
		if (refs[i].isURI()) {
		    System.out.println("Ref["+i+"] (uri) : "+refs[i].getURI());
		    ccpp.addWarning(reply, CCPP.NOT_APPLIED, refs[i].getURI());
		} else {
		    int dn = refs[i].getDiffNumber();
		    System.out.println("Ref["+i+"] (dif) : ("+dn+", "+
				       refs[i].getDiffName()+")");
		    System.out.println("  Diff["+dn+"] : "+
				       ccpp.getProfileDiff(dn));
		}
	    } catch (InvalidProfileException ex) {
		System.out.println("Ref["+i+"] (invalid) : "+
				   refs[i].getUnparsedRef());
	    }
	}
	return reply;
    }

    /**
     * CCPP HEAD method
     */
    public Reply head(Request request)
	throws ProtocolException, ResourceException
    {
	Reply reply = null;
	reply = get(request) ;
	reply.setStream((InputStream) null);
	return reply;
    }

}


