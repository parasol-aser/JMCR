// JdbcServerState.java
// $Id: JdbcServerState.java,v 1.1 2010/06/15 12:27:29 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.jdbc;

import java.util.Vector;

class JdbcServerState {
    JdbcServer server = null;
    Vector     conns  = null;
    
    private static final boolean debug = false;

    final JdbcServer getServer() {
	return server;
    }

    synchronized void registerConnection(JdbcConnection conn) {
	if ( conns == null ) {
	    conns = new Vector(4);
	}
	conns.addElement(conn);
    }

    synchronized void deleteConnection(JdbcConnection conn) {
	if ( conns != null ) {
	    conns.removeElement(conn);
	}
    }

    synchronized void unregisterConnection(JdbcConnection conn) {
	if ( conns != null ) {
	    conns.removeElement(conn);
	}
    }

    synchronized JdbcConnection getConnection() {
	if ((conns != null) && (conns.size() > 0)) {
	    if (debug) {
		System.out.println("+++ GetConnection out of " + conns.size());
	    }
	    JdbcConnection conn = (JdbcConnection) conns.elementAt(0);
	    conns.removeElementAt(0);
	    return conn;
	}
	return null;
    }

    JdbcServerState(JdbcServer server) {
	this.server = server;

    }
}
