// SocketClientFactoryStats.java
// $Id: SocketClientFactoryStats.java,v 1.1 2010/06/15 12:26:09 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http.socket;

public class SocketClientFactoryStats  /* extends ClientFactoryStats */ {
    SocketClientFactory pool = null;

    private static final String __avg[] = { "Light", "Average", 
					    "High", "Dead" };

    public int getFreeConnectionsCount() {
	return pool.freeCount;
    }

    public int getIdleConnectionsCount() {
	return pool.idleCount;
    }

    public int getClientCount() {
	return pool.clientCount;
    }

    public int getLoadAverage() {
	return pool.loadavg;
    }

    public String getServerLoad() {
	return __avg[pool.loadavg-1];
    }

    /*
      SocketClientState cs = null;
      cs = (SocketClientState) pool.freeList.getHead();
      while (cs != null) {
      System.out.println(cs.client
      + " reqcount="
      + cs.client.getRequestCount()
      + ", bindcount="
      + cs.client.getBindCount());
      cs = (SocketClientState)pool.freeList.getNext((LRUAble)cs);
      }
    */

    public SocketClientFactoryStats(SocketClientFactory pool) {
	this.pool = pool;
    }
}
