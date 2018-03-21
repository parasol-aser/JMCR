// ConfigResourceIndexer.java
// $Id: ConfigResourceIndexer.java,v 1.2 2010/06/15 17:53:12 smhuang Exp $
// (c) COPYRIGHT MIT,INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.indexer;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

import java.util.Enumeration;
import java.util.Hashtable;

import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ContainerResource;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.indexer.SampleResourceIndexer;
import org.w3c.www.mime.MimeParser;
import org.w3c.www.http.MimeParserMessageFactory;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HeaderDescription;
import org.w3c.jigsaw.frames.HTTPFrame;
import org.w3c.www.mime.MimeType;

/**
 * This indexer allow to add a configuration file
 * to overwrite the computed configuration
 * for now, ./foo.html will have its configuration file in
 * ./.meta/foo.html.meta
 * but it can be extended to match other filename.
 */
public class ConfigResourceIndexer extends SampleResourceIndexer {

    /**
     * compute and return the file containing the configuration
     */
    private File getConfigFile(File directory, String name) {
	File configdir = new File(directory, ".meta");
	if (configdir.exists() && configdir.isDirectory()) {
	    File config = new File(configdir, name + ".meta");
	    if (config.exists() && !config.isDirectory())
		return config;
	}
	return null;
    }
		

    /**
     * Try to create a resource for the given file.
     * This method makes its best efforts to try to build a default
     * resource out of a file. 
     * @param directory The directory the file is in.
     * @param name The name of the file.
     * @param defs Any default attribute values that should be provided
     *    to the created resource at initialization time.
     * @return A Resource instance, or <strong>null</strong> if the given
     *    file can't be truned into a resource given our configuration
     *    database.
     */
    public Resource createResource(ContainerResource container,
				   RequestInterface request,
				   File directory,
				   String name,
				   Hashtable defs) 
    {
	Resource r = super.createResource(container, request, directory, 
					  name, defs);
	Class proto = null;
	try {
	    proto = Class.forName("org.w3c.jigsaw.frames.HTTPFrame");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    // fatal error!
	    return r;
	}
	if (r == null)
	    return r;
	HTTPFrame frame = null;
	ResourceFrame rf[] = r.collectFrames(proto);
	if (rf != null) {
	    frame = (HTTPFrame) rf[0];
	}
	if (frame == null) {
	    return r;
	}

	File config = getConfigFile(directory, name);
	if (config != null) {
	    // we found a configuration file, do some more processing!
	    try {
		// first, extract and parse the config file
		BufferedInputStream buf;
		buf = new BufferedInputStream(new FileInputStream(config));
		MimeParser p = new MimeParser(buf,
					      new MimeParserMessageFactory());
		HttpEntityMessage msg = (HttpEntityMessage) p.parse();
		Enumeration e = msg.enumerateHeaderDescriptions();
		// then override some configuration
		while ( e.hasMoreElements() ) {
		    // use some well known descriptions
		    HeaderDescription d = (HeaderDescription) e.nextElement();
		    if ( d.isHeader(HttpEntityMessage.H_CONTENT_TYPE)) {
			MimeType mtype = null;
			try {
			    mtype = msg.getContentType();
			} catch (Exception ex) {
			    // ok by default use something binary
			    mtype = MimeType.APPLICATION_OCTET_STREAM;
			}
			if (mtype.hasParameter("charset")) {
			    String charset =mtype.getParameterValue("charset");
			    MimeType m = new MimeType(mtype.getType(),
						      mtype.getSubtype());
			    frame.setValue("content-type", m);
			    frame.setValue("charset", charset);
			} else {
			    frame.setValue("content-type", mtype);
			}
			continue;
		    }
		    if ( d.isHeader(HttpEntityMessage.H_CONTENT_LANGUAGE)) {
			String lang[] = msg.getContentLanguage();
			if (lang.length > 0 ) {
			    frame.setValue("content-language", lang[0]);
			}
			continue;
		    }
		    if ( d.isHeader(HttpEntityMessage.H_CONTENT_ENCODING)) {
			String enc[] = msg.getContentEncoding();
			if (enc.length > 0 ) {
			    frame.setValue("content-encoding", enc[0]);
			}
			continue;
		    }
		}
	    } catch (Exception ex) {
		// do nothing, keep configured as it was
		// by the super indexer
		ex.printStackTrace();
	    }
	}
	return r;
    }
}
