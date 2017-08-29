// CacheProp.java
// $Id: CacheProp.java,v 1.2 2010/06/15 17:53:07 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.proxy;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.DoubleAttribute;
import org.w3c.tools.resources.FileAttribute;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.LongAttribute;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.jigsaw.http.httpd;

import org.w3c.jigsaw.config.PropertySet;

import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.cache.CacheFilter;
import org.w3c.www.protocol.http.cache.CacheStore;

class CacheProp extends PropertySet {
    private static String title = "Cache properties";

    static {
	Class     c = null;
	Attribute a = null;

	try {
	    c = Class.forName("org.w3c.jigsaw.proxy.CacheProp");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// The Cache directory
	a = new FileAttribute(CacheStore.CACHE_DIRECTORY_P,
			      null,
			      Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// The cache size in bytes:
	a = new LongAttribute(CacheFilter.CACHE_SIZE_P
			      , new Long(20971520)
			      , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// The cache store size (the maximum disc used by the resources,
	// not the database or the headers
	a = new LongAttribute(CacheStore.STORE_SIZE_P
			      , new Long(23068672)
			      , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// The debug flag:
	a = new BooleanAttribute(CacheFilter.DEBUG_P
				 , Boolean.FALSE
				 , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// The is-shared flag:
	a = new BooleanAttribute(CacheFilter.SHARED_P
				 , Boolean.TRUE
				 , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// Is the cache to be connected ?
	a = new BooleanAttribute(CacheFilter.CACHE_CONNECTED_P
				 , Boolean.TRUE
				 , Attribute.EDITABLE);
       AttributeRegistry.registerAttribute(c, a);
       // Are garbage collections allowed ?
       a = new BooleanAttribute(CacheStore.GARBAGE_COLLECTION_ENABLED_P
				, Boolean.TRUE
				, Attribute.EDITABLE);
       AttributeRegistry.registerAttribute(c, a);
       // The allowed file size ratio:
       a = new DoubleAttribute(CacheStore.FILE_SIZE_RATIO_P
			       , new Double((double) 0.1)
			       , Attribute.EDITABLE);
       AttributeRegistry.registerAttribute(c, a);
       // the gabrage collection ration, the % of the cache to be kept
       // after a gc
       a = new DoubleAttribute(CacheStore.GARBAGE_COLLECTION_THRESHOLD_P
			       , new Double((double) 0.8)
			       , Attribute.EDITABLE);
       AttributeRegistry.registerAttribute(c, a);
       // the delay between two synchronization of the store
       a = new LongAttribute(CacheStore.SYNCHRONIZATION_DELAY_P
			     , new Long(60000)
			     , Attribute.EDITABLE);
       AttributeRegistry.registerAttribute(c, a);
       // the delay between two attempts to compact generations
       a = new LongAttribute(CacheStore.GENERATION_COMPACT_DELAY_P
			     , new Long(60000)
			     , Attribute.EDITABLE);
       AttributeRegistry.registerAttribute(c, a);
       // the maximum unmber of cached resources in the store
       a = new IntegerAttribute(CacheStore.MAX_CACHED_RESOURCES_P
				, new Integer(50000)
				, Attribute.EDITABLE);
       AttributeRegistry.registerAttribute(c, a);
       // the maximum unmber of generations
       a = new IntegerAttribute(CacheStore.MAX_GENERATIONS_P
			       , new Integer(10)
			       , Attribute.EDITABLE);
       AttributeRegistry.registerAttribute(c, a);
       // the Class of the validator
       a = new StringAttribute(CacheFilter.VALIDATOR_P
			       , null
			       , Attribute.EDITABLE);
       AttributeRegistry.registerAttribute(c, a);
       // the Class of the Sweeper
       a = new StringAttribute(CacheFilter.SWEEPER_P
			       , null
			       , Attribute.EDITABLE);
       AttributeRegistry.registerAttribute(c, a);
       // the Class of the Serializer
       a = new StringAttribute(CacheFilter.SERIALIZER_P
			       , null
			       , Attribute.EDITABLE);
       AttributeRegistry.registerAttribute(c, a);
    }
			       
    /**
     * Get this property set title.
     * @return A String encoded title.
     */

    public String getTitle() {
	return title;
    }

    CacheProp(String name, httpd server) {
	super(name, server);
    }
}
