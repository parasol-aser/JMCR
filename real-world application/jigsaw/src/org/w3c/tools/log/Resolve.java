// Resolve.java
// $Id: Resolve.java,v 1.1 2010/06/15 12:28:37 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1996-1999.
// Please first read the full copyright statement in file COPYRIGHT.html

// a small log resolver that can add timestamps for sorting.
// @author Yves Lafon <ylafon@w3.org>

package org.w3c.tools.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * A simple and a little specific resolver
 * It can add numeric timestamps to ease the sort of the logfile
 * It can do DNS resolution (of course) using multiple threads
 * a -help explains all ;)
 */

public class Resolve {

    public static void main(String args[]) {

	BufferedReader bf        = null;
	int            maxThread = 0;
	boolean        usage     = false;
	int            offset    = 0;
	boolean        timestamp = true;
	boolean        resolve   = true;
	DNSResolver    resolver;

	if (args.length > 0) {
	    while (args.length - offset > 0 && args[offset].charAt(0) == '-') {
		if (args[offset].equals("-thmax")) {
		    offset++;
		    if (resolve) {
			try {
			    maxThread = Integer.parseInt(args[offset]);
			} catch (NumberFormatException nfe) {
			    usage = true;
			}
		    }
		} else if (args[0].equals("-notime")) {
		    timestamp = false;
		} else if (args[0].equals("-nores")) {
		    timestamp = true;
		    resolve = false;
		} else if (args[0].equals("-help")) {
		    usage = true;
		} else {
		    usage = true;
		}
		offset++;
	    }
	}
	if (usage) {
	    System.out.println("Usage: java org.w3c.tools.log.Resolve " + 
			       " [-notime] [-nores] [-thmax #] [logfile]*");
	    System.out.println("       it can also read from stdin.");
	    System.out.println("       java -help");
	    System.exit(0);
	}
	if (args.length - offset > 0) {
	    while (offset < args.length) {
		File logfile = new File(args[offset]) ;    
		FileReader fr;
		
		if (!logfile.exists()) {
		    System.err.println("No logfile of this name :" 
				       + args[offset]);
		    System.exit(1);
		}
		try {
		    fr = new FileReader(logfile);
		} catch (FileNotFoundException ex) {
		    System.err.println("log file " + args[offset] 
				       + "not found!");
		    return;
		}
		bf = new BufferedReader(fr);
		if (maxThread != 0)
		    resolver = new DNSResolver(bf, maxThread, 
					       timestamp, resolve);
		else
		    resolver = new DNSResolver(bf, timestamp, resolve);
		resolver.readLog();		
		offset++;
	    }
	} else {
	    bf = new BufferedReader(new InputStreamReader(System.in));
	    if (maxThread != 0)
		resolver = new DNSResolver(bf, maxThread, timestamp, resolve);
	    else
		resolver = new DNSResolver(bf, timestamp, resolve);
	    resolver.readLog();
	}
    }
}
