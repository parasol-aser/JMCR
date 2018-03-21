// CCPPRequest.java
// $Id: CCPPRequest.java,v 1.1 2010/06/15 12:28:20 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ccpp;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.www.http.HttpExtList;
import org.w3c.www.http.HttpExt;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpTokenList;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class CCPPRequest implements CCPP {

    Request     request      = null;
    ProfileRef  references[] = null;
    HttpExtList httpextlist  = null;

    /**
     * Get the HTTP Request
     * @return a Request
     */
    public Request getHTTPRequest() {
	return request;
    }

    /**
     * Get the standard CCPP reason phrase for the given warning code.
     * @param warning The given warning code.
     * @return A String giving the standard reason phrase, or
     * <strong>null</strong> if the status doesn't match any knowned error.
     */
    public static String getStandardWarning(int warning) {
	int category = warning / 100;
	int catcode  = warning % 100;
	switch(category) 
	    {
	    case 1:
		if ((catcode >= 0) && (catcode < msg_100.length))
		    return msg_100[catcode];
		break;
	    case 2:
		if ((catcode >= 0) && (catcode < msg_200.length))
		    return msg_200[catcode];
		break;
	    }
	return UNKNOWN_WARNING_MESSAGE;
    }

    /**
     * Get a header value (relative to the CC/PP Extension protocol)
     * @param request the HTTP Request
     * @param header the header name (ie "Profile")
     * @return a String.
     */
    public String getCCPPHeaderValue(String header) {
	return request.getExtHeader(HTTP_EXT_ID, header);
    }

    /**
     * Get the profile diff header relative to the given profile diff number.
     * @param request the HTTP Request
     * @param diffnumber the diff number
     */
    public String getProfileDiff(int diffnumber) 
    {
	String diffname = PROFILE_DIFF_HEADER+"-"+diffnumber;
	return getCCPPHeaderValue(diffname);
    }

    /**
     * Get the Profile references (absolute URI or Profile-diff-name)
     * ordered by priority (the last one has the highest priority).
     * @return a ProfileRef array (or null)
     * @see ProfileRef
     */
    public ProfileRef[] getProfileReferences() {
	if (references == null) {
	    String profile = getCCPPHeaderValue(PROFILE_HEADER);
	    if (profile != null) {
		String profiles[] = 
		    (String[]) HttpFactory.parseTokenList(profile).getValue();
		references = new ProfileRef[profiles.length];
		for (int i = 0 ; i < references.length ; i++) {
		    references[i] = new ProfileRef(profiles[i]);
		}
	    } else {
		return null;
	    }
	}
	return references;
    }

    /**
     * Get the CC/PP Request associated to the given HTTP Request
     * @param request the HTTP Request
     * @return a CCPPRequest instance
     */
    public static CCPPRequest getCCPPRequest(Request request) {
	if (request.hasState(CCPP_REQUEST_STATE)) {
	    return (CCPPRequest) request.getState(CCPP_REQUEST_STATE);
	} else {
	    CCPPRequest ccpprequest = new CCPPRequest(request);
	    request.setState(CCPP_REQUEST_STATE, ccpprequest);
	    return ccpprequest;
	}
    }

    /**
     * Set the Acknowledgement Headers if it's appropriate.
     * @param reply the reply
     * @return the aknowledged reply
     */
    protected Reply acknowledge(Reply reply) {
	HttpExtList man = request.getHttpManExtDecl();
	if ((man != null) && 
	    (man.getLength() == 1) &&
	    (man.getHttpExt(HTTP_EXT_ID) != null)) {
	    reply.setEnd2EndExtensionAcknowledgmentHeader();
	}

	HttpExtList cman = request.getHttpCManExtDecl();
	if ((cman != null) && 
	    (cman.getLength() == 1) &&
	    (cman.getHttpExt(HTTP_EXT_ID) != null)) {
	    reply.setHopByHopExtensionAcknowledgmentHeader();
	}

	return reply;
    }

    /**
     * Add a CC/PP Warning to the given reply.
     * @param reply the HTTP Reply
     * @param warning the CC/PP Warning code
     * @param reference the Profile reference
     */
    public void addWarning(Reply reply, int warning, String reference) {
	CCPPWarning ccppwarning = (CCPPWarning)
	    reply.getState(CCPPWarning.CCPPWARNING_STATE);
	if (ccppwarning == null) {
	    ccppwarning = new CCPPWarning();
	    reply.setState(CCPPWarning.CCPPWARNING_STATE, ccppwarning);
	}
	ccppwarning.addWarning(warning, reference);
	// is the extension declared?
	HttpExtList list = reply.getExtList(HTTP_EXT_ID);
	if (list == null) {
	    list = new  HttpExtList(httpextlist);
	    reply.setHttpExtDecl(list);
	}
	HttpExt ext = list.getHttpExt(HTTP_EXT_ID);
	reply.setExtensionHeader(ext, 
				 PROFILE_WARNING_HEADER, 
				 ccppwarning.toString());
    }

    private CCPPRequest(Request request) {
	this.request     = request;
	this.httpextlist = request.getExtList(HTTP_EXT_ID);
    }
}
