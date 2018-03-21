// ServletIndexer.java
// $Id: ServletIndexer.java,v 1.1 2010/06/15 12:24:11 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.servlet;

import java.io.File;

import java.util.Enumeration;
import java.util.Hashtable;



import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceReference;

import org.w3c.tools.resources.indexer.SampleResourceIndexer;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ServletIndexer extends SampleResourceIndexer {

    /**
     * Copy one hastable in another one.
     * @param fromdefs The source
     * @param todefs The destination
     */
    protected void copyDefs(Hashtable fromdefs, Hashtable toDefs) {
	Enumeration keys = fromdefs.keys();
	while(keys.hasMoreElements()) {
	    Object key = keys.nextElement();
	    toDefs.put(keys, fromdefs.get(key));
	}
    }

    /**
     * Create a default file resource for this file (that exists).
     * @param directory The directory of the file.
     * @param name The name of the file.
     * @param defs A set of default attribute values.
     * @return An instance of Resource, or <strong>null</strong> if
     *    we were unable to create it.
     */

    protected Resource createFileResource(File directory,
					  RequestInterface req,
					  String name,
					  Hashtable defs) 
    {
	if (! name.endsWith(".class"))
	    return super.createFileResource(directory, req, name, defs);
	ResourceReference rr = null;
	FramedResource template = null;
	
	// Check that at least one class is defined for all the extensions:
	String exts[] = getFileExtensions(name) ;
	if ( exts == null )
	    return null ;
	for (int i = exts.length-1 ; i >= 0 ; i--) {
	    rr = getTemplateFor(exts[i]) ;
	    if ( rr != null )
		break ;
	}
	if ( rr == null ) {
	    // Look for a default template:
	    if ((rr = loadExtension(defname)) == null)
		return null ;
	    return super.createFileResource(directory, req, name, defs);
	} else {
	    //this could become a servlet
	    Hashtable tempdefs = null;
	    String s_dir = "directory".intern();
	    String s_ide = "identifier".intern();
	    String s_ser = "servlet-class".intern();
	    String s_con = "context".intern(); 
	    String s_url = "url".intern(); 
	    if (defs != null) {
		tempdefs = (Hashtable) defs.clone();
	    } else {
		tempdefs = new Hashtable(5) ;
	    }
	    if ( tempdefs.get(s_dir) == null )
		tempdefs.put(s_dir, directory) ;
	    if ( tempdefs.get(s_con) == null )
		tempdefs.put(s_con, getContext());
	    try {
		template = (FramedResource) rr.lock();
		if (template instanceof ServletWrapper) {
		    if (tempdefs.get(s_ser) == null)
			tempdefs.put(s_ser, name);
		    String id = getIndexedFileName(name);
		    tempdefs.put(s_ide, id) ;
		    String url = (String) tempdefs.get(s_url);
		    if ((url != null) && (url.endsWith(".class"))) {
			int idx = url.lastIndexOf(".class");
			tempdefs.put(s_url, url.substring(0, idx));
		    }
		} else {
		    if ( tempdefs.get(s_ide) == null )
			tempdefs.put(s_ide, name);
		}
		if (exts != null) {
		    // Merge with values defined by the extension:
		    for (int i = exts.length ; --i >= 0 ; ) 
			mergeDefaultAttributes(template, exts[i], tempdefs) ;
		}
		// Create, initialize and return the new resource
		try {
		    FramedResource cloned = 
			(FramedResource) template.getClone(tempdefs);
		    if (cloned instanceof ServletWrapper) {
			ServletWrapper wrapper = (ServletWrapper) cloned;
			// check the servlet class
			if (! wrapper.isWrappingAServlet())
			    return null;
		    }
		    //ok, the defs are good.
		    copyDefs(tempdefs, defs);
		    return cloned;
		} catch (Exception ex) {
		    ex.printStackTrace() ;
		    return null ;
		}
	    } catch (InvalidResourceException ex) {
		ex.printStackTrace();
		return null;
	    } finally {
		rr.unlock();
	    }
	} 
    }

    /**
     * Try to create a virtual resource if the real (physical) resource
     * is not there.
     * @param directory The directory the file is in.
     * @param name The name of the file.
     * @param defs Any default attribute values that should be provided
     *    to the created resource at initialization time.
     * @return A Resource instance, or <strong>null</strong> if the given
     *    file can't be truned into a resource given our configuration
     *    database.
     */

    protected Resource createVirtualResource( File directory,
					      RequestInterface req,
					      String name,
					      Hashtable defs) 
    {
	Resource res = super.createVirtualResource(directory, req, name, defs);
	if (res != null)
	    return res;
	//could be a servlet
	char fileSeparatorChar = File.separatorChar;
	String sname = name.replace('.', fileSeparatorChar)+".class";
	File servletfile = new File(directory, sname);
	if (servletfile.exists())
	    return createFileResource(directory, null, name+".class", defs);
	else
	    return null;
    }

    protected String getIndexedFileName(String name) {
	String indexed = name;
	int idx = name.lastIndexOf(".class");
	if (idx != -1)
	    indexed = name.substring(0,idx);
	return indexed;
    }
}
