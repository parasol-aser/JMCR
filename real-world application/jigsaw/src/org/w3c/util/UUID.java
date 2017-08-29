// UUID.java
// $Id: UUID.java,v 1.1 2010/06/15 12:25:40 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.w3c.tools.crypt.Md5;

/**
 * A UUID (from java.rmi.server.UID)
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public final class UUID {

    /**
     * @serial Integer that helps create a unique UID.
     */
    private int unique;

    /**
     * @serial Long used to record the time.  The <code>time</code>
     * will be used to create a unique UID.
     */
    private long time;

    /**
     * InetAddress to make the UID globally unique
     */
    private static String address;

    /**
     * a random number
     */ 
    private static int hostUnique;

    /**
     * Used for synchronization
     */
    private static Object mutex;

    private static long lastTime;
    private static long DELAY;

    private static String generateNoNetworkID() {
	Thread current = Thread.currentThread();
	String nid = current.activeCount()+
	    System.getProperty("os.version")+
	    System.getProperty("user.name")+
	    System.getProperty("java.version");
	System.out.println(nid);
	Md5 md5 = new Md5(nid);
	md5.processString();
	return md5.getStringDigest();
    }

    static {
	hostUnique = (new Object()).hashCode();
	mutex      = new Object();
	lastTime   = System.currentTimeMillis();
	DELAY      = 10; // in milliseconds
	try {
	    String s   = InetAddress.getLocalHost().getHostAddress();
	    Md5    md5 = new Md5(s);
	    md5.processString();
	    address = md5.getStringDigest();
	} catch (UnknownHostException ex) {
	    address = generateNoNetworkID();
	}
    }

    public UUID() {
	synchronized (mutex) {
	    boolean done = false;
	    while (!done) {
		time = System.currentTimeMillis();
		if (time < lastTime+DELAY) {
		    // pause for a second to wait for time to change
		    try {
			Thread.currentThread().sleep(DELAY);
		    } catch (java.lang.InterruptedException e) {
		    }	// ignore exception
		    continue;
		} else {
		    lastTime = time;
		    done = true;
		}
	    }
	    unique = hostUnique;
	}
    }

    public String toString() {
	return 
	    Integer.toString(unique,16)+ "-"+
	    Long.toString(time,16) + "-" +
	    address;
    }

    public boolean equals(Object obj) {
	if ((obj != null) && (obj instanceof UUID)) {
	    UUID uuid = (UUID)obj;
	    return (unique == uuid.unique &&
		    time == uuid.time &&
		    address.equals(uuid.address));
	} else {
	    return false;
	}
    }
    
    public static void main(String args[]) {
	System.out.println(new UUID());
	System.out.println(new UUID());
	System.out.println(new UUID());
	System.out.println(new UUID());
    }

}
