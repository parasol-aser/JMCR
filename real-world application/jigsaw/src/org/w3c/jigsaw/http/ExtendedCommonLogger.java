// ExtendedCommonLogger.java
// $Id: ExtendedCommonLogger.java,v 1.1 2010/06/15 12:22:00 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http ;

import java.net.URL;

import java.util.Date ;

import org.w3c.jigsaw.auth.AuthFilter;

/**
 * The ExtendedCommonLogger class implements the abstract Logger class.
 * It just rotates the log every month and use the extended log format
 * @see org.w3c.jigsaw.http.CommonLogger
 */

public class ExtendedCommonLogger extends CommonLogger {

    private   byte                 msgbuf[] = null ;

    /**
     * Log the given HTTP transaction.
     * This is shamelessly slow.
     */

    public void log (Request request, Reply reply, int nbytes, long duration) {
	Client client = request.getClient() ;
	long   date   = reply.getDate();

	String user = (String) request.getState(AuthFilter.STATE_AUTHUSER);
	URL urlst = (URL) request.getState(Request.ORIG_URL_STATE);
	String requrl;
	if (urlst == null) {
	    URL u = request.getURL();
	    if (u == null) {
		requrl = noUrl;
	    } else {
		requrl = u.toExternalForm();
	    }
	} else {
	    requrl = urlst.toExternalForm();
	}
	StringBuffer sb = new StringBuffer(512);
	String logs;
	int status = reply.getStatus();
	if ((status > 999) || (status < 0)) {
	    status = 999; // means unknown
	}
	synchronized(sb) {
	    byte ib[] = client.getInetAddress().getAddress();
	    if (ib.length == 4) {
		boolean doit;
		for (int i=0; i< 4; i++) {
		    doit = false;
		    int b = ib[i];
		    if (b < 0) {
			b += 256;
		    }
		    if (b > 99) {
			sb.append((char)('0' + (b / 100)));
			b = b % 100;
			doit = true;
		    }
		    if (doit || (b > 9)) {
			sb.append((char)('0' + (b / 10)));
			b = b % 10;
		    }
		    sb.append((char)('0'+b));
		    if (i < 3) {
			sb.append('.');
		    }
		}
	    } else { // ipv6, let's be safe :)
		sb.append(client.getInetAddress().getHostAddress());
	    }
	    sb.append(" - ");
	    if (user == null) {
		sb.append("- [");
	    } else {
		sb.append(user);
		sb.append(" [");
	    }
	    dateCache(date, sb);
	    sb.append("] \"");
	    sb.append(request.getMethod());
	    sb.append(' ');
	    sb.append(requrl);
	    sb.append(' ');
	    sb.append(request.getVersion());
	    sb.append("\" ");
	    sb.append((char)('0'+ status / 100));
	    status = status % 100;
	    sb.append((char)('0'+ status / 10));
	    status = status % 10;
	    sb.append((char)('0'+ status));
	    sb.append(' ');
	    if (nbytes < 0) {
		sb.append('-');
	    } else {
		sb.append(nbytes);
	    }
	    if (request.getReferer() == null) {
		if (request.getUserAgent() == null) {
		    sb.append(" \"-\" \"-\"");
		} else {
		    sb.append(" \"-\" \"");
		    sb.append(request.getUserAgent());
		    sb.append('\"');
		}
	    } else {
		if (request.getUserAgent() == null) {
		    sb.append(" \"");
		    sb.append(request.getReferer());
		    sb.append("\" \"-\"");
		} else {
		    sb.append(" \"");
		    sb.append(request.getReferer());
		    sb.append("\" \"");
		    sb.append(request.getUserAgent());
		    sb.append('\"');
		}
	    }
	    sb.append('\n');
	    logs = sb.toString();
	}
	logmsg(logs);
    }

    /**
     * Construct a new Logger instance.
     */

    ExtendedCommonLogger () {
	this.msgbuf = new byte[128] ;
    }   

}
