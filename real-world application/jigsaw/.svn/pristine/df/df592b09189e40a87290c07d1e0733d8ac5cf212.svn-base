// ConnectionManager.java
// $Id: ConnectionManager.java,v 1.1 2010/06/15 12:27:31 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.jdbc;

import java.sql.SQLException;

import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import java.util.LinkedList;

import org.w3c.util.LRUList;
import org.w3c.util.SyncLRUList;
import org.w3c.util.PropertyMonitoring;
import org.w3c.util.ObservableProperties;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */

class ManagerDescription {
    ConnectionManager manager = null;
    Properties  properties = null;
	
    final ConnectionManager getManager() {
	return manager;
    }
	
    final boolean sameProperties(Properties props) {
	return (Jdbc.getMaxConn(props) == Jdbc.getMaxConn(properties));
    }
	
    ManagerDescription(ConnectionManager manager, Properties props) {
	this.manager    = manager;
	this.properties = props;
    }
}

public class ConnectionManager implements PropertyMonitoring {

    public final static long WAIT_TIMEOUT = 1000 * 60 * 2; // 2 minutes

    public final static boolean debug = false;


    public final static int STATE_UNCHANGED = 0;
    public final static int STATE_CHANGED   = 1;

    protected int state = STATE_UNCHANGED;

    protected int conn_count = 0;
    protected int conn_max;
    protected int conn2free;

    Properties props = null;

    /**
     * To keep track of existing managers
     */
    private static ManagerDescription managers[] = null;

    /**
     * The LRU list of idle connections.
     */
    protected LRUList connectionsLru = null;

    /**
     * The LRU list of used connection
     */
    protected LinkedList usedConnections = null;

    /**
     * PropertyMonitoring implementation.
     */
    public boolean propertyChanged(String name) {
	if (name.equals(Jdbc.MAX_CONNECTIONS_P)) {
	   conn_max  = Jdbc.getMaxConn(props);
	   conn2free = Math.max(conn_max / 5, 1);
	   return true;
	}
	return false;
    }

    /**
     * Get a connection to the given server.
     * @param server the jdbc server.
     * @return a JdbcConnection or null if there is no connection available.
     */
    protected JdbcConnection getConnection(JdbcServer server) {
	JdbcServerState ss   = server.getState();
	JdbcConnection  conn = null;

	while ( true ) {
	    synchronized (this) {
		while ( true ) {
		    conn = ss.getConnection();
		    if ( conn == null )
			break;
		    if (debug) {
			System.err.println("Reusing connection. ");
		    }
		    if (conn.markUsed())
			return conn;
		}
		if (negotiateConnection(server)) {
		    conn = allocateConnection(server);
		    if ( conn.markUsed() ) {
			return conn;
		    } else {
			// Failed to establish a fresh connection !
			return null;
		    }
		}
	    }
	    // Wait for a connection to become available:
	    try {
		waitForConnection(server);
	    } catch (InterruptedException ex) {
	    }
	    if (state == STATE_UNCHANGED) {
		// timeout reached
		if (debug) {
		    System.err.println(">>> Wait timeout reached.");
		}
		freeConnections();
	    }
	}
    }

    protected Comparator conn_comparator = new Comparator() {
	public int compare(Object o1, Object o2) {
	    if ((o1 instanceof JdbcConnection) && 
		(o2 instanceof JdbcConnection))	
		{
		    JdbcConnection conn1 = (JdbcConnection)o1;
		    JdbcConnection conn2 = (JdbcConnection)o2;
		    if (conn1.isClosed()) {
			if (conn2.isClosed()) {
			    return 0;
			}
			return -1;
		    } 
		    return (int)(conn2.getQueryStamp() - 
				 conn1.getQueryStamp());
		}
	    throw new ClassCastException("can't compare");
	}
    };

    /**
     * PANIC, we need to close some used connection.
     */
    protected synchronized void freeConnections() {
	if (debug) {
	    System.err.println(">>> Closing waiting connection");
	}
	Collections.sort(usedConnections, conn_comparator);
	int size = usedConnections.size();
	int i    = (conn2free > size) ? size : conn2free;
	while (i-- > 0) {
	    JdbcConnection conn = 
		(JdbcConnection) usedConnections.removeFirst();
	    // delete it
	    conn.delete();
	}
    }

    /**
     * Connections management - Allocate a new connection for this server.
     * @param server the JdbcServer
     * @return a newly created connection or null
     */
    protected JdbcConnection allocateConnection(JdbcServer server) {
	if (debug) {
	    System.err.println("Allocating new connection to "+server);
	}
	JdbcConnection conn = new JdbcConnection(server);
	notifyConnection(conn);
	return conn;
    }

    protected boolean negotiateConnection(JdbcServer server) {
	if (conn_count >= conn_max) {
	    // remove the oldest idle Connection
	    JdbcConnection conn = (JdbcConnection) connectionsLru.removeTail();
	    if (conn == null) { // no idle connection
		return false;
	    } else {
		if (debug) {
		    System.err.println("DELETING IDLE CONNECTION!!!");
		}
		conn.delete();
	    }
	}
	return true;
    }

    protected void deleteConnection(JdbcConnection conn) {
	JdbcServerState ss = conn.getServer().getState();
	ss.deleteConnection(conn);
	usedConnections.remove(conn);
	synchronized(this) {
	    --conn_count;
	    if (debug) {
		System.err.println("+++ delete conn_count: " + conn_count);
	    }
	    state = STATE_CHANGED;
	    notifyAll();
	}
    }

    /**
     * A new connection has just been created.
     * @param conn the new connection
     */
    protected synchronized void notifyConnection(JdbcConnection conn) {
	++conn_count;
    }

    /**
     * The given connection is about to be used.
     * Update our list of available servers.
     * @param conn The idle connection.
     */
    public void notifyUse(JdbcConnection conn) {
	if (debug) {
	    System.err.println("+++ JdbcConnection used ["+conn_count+"]");
	}
	connectionsLru.remove(conn);
	usedConnections.add(conn);
    }

    /**
     * The given connection can be reused, but is now idle.
     * @param conn The connection that is now idle.
     */
    public synchronized void notifyIdle(JdbcConnection conn) {
	if (debug) {
	    System.err.println("+++ JdbcConnection idle ["+conn_count+"]");
	}
	usedConnections.remove(conn);
	connectionsLru.toHead(conn);
	JdbcServerState ss = conn.getServer().getState();
	ss.registerConnection(conn);
	state = STATE_CHANGED;
	notifyAll();
    }

    /**
     * Wait for a connection to come up.
     * @param server, the target server.
     * @exception InterruptedException If interrupted..
     */

    protected synchronized void waitForConnection(JdbcServer server)
	throws InterruptedException
    {
	state = STATE_UNCHANGED;
	wait(WAIT_TIMEOUT);
    }

    /**
     * Get an instance of the Jdbc manager.
     * This method returns an actual instance of the Jdbc manager. It may
     * return different managers, if it decides to distribute the load on
     * different managers (avoid the ConnectionManager being a bottleneck).
     * @return An application wide instance of the Jdbc manager.
     */

    public static synchronized ConnectionManager getManager(Properties p) { 
	if (managers != null) {
	    for (int i = 0 ; i < managers.length ; i++) {
		if ( managers[i] == null )
		    continue;
		if ( managers[i].sameProperties(p) ) {
		    return managers[i].getManager();
		}
	    }
	}
	ConnectionManager manager = new ConnectionManager(p);
	if (managers != null) {
	    ManagerDescription nm[]= new ManagerDescription[managers.length+1];
	    System.arraycopy(managers, 0, nm, 0, managers.length);
	    nm[managers.length] = new ManagerDescription(manager, p);
	} else {
	    managers    = new ManagerDescription[1];
	    managers[0] = new ManagerDescription(manager, p);
	}
	return manager;
    }

    public static ConnectionManager getManager() {
	return getManager(System.getProperties());
    }

    private ConnectionManager(Properties p) {
	this.props           = p;
	this.connectionsLru  = new SyncLRUList();
	this.usedConnections = new LinkedList();
	if (props instanceof ObservableProperties) {
	    ((ObservableProperties) props).registerObserver(this);
	}
	this.conn_max  = Jdbc.getMaxConn(props);
	this.conn2free = Math.max(conn_max / 5, 1);
    }

}
