// JdbcConnection.java
// $Id: JdbcConnection.java,v 1.1 2010/06/15 12:27:32 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.jdbc;

import java.io.PrintStream;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.w3c.util.LRUAble;

public class JdbcConnection implements LRUAble {

    public static final boolean debug = false;

    public static final int QUERY_TIMEOUT = 5 * 60; // 5 minutes

    protected long querystamp = -1;

    Connection conn = null;

    // LRUAble interface implementation.
    protected LRUAble lru_next = null;
    protected LRUAble lru_prev = null;
    /**
     * The server this connection is attached to.
     */
    protected JdbcServer  server  = null;

    /**
     * LRUAble interface - Get previous item in the LRU list.
     * @return The previous item, or <strong>null</strong>.
     */

    public LRUAble getNext() {
	return lru_next;
    }

    /**
     * LRUAble interface - Get next item in the LRU list.
     * @return The next item, or <strong>null</strong>.
     */

    public LRUAble getPrev() {
	return lru_prev;
    }

    /**
     * LRUAble interface - Set the next item for this server.
     */

    public void setNext(LRUAble next) {
	lru_next = next;
    }

    /**
     * LRUAble interface - Set the previous item for this server.
     */

    public void setPrev(LRUAble prev) {
	lru_prev = prev;
    }

    protected final JdbcServer getServer() {
	return server;
    }

    /**
     * Mark the connection used.
     * @return true if the connection is usable, false otherwise.
     */
    public synchronized boolean markUsed() {
	try {
	    if (conn != null && conn.isClosed()) {
		try { conn.close(); } catch (Exception ex) {} ; // just in case
		conn = null;
	    }
	} catch (SQLException sqlex) {
	    // isClosed was not possible...
	    try { conn.close(); } catch (Exception ex) {} ; // just in case
	    conn = null;
	}
	if (conn == null) {
	    try {
		DriverManager.setLoginTimeout(60); // 1 minute
		conn = DriverManager.getConnection(server.uri, 
						   server.user,
						   server.password);
	    } catch (SQLException ex) {
		if (debug) {
		    System.err.println(ex.getMessage());
		}
		// not good
		server.unregisterConnection(this);
		server.deleteConnection(this);
		return false;
	    }
	}
	server.unregisterConnection(this);
	return true;
    }

    public ResultSet performQuery(String command)
	throws SQLException
    {
	if (conn != null) {
	    try {
		querystamp = System.currentTimeMillis();
		Statement smt = conn.createStatement();
		smt.setQueryTimeout(QUERY_TIMEOUT);
		ResultSet set = smt.executeQuery(command);
		return set;
	    } finally {
		querystamp = -1;
	    }
	}
	throw new SQLException("no connection");
    }

    public int performUpdate(String command)
	throws SQLException
    {
	if (conn != null) {
	    try {
		querystamp = System.currentTimeMillis();
		Statement smt = conn.createStatement();
		smt.setQueryTimeout(QUERY_TIMEOUT);
		return smt.executeUpdate(command);
	    } finally {
		querystamp = -1;
	    }
	}
	throw new SQLException("no connection");
    }

    public long getQueryStamp() {
	return querystamp;
    }
	
    public DatabaseMetaData getMetaData() 
    	throws SQLException
    {
	if (conn != null) {
	    return conn.getMetaData();
	}
	throw new SQLException("no connection");
    }

    /**
     * Close (if necessary) and delete this connection.
     */
    public void delete() {
	close();
	server.unregisterConnection(this);
	server.deleteConnection(this);
    }

    public void close() {
	if (conn != null) {
	    try {
		conn.close();
	    } catch (SQLException ex) {
		// abort anyway
	    }
	    conn = null;
	}
    }

    public boolean isClosed() {
	if (conn != null) {
	    try {
		return conn.isClosed();
	    } catch (SQLException ex) {
		return true;
	    }
	}
	return true;
    }

    JdbcConnection(JdbcServer server) {
	this.server = server;
    }

}
