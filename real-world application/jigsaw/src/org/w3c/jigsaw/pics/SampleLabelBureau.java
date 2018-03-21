// SampleLabelBureau.java
// $Id: SampleLabelBureau.java,v 1.1 2010/06/15 12:25:29 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.pics ;

import java.io.File;

import java.util.Hashtable;

/**
 * This class implements a label bureau.
 * The database for labels is provided by the underlying file system. The 
 * organisation is as follows:
 * <p>The LabelBureau is attached to some directory <strong>D</strong>. For 
 * each service it handles, you have to create a subdirectory, according
 * to the service name (its identifying URL). So for example, if you want to
 * provide ratings as <strong>www.rating.com</strong>, you will have to create
 * a <strong>D/www.rating.com</strong> directory.
 * <p>Uner this <em>service</em> directory, for each site you want to label,
 * you have to create a sub-directory. In our example, if our rating service
 * wants to label www.w3.org, you will have to create a 
 * <strong>D/www.rating.com/www.w3.org</strong> directory. This directory
 * should reflect the space of the labeled server (ie having the same file 
 * hierarchy), and each file should be the label  itself (as transmited).
 * <p>In our example, if the LabelBureau wants to find the label by 
 * www.rating.com for http://www.w3.org/pub/WWW/Overview.html, it will look
 * for the file
 * <strong>D/www.rating.com/http/www.w3.org/pub/WWW/Overview.html-label
 * </strong>.
 * <p>So, we really use the underlying file system as a database for labels.
 * <p>FIXME: the LabelBureau should be an interface, same stands for the
 * LabelService and Label classes.
 */

public class SampleLabelBureau implements LabelBureauInterface {
    File      directory = null ;
    Hashtable services  = null ;

    /**
     * Get this label bureau directory.
     */

    public String getIdentifier () {
	return directory.getAbsolutePath() ;
    }

    /**
     * Lookup for the given service in this bureau.
     * @param name The service name.
     * @return A LabelService instance, or <strong>null</strong> if none
     *    was found.
     */

    public LabelServiceInterface getLabelService (String url) {
      // le service est deja en memoire
	LabelServiceInterface s = (LabelServiceInterface)services.get(url) ;
	if ( s == null ) { // il faut recuperer le service
	    try {
		s = new SampleLabelService (this, url) ;
	    } catch (UnknownServiceException e) {
		return null ;
	    }
	    services.put (url, s) ;
	}
	return s ;
    }

    /**
     * Create a new LabelBureau.
     * The configuration files from the label bureau (the place were it takes
     * its labels from), is given by the provided directory.
     * @param directory This bureau root directory.
     * @see org.w3c.jigsaw.pics.LabelServiceInterface
     */

    public SampleLabelBureau (File directory) {
	this.directory = directory ;
	this.services  = new Hashtable () ;
    }

}


