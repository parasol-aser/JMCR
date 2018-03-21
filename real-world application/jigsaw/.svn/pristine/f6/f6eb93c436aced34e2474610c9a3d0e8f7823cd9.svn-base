// PushCacheEntityResource.java
// $Id: PushEntityCachedResource.java,v 1.1 2010/06/15 12:25:45 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2001.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.cache.push;

import org.w3c.www.protocol.http.Request;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.w3c.www.protocol.http.cache.CachedResource;
import org.w3c.www.protocol.http.cache.EntityCachedResource;

/**
 * PushEntityCachedResource
 * EntityCachedResource that reads data from the file rather than attempting
 * to use ActiveStream to tee output to a client that is not there.
 *
 * @author Paul Henshaw, The Fantastic Corporation, Paul.Henshaw@fantastic.com
 * @version $Revision: 1.1 $
 * $Id: PushEntityCachedResource.java,v 1.1 2010/06/15 12:25:45 smhuang Exp $
 */
public class PushEntityCachedResource extends EntityCachedResource {

    /**
     * This constructor required to handle startup when cache already
     * contains PushEntityCachedResources
     */
    public PushEntityCachedResource() {
	super();
    }

    /**
     * Construct a PushEntityCachedResource 
     *
     * Used by the PushCacheManager to actually store a PUSH resource.
     * Note PushEntityCachedResource are used only when saving 
     * resources.  When extracting resources from the cache, 
     * EntityCachedResources are used.
     * 
     * @param filter  the PushCacheFilter that in fact has not done 
     *                anything yet, but which knows how to handle a 
     *                PUSHed resource
     * @param req     the forged request for a URL
     * @param rep     the forged reply for the URL
     */
    protected PushEntityCachedResource(PushCacheFilter filter, Request req, 
				    PushReply rep) {
	try {
	    invalidated = false;
	    setValue(ATTR_IDENTIFIER, req.getURL().toExternalForm());

	    // Keep fast track of the filter:
	    this.filter = filter;

	    // update the headers
	    updateInfo(req, rep);

	    // and do some calculation according to the validator
	    filter.getValidator().updateExpirationInfo(this, req, rep);

	    // Save the content of resource into the content cache:
	    java.io.File outfile=filter.getPushCacheStore().getNewEntryFile();

	    FileOutputStream os=new FileOutputStream(outfile);
	    FileInputStream is=rep.getStream();

	    byte[] buffer=new byte[4096];
	    int wrote=0;
	    int wantedsize=rep.getContentLength();
	    int thistime;
	    while(wrote<wantedsize) {
		thistime=Math.min(is.available(), 4096);
		is.read(buffer,0,thistime);
		os.write(buffer,0,thistime);
		wrote+=thistime;
	    }

	    os.close();
	    setFile(outfile);
	    setLoadState(STATE_LOAD_COMPLETE);
	    setCurrentLength(wrote);
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
    }
}
