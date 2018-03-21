// URLUtils.java
// $Id: URLUtils.java,v 1.1 2010/06/15 12:25:37 smhuang Exp $
// (c) COPYRIGHT ERCIM, Keio and MIT, 2003.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.util;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

public class URLUtils {
    
    static Method url_defport;
    
    static {
	try {
	    Class c = java.net.URL.class;
	    url_defport = c.getMethod("getDefaultPort", (Class [])null);
	} catch (NoSuchMethodException ex) {
	    // not using a recent jdk...
	    url_defport = null;
	}
    }
    
    /**
     * Checks that the protocol://host:port part of two URLs are equal.
     * @param u1, the first URL to check
     * @param u2, the second URL to check
     * @return a boolean, true if the protocol://host:port part of the URL
     * are equals, false otherwise
     */
    public static boolean equalsProtocolHostPort(URL u1, URL u2) {
	if ((u1 == null) || (u2 == null)) {
	    return false;
	}
	// check that the protocol are the same (as it impacts the 
	// default port check
	if (!u1.getProtocol().equalsIgnoreCase(u2.getProtocol())) {
	    return false;
	}
	// check that both hostnames are equal
	if (!u1.getHost().equalsIgnoreCase(u2.getHost())) {
	    return false;
	}
	int u1p = u1.getPort();
	int u2p = u2.getPort();
	// if port is ok, it's good!
	if (u1p == u2p) {
	    return true;
	} else if ((u1p>0) && (u2p>0)) {
	    return false;
	}
	// otherwise, the painful comparison of -1 and such
	if (url_defport != null) {
	    if (u1p == -1) {
		try {
		    int u1dp;
		    u1dp = ((Integer) url_defport.invoke(u1, 
						 (Object [])null)).intValue();
		    return (u2p == u1dp);
		} catch (InvocationTargetException ex) {
		} catch (IllegalAccessException iex) {
		}
	    } else {
		try {
		    int u2dp;
		    u2dp = ((Integer) url_defport.invoke(u2, 
						 (Object [])null)).intValue();
		    return (u1p == u2dp);
		} catch (InvocationTargetException ex) {
		} catch (IllegalAccessException iex) {
		}
	    }
	} 
	// no JDK 1.4 this is becoming painful...
	if (u1p == -1) {
	    String s = u1.getProtocol();
	    int u1dp = 0;
	    if (s.equalsIgnoreCase("http")) {
		u1dp = 80;
	    } else if (s.equalsIgnoreCase("https")) {
		u1dp = 443;
	    } // FIXME do others?
	    return (u2p == u1dp);
	} else {
	    String s = u2.getProtocol();
	    int u2dp = 0;
	    if (s.equalsIgnoreCase("http")) {
		u2dp = 80;
	    } else if (s.equalsIgnoreCase("https")) {
		u2dp = 443;
	    } // FIXME do others?
	    return (u1p == u2dp);
	}
    }

    /**
     * normalize an URL, 
     * @param u, the URL to normalize
     * @return a new URL, the normalized version of the parameter, or
     * the u URL, if something failed in the process
     */
    public static URL normalize(URL u) {
	String proto = u.getProtocol().toLowerCase();
	String host = u.getHost().toLowerCase();
	int port = u.getPort();
	
	if (port != -1) {
	    if (url_defport != null) {
		try {
		    int udp;
		    udp = ((Integer) url_defport.invoke(u, 
						(Object [])null)).intValue();
		    // we have the default, skip the port part
		    if (udp == port) {
			port = -1;
		    }
		} catch (InvocationTargetException ex) {
		} catch (IllegalAccessException iex) {
		}	
	    } else {
		switch(port) {
		case 21:
		    if (proto.equals("ftp")) {
			port = -1;
		    }
		    break;
		case 80:
		    if (proto.equals("http")) {
			port = -1;
		    }
		    break;
		case 443:
		    if (proto.equals("https")) {
			port = -1;
		    }
		    break;
		}
	    }
	}
	try {
	    URL _nu;
	    if (port == -1) {
		_nu = new URL(proto, host, u.getFile());
	    } else {
		_nu = new URL(proto, host, port, u.getFile());
	    }
	    return _nu;
	} catch (MalformedURLException ex) {
	}
	return u;
    }
}
