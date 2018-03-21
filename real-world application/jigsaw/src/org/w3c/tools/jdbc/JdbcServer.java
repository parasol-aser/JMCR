// JdbcServer.java
// $Id: JdbcServer.java,v 1.2 2010/06/15 17:53:06 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Vector;
import java.util.HashMap;
import java.util.Properties;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class JdbcServer {

    public static final boolean debug = false;

    String uri      = null;
    String user     = null;
    String password = null;

    /**
     * Our connection manager.
     */
    ConnectionManager manager = null;

    /**
     * Our server state
     */
    JdbcServerState state = null;

    /**
     * The cached servers, index is the jdbc URI
     */
    protected static HashMap servers = new HashMap();

    /**
     * the drivers loaded
     */
    protected static Vector drivers = new Vector();

    /**
     * Default properties
     */
    protected static Properties defaultProps = new Properties();

    /**
     * Get our server state (used to store things)
     * @return a JdbcServerState instance.
     */
    protected final JdbcServerState getState() {
	return state;
    }

    /**
     * Get the Server object from the cache, if it is not present, try
     * to create one. A driver must have been selected.
     * @param uri the jdbc URI of the DB server
     * @param user the user name
     * @param password the password
     * @return an instance of JdbcServer 
     */
    public static JdbcServer getServer(String uri, 
				       String user,
				       String password) 
    {
	return getServer(uri, user, password, null, System.getProperties());
    }

    /**
     * Get the Server object from the cache, if it is not present, try
     * to create one.
     * @param uri the jdbc URI of the DB server
     * @param props the Jdbc properties
     * @return an instance of JdbcServer 
     */
    public static JdbcServer getServer(String uri, Properties props) {
	return getServer(uri, 
			 Jdbc.getUser(props),
			 Jdbc.getPassword(props),
			 Jdbc.getDriver(props),
			 props);
    }

    /**
     * Get the Server object from the cache, if it is not present, try
     * to create one.
     * @param uri the jdbc URI of the DB server
     * @param user the user name
     * @param password the password
     * @param driver the JDBC driver name
     * @return an instance of JdbcServer 
     */
    public static JdbcServer getServer(String uri, 
				       String user,
				       String password,
				       String driver)
    {
	return getServer(uri, user, password, driver, defaultProps);
    }

    /**
     * Get the Server object from the cache, if it is not present, try
     * to create one.
     * @param uri the jdbc URI of the DB server
     * @param user the user name
     * @param password the password
     * @param driver the JDBC driver name
     * @param props the Jdbc properties
     * @return an instance of JdbcServer 
     */
    public static JdbcServer getServer(String uri, 
				       String user,
				       String password,
				       String driver,
				       Properties props) 
    {
	// register the driver
	if (drivers == null)
	    drivers = new Vector(4);
	
	if ((driver != null) && (!drivers.contains(driver))) {
	    try {
		Class.forName(driver);
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	    drivers.addElement(driver);
	}

	String key = null;
	if (user != null) {
	    key = uri + "?" + user;
	} else {
	    key = uri;
	}
	JdbcServer server = null;
	server = (JdbcServer) servers.get(key);
	
	if (server != null) {
	    server.updateProperties(props);
	    return server;
	}
	// no cached server
	ConnectionManager manager = ConnectionManager.getManager(props);
	server = new JdbcServer(uri, user, password, manager);
	servers.put(key, server);
	return server;
    }

    protected void updateProperties(Properties props) {
	this.manager = ConnectionManager.getManager(props);
    }

    /**
     * Unregister a connection from the idle list.
     * Unregistering a connection means that the server shouldn't keep
     * track of it any more. This can happen in two situations:
     * <ul>
     * <li>The connection won't be reusable, so there is no point
     * for the server to try to keep track of it. In this case, the
     * connection is forgotten, and the caller will terminate it by invoking
     * the connection's input stream close method.
     * <li>The connection has successfully handle a connection, and the
     * connection is about to be reused. During the time of the request
     * processing, the server looses track of this connection, which will
     * register itself again when back to idle.
     * @param conn The connection to unregister from the idle list.
     */
    public synchronized void unregisterConnection(JdbcConnection conn) {
	manager.notifyUse(conn);
    }

    public void deleteConnection(JdbcConnection conn) {
	manager.deleteConnection(conn);
    }

    public ResultSet runRequest(String command, boolean close)
	throws SQLException
    {
	return runQuery(command, close);
    }

    public ResultSet runQuery(String command, boolean close)
	throws SQLException
    {
	if (debug) {
	    System.err.println(">>> "+command);
	}

	ResultSet      reply = null;
	JdbcConnection conn  = null;

	// Allocate a connection and run the request:
	int maxretry = 3;
	boolean closed = close;
	for (int i = 0 ; (reply == null) && (i < maxretry) ; i++) {
	    if ((conn  = manager.getConnection(this)) == null)
		continue;
	    try {
		reply = conn.performQuery(command);
	    } catch (SQLException ex) {
		if (debug) {
		    ex.printStackTrace();
		}
		closed = true;
		throw ex;
	    } finally {
		if (closed) {
		    conn.delete();
		} else {
		    manager.notifyIdle(conn);
		}
	    }
	}
	if (conn == null) {
	    throw new SQLException("Can't connect to database");
	}
	return reply;
    }

    public int runUpdate(String command, boolean close)
	throws SQLException
    {
	if (debug) {
	    System.err.println(">>> "+command);
	}

	int            reply = -1;
	JdbcConnection conn  = null;

	// Allocate a connection and run the request:
	int maxretry = 3;
	for (int i = 0 ; (reply == -1) && (i < maxretry) ; i++) {
	    if ((conn  = manager.getConnection(this)) == null)
		continue;
	    try {
		reply = conn.performUpdate(command);
	    } catch (SQLException ex) {
		if (debug) {
		    ex.printStackTrace();
		    System.err.println(">>> STATE : "+ex.getSQLState());
		    System.err.println(">>> code  : "+ex.getErrorCode());
		}
		close = true;
		throw ex;
	    } finally {
		if (close) {
		    conn.delete();
		} else {
		    manager.notifyIdle(conn);
		}
	    }
	}
	if (conn == null) {
	    throw new SQLException("Can't connect to database");
	}
	return reply;
    }

    /**
     * Get the MetaData of the tables.
     * @return a DatabaseMetaData instance
     */
    public DatabaseMetaData getMetaData() 
	throws SQLException
    {
	DatabaseMetaData reply = null;
	JdbcConnection   conn  = null;

	// Allocate a connection and run the request:
	int maxretry = 3;
	for (int i = 0 ; (reply == null) && (i < maxretry) ; i++) {
	    if ((conn  = manager.getConnection(this)) == null)
		continue;
	    try {
		reply = conn.getMetaData();
	    } finally {
		manager.notifyIdle(conn);
	    }
	}
	if (conn == null) {
	    throw new SQLException("Can't connect to database");
	}
	return reply;
    }

    public String toString() {
	return uri;
    }

    /**
     * Constructor
     * @param uri the jdbc URI of the DB server
     * @param user the user name
     * @param password the password
     * @param manager the connection manager
     */
    protected JdbcServer(String uri, 
			 String user, 
			 String password, 
			 ConnectionManager manager)
    {
	this.uri      = uri;
	this.user     = user;
	this.password = password;
	this.state    = new JdbcServerState(this);
	this.manager  = manager;
    }

   
}
