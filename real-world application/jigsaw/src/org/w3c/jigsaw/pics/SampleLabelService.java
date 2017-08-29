// SampleLabelService.java
// $Id: SampleLabelService.java,v 1.1 2010/06/15 12:25:30 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.pics ;

import java.io.File;
import java.io.PrintStream;

import java.net.URL ;
import java.net.MalformedURLException ;
import java.util.Vector ;

/**
 * The internal representation of a LabelService.
 * A LabelService is an object which should be able to deliver labels for any
 * URL. This implementation doesn't use any fancy database (it should), it uses
 * the file system as a Database, in fact.
 * <p>Each service is assigned a directory, and for each requested labels, this
 * directory is looked up for the appropriate URL. So if you want to label
 * <strong>http://www.w3.org/pub/WWW</strong> you have to create, under
 * this service directory a file named 
 * <strong>http/www.w3.org/pub/WWW/label</strong>. To label its Overview.html 
 * document define the 
 * <strong>http/www.w3.org/pub/WWW/Overview.html-label</strong> file. 
 * The label itself is the content of the file.
 */

public class SampleLabelService implements LabelServiceInterface {
    File   directory = null ;		 // This service directory
    String name      = null ;		 // This service name (URL)

    /**
     * Get his service directory.
     */

    private final File getDirectory() {
	return directory ;
    }

    /**
     * Slashify a path component.
     * Turn the slashes in the given URL into appropriate file name separator
     * from the underlying OS.
     * @param path The path to slashify.
     * @return A String properly slashified.
     */

    private String slashify (String path) {
	String separator = System.getProperty ("file.separator") ;
	if ( separator.equals ("/") ) 
	    return path ;
	if ( separator.length() != 1 ) 
	    throw new RuntimeException (this.getClass().getName()
					+ " invalid separator length !") ;
	return path.replace ('/', '\\') ;
    }

    /**
     * Filify an URL.
     * This methods takes an URL as input, and returns an uniq File object
     * relative to the given SampleLabelService directory.
     * @param u The URL to filify.
     * @param generic Filify for generic labels if <strong>true</strong>.
     */

    public File filify (URL u, boolean generic) {
	File file = null ;
	file = new File (getDirectory(), u.getProtocol()) ;
	file = new File (file, u.getHost()) ;
	if ( (u.getPort() != 80) && (u.getPort() != -1) ) 
	    file = new File (file, new Integer(u.getPort()).toString()) ;
	if ( (u.getFile() != null) && ( ! u.getFile().equals("/")) ) {
	    String part = (generic 
			   ? u.getFile().substring(1) + ".gen"
			   : u.getFile().substring(1)) ;
	    file = new File (file, slashify(part)) ;
	} else if (generic) {
	    file = new File(file.getParent(), file.getName()+".gen");
	}
	if (PICS.debug())
	    System.out.println("Label for "+u+" in ["+file+"]");
	return file ;
    }

    /**
     * Get this service name.
     * @return A String instance, being the service name.
     */

    public String getName() {
	return name ;
    }

    /**
     * Dump this service description into the given buffer.
     * @param into The StringBuffer to dump the service to.
     */

    public void dump (StringBuffer into, int format) {
	into.append (" \""+getName()+"\"") ;
    }

    /**
     * Get the specific label for the given URL.
     * @param url The URL whose label is searched.
     */

    public LabelInterface getSpecificLabel (URL url) {
	File flabel = filify (url, false) ;
	if ( flabel.exists () ) {
	    SampleLabel l = null ;
	    try {
		l = new SampleLabel (flabel) ;
	    } catch (InvalidLabelException e) {
		return null ;
	    }
	    return l ;
	}
	return null ;
    }

    /**
     * Get the most speicific generic label for an URL.
     * @param url The URL whose generic label is to be retreived.
     * @return An object conforming to the LabelInterface, or
     *    <strong>null</strong> if none was found.
     */

    // This is awfully slow, I need a real database for a real implementation
    public LabelInterface getGenericLabel (URL url) {
	try {
	    while ( true ) {
		File flabel = filify (url, true) ;
		if ( flabel.exists() ) {
		    SampleLabel l = null ;
		    try {
			l = new SampleLabel (flabel) ;
		    } catch (InvalidLabelException e) {
			return  null ;
		    }
		    return l ;
		}
		String file = url.getFile() ;
		if ( (file == null) || file.equals ("/") ) 
		    return null ;
		String parent  = url.getFile();
		if (parent.length()-1 == parent.lastIndexOf("/"))
		    parent = parent.substring(0,parent.length()-2);
		parent = parent.substring(0,parent.lastIndexOf("/"));
		if (parent.length() == 0)
		    parent = "/";
		url = new URL (url.getProtocol()
			       , url.getHost()
			       , url.getPort()
			       , parent) ;
	    }
	} catch (MalformedURLException e) {
	    e.printStackTrace() ;
	    return null ;
	}
    }

    /**
     * Get the tree labels for the given URL.
     * @param url The URL whose tree labels are to be retreived.
     * @return An array of SampleLabel, each comforming to the LabelInterface.
     */

    // This is awfully slow, I need a real database for a real implementation
    public LabelInterface[] getTreeLabels (URL url) {
	File labels = filify (url, false) ;
	if ( ! labels.isDirectory() )
	    return null ;
	String files[] = labels.list() ;
	if ( files == null )
	    return null ;
	// Include the label for the queried URL itself first:
	LabelInterface myself = getGenericLabel(url) ;
	Vector         v      = new Vector (files.length) ;
	if ( myself != null )
	    v.addElement(myself) ;
	// Get the other labels:
	for (int i = 0 ; i < files.length ; i++) {
	    SampleLabel l = null ;
	    try {
		l = new SampleLabel (new File (labels, files[i])) ;
	    } catch (InvalidLabelException e) {
		l = null ;
	    } 
	    if ( l != null )
		v.addElement (l) ;
	}
	if ( v.size() > 0 ) {
	    SampleLabel ls[] = new SampleLabel[v.size()] ;
	    v.copyInto (ls) ;
	    return ls ;
	}
	else {
	    return null ;
	}
    }

    /**
     * Get the generic tree labels for given URL.
     * @param url The URL whose tree labels are to be retreived.
     * @return An array of SampleLabel, each of which conforms to the 
     *    LabelInterface.
     */

    // This is awfully slow, I need a real database for a real implementation
    public LabelInterface[] getGenericTreeLabels (URL url) {
	File labels = filify (url, false) ;
	if ( ! labels.isDirectory() )
	    return null ;
	String files[] = labels.list() ;
	if ( files == null )
	    return null ;
	// Include the label for the queried URL itself first:
	LabelInterface myself = getGenericLabel(url) ;
	Vector         v      = new Vector (files.length) ;
	if ( myself != null )
	    v.addElement(myself) ;
	// Get the other labels.
    floop:
	for (int i = 0 ; i < files.length ; i++) {
	    if ( ! files[i].endsWith (".gen") )
		continue floop ;
	    SampleLabel l = null ;
	    try {
		l = new SampleLabel (new File (labels, files[i])) ;
	    } catch (InvalidLabelException e) {
		l = null ;
	    } 
	    if ( l != null )
		v.addElement (l) ;
	}
	if ( v.size() > 0 ) {
	    SampleLabel ls[] = new SampleLabel[v.size()] ;
	    v.copyInto (ls) ;
	    return ls ;
	}
	else {
	    return null ;
	}
    }

    public SampleLabelService (SampleLabelBureau b, String name) 
	throws UnknownServiceException 
    {
	this.name      = name ;
	try {
	    this.directory = new File (b.getIdentifier()) ;
	    this.directory = filify (new URL(name), false) ;
	} catch (MalformedURLException e) {
	    throw new UnknownServiceException (name) ;
	}
	if ( PICS.debug() )
	    System.out.println ("LabelService for " + name
				+ " is in " + directory.getAbsolutePath());
	if ((!this.directory.exists()) || (!this.directory.isDirectory()))
	    throw new UnknownServiceException (name) ;
    }

}


