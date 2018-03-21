// WEBDAV.java
// $Id: WEBDAV.java,v 1.1 2010/06/15 12:27:42 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav;

import org.w3c.www.http.HTTP;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public interface WEBDAV {

    // namespaceURI
    public final static String NAMESPACE_URI      = "DAV:";
    public final static String NAMESPACE_PREFIX   = "D";

    public final static String ENCODING   = "utf-8";

    // WEBDAV headers
    public final static String DAV_HEADER         = "DAV";
    public final static String DEPTH_HEADER       = "Depth";
    public final static String DESTINATION_HEADER = "Destination";
    public final static String IF_HEADER          = "If";
    public final static String LOCK_TOKEN_HEADER  = "Lock-Token";
    public final static String OVERWRITE_HEADER   = "Overwrite";
    public final static String STATUS_URI_HEADER  = "Status-URI";
    public final static String TIMEOUT_HEADER     = "Timeout";

    // WEBDAV header values
    public final static String CLASS_1_COMPLIANT = "1";
    public final static String CLASS_2_COMPLIANT = "1,2";

    public final static int DEPTH_0        = 0;
    public final static int DEPTH_1        = 1;
    public final static int DEPTH_INFINITY = -1;

    // WEBDAV messages
    public static final String dav_msg_100[] = {
	"Continue",				// 100
	"Switching Protocols",			// 101
	"Processing"    			// 102 (WEBDAV specific)
    };

    public static final String dav_msg_200[] = {
	"OK",					// 200
	"Created",				// 201
	"Accepted",				// 202
	"Non-Authoritative information",	// 203
	"No Content",				// 204
	"Reset Content",			// 205
	"Partial Content",			// 206
	"Multi-Status"   			// 207 (WEBDAV specific)
    };
    
    public static final String dav_msg_300[] = HTTP.msg_300;

    public static final String dav_msg_400[] = {
	"Bad Request",				// 400
	"Unauthorized",				// 401
	"Payment Required",			// 402
	"Forbidden",				// 403
	"Not Found",				// 404
	"Method Not Allowed",			// 405
	"Not Acceptable",			// 406
	"Proxy Authentication Required",	// 407
	"Request Timeout",			// 408
	"Conflict",				// 409
	"Gone",					// 410
	"Length Required",			// 411
	"Precondition Failed",			// 412
	"Request Entity Too Large",		// 413
	"Request-URI Too Long",			// 414
	"Unsupported Media Type",		// 415
	"Requested Range Not Satisfiable",      // 416
	"Expectation Failed",                   // 417
	"",                                     // no 418 def
	"",                                     // no 419 def 
	"",                                     // no 420 def
	"",                                     // no 421 def
	"Unprocessable Entity",                 // 422 (WEBDAV specific)
	"Locked",                               // 423 (WEBDAV specific)
	"Failed Dependency"                     // 424 (WEBDAV specific)
    };

    public static final String dav_msg_500[] = {
	"Internal Server Error",		// 500
	"Not Implemented",			// 501
	"Bad Gateway",				// 502
	"Service Unavailable",			// 503
	"Gateway Timeout",			// 504
	"HTTP Version Not Supported",		// 505
	"",                                     // no 506 def
	"Insufficient Storage",                 // 507 (WEBDAV specific)
	"",                                     // no 508 def
	"",                                     // no 509 def
	"Not Extended"                          // 510
    };

    // WEBDAV status code
    public static final int PROCESSING           = 102;

    public static final int MULTI_STATUS         = 207;

    public static final int UNPROCESSABLE_ENTITY = 422;
    public static final int LOCKED               = 423;
    public static final int FAILED_DEPENDENCY    = 424;

    public static final int INSUFFICIENT_STORAGE = 507;

}
