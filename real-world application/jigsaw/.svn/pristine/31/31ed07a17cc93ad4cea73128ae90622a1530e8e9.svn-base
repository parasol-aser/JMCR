// httpdStatistics.java
// $Id: httpdStatistics.java,v 1.1 2010/06/15 12:21:57 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http;

import java.net.URL;

// FIXME

import org.w3c.jigsaw.http.socket.SocketClientFactory;
import org.w3c.jigsaw.http.socket.SocketClientFactoryStats;

/**
 * This class maintains server wide statistics about hits.
 * This class should probably be coded as a resource itself, and made 
 * accessible through a specific HTTPResource. I am just having fun 
 * for the time being.
 */

public class httpdStatistics {
    protected httpd server = null ;

    /**
     * The min processing time in ms.
     */
    protected long r_min = Long.MAX_VALUE ;
    /**
     * The min processing time in ms for dynamic content
     */
    protected long rd_min = Long.MAX_VALUE ;
    /**
     * The URL that has been processed the fastest.
     */
    protected URL u_min = null ;
    /**
     * The URL that has been processed the fastest for dynamic content
     */
    protected URL ud_min = null ;
    /**
     * The maximum processing time in ms.
     */
    protected long r_max = 0 ;
    /**
     * The maximum processing time in ms for dynamic content
     */
    protected long rd_max = 0 ;
    /**
     * The URL that has been processed the slowest.
     */
    protected URL u_max = null ;
    /**
     * The URL that has been processed the slowest for dynamic content
     */
    protected URL ud_max = null ;
    /**
     * The total number of hits.
     */
    protected long t_hits  = 0 ;
    /**
     * The total number of hits for dynamic content
     */
    protected long td_hits  = 0 ;   
    /**
     * The total number of emited bytes.
     */
    protected long t_bytes = 0 ;
    /**
     * The total number of emited bytes for dynamic content
     */
    protected long td_bytes = 0 ;
    /**
     * The total time spent in processing requests in ms.
     */
    protected long t_req = 0 ;
    /**
     * The total time spent in processing requests in ms for dynamic content
     */
    protected long td_req = 0 ;
    /**
     * The date at which the server was started (ms since Java epoch).
     */
    protected long start_time = 0;

    // FIXME temporary hack
    protected SocketClientFactoryStats factoryStats = null;
    protected boolean init = false;

    /**
     * Update the current statistics with the given request.
     * @param client The client that processed the request.
     * @param request The request that has been processed.
     * @param nbytes The number of emited bytes in reply's body.
     * @param duration The processing time of the request.
     */

    protected synchronized void updateStatistics(Client client
						 , Request request, Reply reply
						 , int nbytes
						 , long duration) {
	if (reply.isDynamic()) {
	    if ( duration > rd_max ) {
		rd_max = duration ;
		ud_max = request.getURL() ;
	    }
	    if ( duration < rd_min ) {
		rd_min = duration ;
		ud_min = request.getURL() ;
	    }
	    td_req   += duration ;
	    td_bytes += nbytes ;
	    td_hits++ ;
	} else {
	    if ( duration > r_max ) {
		r_max = duration ;
		u_max = request.getURL() ;
	    }
	    if ( duration < r_min ) {
		r_min = duration ;
		u_min = request.getURL() ;
	    }
	    t_req   += duration ;
	    t_bytes += nbytes ;
	    t_hits++ ;
	}
    }

    /**
     * Get the current server load.
     * @return A number between <strong>1</strong> and <strong>4</strong>.
     */

    public int getServerLoad() {
	if (!init) {
	    initFactoryStats();
	}
	if (factoryStats != null) {
	    return factoryStats.getLoadAverage();
	}
	// return server.pool.loadavg;
	return -1;
    }

    /**
     * Get the number of free threads in the server.
     * @return The number of threads ready to server client requests.
     */

    public int getFreeThreadCount() {
	if (!init) {
	    initFactoryStats();
	}
	if (factoryStats != null) {
	    return factoryStats.getFreeConnectionsCount();
	}
        // return server.pool.freeCount;
	return -1;
    }

    /**
     * Get the number of idle threads in the server.
     * Idle threads are the threads ready to accept more requests on a given
     * connection.
     * @return The number of idle threads.
     */

    public int getIdleThreadCount() {
	if (!init) {
	    initFactoryStats();
	}
	if (factoryStats != null) {
	    return factoryStats.getIdleConnectionsCount();
	}
	// return server.pool.idleCount;
	return -1;
    }

    /**
     * Get the total number of client threads.
     * @return The total number of created threads.
     */

    public int getTotalThreadCount() {
	if (!init) {
	    initFactoryStats();
	}
	if (factoryStats != null) {
	    return factoryStats.getClientCount();
	}
	// return server.pool.clientCount;
	return -1;
    }

    /**
     * Get the total number of hits.
     * @return The total number of processed requests since the server is up.
     */

    public long getHitCount() {
	return (t_hits+td_hits) ;
    }

    /**
     * Get the total number of hits for dynamic content.
     * @return The total number of processed requests since the server is up.
     */

    public long getDynamicHitCount() {
	return td_hits ;
    }

    /**
     * Get the total number of hits for static content.
     * @return The total number of processed requests since the server is up.
     */

    public long getStaticHitCount() {
	return t_hits ;
    }

    /**
     * Get the mean request processing time.
     * @return The average time to process a request.
     */

    public long getMeanRequestTime() {
	return ((t_hits+td_hits) > 0) ? (t_req+td_req) / (t_hits+td_hits) : -1;
    }

    /**
     * Get the mean request processing time for dynamic content
     * @return The average time to process a request.
     */

    public long getMeanDynamicRequestTime() {
	return (td_hits > 0) ? (td_req / td_hits) : -1;
    }

    /**
     * Get the mean request processing time.
     * @return The average time to process a request.
     */

    public long getMeanStaticRequestTime() {
	return (t_hits > 0) ? (t_req / t_hits) : -1;
    }

    /**
     * Get the max request processing time.
     * @return A long giving the maximum duration for a request.
     */

    public long getMaxRequestTime() {
	return Math.max(r_max,rd_max) ;
    }

    /**
     * Get the max request processing time for dynamic content
     * @return A long giving the maximum duration for a request.
     */

    public long getMaxDynamicRequestTime() {
	return rd_max ;
    }
    /**
     * Get the max request processing time for static content
     * @return A long giving the maximum duration for a request.
     */

    public long getMaxStaticRequestTime() {
	return r_max ;
    }

    /**
     * Get the URL of the request that took the longest time to be processed.
     * @return A String giving the URL of the corresponding request, or
     *    <strong>null</strong> if no request has been processed yet.
     */

    public URL getMaxRequestURL() {
	return (r_max > rd_max) ? u_max : ud_max ;
    }

    /**
     * Get the min request processing time.
     * @return A long giving the minimum request processing time.
     */

    public long getMinRequestTime() {
	return Math.min(r_min, rd_min) ;
    }

    /**
     * Get the min request processing time for dynamic content
     * @return A long giving the minimum request processing time.
     */

    public long getMinDynamicRequestTime() {
	return rd_min ;
    }

    /**
     * Get the min request processing time.
     * @return A long giving the minimum request processing time.
     */

    public long getMinStaticRequestTime() {
	return r_min ;
    }
    /**
     * Get the URL of the request that took the smallest time to be processed.
     * @return A String giving the URL of the corresponding request, or
     *    <strong>null</strong> if no request has been processed yet.
     */

    public URL getMinRequestURL() {
	return (r_min < rd_min) ? u_min : ud_min ;
    }

    /**
     * Get the total number of bytes emited.
     * @return A long giving the total number of bytes emited by the server.
     *    This count that not include the reply's header, but only the reply's
     *    body (or <em>entity</em> size).
     */

    public long getEmittedBytes() {
	return t_bytes + td_bytes ;
    }

    /**
     * Get the time at which the server was started.
     * @return A number of milliseconds since java epoch, giving the date
     * at which the server started.
     */

    public long getStartTime() {
	return start_time;
    }

    /**
     * As we can't start the after the server socket (client factory)
     * we have to create when requested, which is always after the creation
     * of the whole server..
     */
    private void initFactoryStats() {
	if (server.factory instanceof SocketClientFactory) {
	    SocketClientFactory f = (SocketClientFactory) server.factory;
	    factoryStats = new SocketClientFactoryStats(f);
	}
	init = true;
    }

    httpdStatistics (httpd server) {
	this.server     = server ;
	this.start_time = System.currentTimeMillis();
    }

}
