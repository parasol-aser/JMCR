// AutoReloadServletLoader.java
// $Id: AutoReloadServletLoader.java,v 1.1 2010/06/15 12:24:14 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.servlet;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.Hashtable;

class ServletClassEntry {

    long   classStamp   = 0;
    Class  servletClass = null;
    File   classFile    = null;
    boolean systemClass = false;

    public boolean isModified() {
	if (! systemClass)
	    return (classFile.lastModified() > classStamp);
	return false;
    }

    public void update() {
	if (! systemClass)
	    classStamp   = classFile.lastModified();
    }

    public ServletClassEntry(Class servletClass) {
	this.servletClass = servletClass;
	this.systemClass  = true;
    }

    public ServletClassEntry (File classFile, 
			      Class servletClass)
    {
	this.classFile    = classFile;
	this.servletClass = servletClass;
	if (classFile != null)
	    this.classStamp   = classFile.lastModified();
	this.systemClass  =  false;
    }

}

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class AutoReloadServletLoader extends ClassLoader {
    private static final boolean debug = false;
    private Hashtable classes = null;
    private int CLASSES_DEFAULT_SIZE = 13;

    JigsawServletContext context = null;

    private void trace(String msg) {
	trace(msg,true);
    }

    private void trace(String msg, boolean display) {
	if (display)
	    System.out.println("["+context.getServletDirectory()+"]: "+msg);
    }

    private String removeSpace(String string) {
	int start = -1;
	int end = string.length();
	boolean spaces = false;
	while (string.charAt(++start) == ' '){ spaces = true; };
	while (string.charAt(--end) == ' '){ spaces = true; };
	if (spaces)
	    return string.substring(start, ++end);
	else
	    return new String(string);
    }

    private String packaged(String name) {
	String cname = removeSpace(name);
	try {
	    if (cname.endsWith(".class")) {
		int idx = cname.lastIndexOf('.');
		if (idx != -1)
		    cname = cname.substring(0,idx);
	    }
	    return cname.replace('.', '/')+".class";
	} catch (Exception ex) {
	    return name;
	}
    }

    private String noext(String name) {
	int idx = name.lastIndexOf(".class");
	if ((idx != -1) && 
	    ((idx+6 == name.length()) || 
	     (Character.isWhitespace(name.charAt(idx+6)))))
	{
	    return name.substring(0,idx);
	}
	return name;
    }

    protected boolean classChanged(String name) {
	ServletClassEntry entry = (ServletClassEntry)classes.get(noext(name));
	if (entry != null) {
	    if (debug) {
		System.out.println("entry    : "+name);
		if (!entry.systemClass) {
		    System.out.println("file     : "+
				       entry.classFile.getAbsolutePath());
		    System.out.println("Stamp    : "+entry.classStamp);
		}
		System.out.println("System   : "+entry.systemClass);
		System.out.println("modified : "+entry.isModified());
	    }
	    return entry.isModified();
	}
	return false; //true
    }

    /**
     * Get a cached class.
     * @return a Class instance
     * @exception ClassNotFoundException if the Class can't be found
     */
    protected final Class getCachedClass(String name, boolean resolve) 
	throws ClassNotFoundException
    {
	ServletClassEntry entry = (ServletClassEntry)classes.get(noext(name));
	if (entry != null) {
	    if (! entry.isModified()) {
		trace(name+": not modified",debug);
		return entry.servletClass;
	    } else if (! entry.systemClass) {
		entry.servletClass =  loadClassFile(entry.classFile);
		if ( resolve )
		    resolveClass(entry.servletClass);
		entry.update();
		trace(name+": reloaded",debug);
		return entry.servletClass;
	    }
	}
	return null;
    }

    protected void checkPackageAccess(String name) {
	SecurityManager s = System.getSecurityManager();
	if ( s != null ) {
	    int i = name.lastIndexOf('.');
	    if ( i >= 0 )
		s.checkPackageAccess(name.substring(0, i));
	}
    }

    /**
     * Given the class name, return its File name.
     * @param name The class to be loaded.
     * @return The File for the class.
     */
    protected File locateClass(String name) {
	File classfile = null;
	File base = context.getServletDirectory();
	String cname = null;

	cname = packaged(name);
	classfile = new File(base, cname);
	if (classfile != null)
	    if (classfile.exists()) return classfile;

	return null;
    }

    /**
     * Load a Class from its class file..
     * @return a Class instance
     * @exception ClassNotFoundException if the Class can't be found
     */
    protected Class loadClassFile(File file) 
	throws ClassNotFoundException
    {
	byte data[] = null;
	if ( file == null )
	    throw new ClassNotFoundException("invalid servlet base");
	trace("located at "+file,debug);
	try {
	    BufferedInputStream in      = 
		new BufferedInputStream( new FileInputStream( file) );
	    ByteArrayOutputStream out   = new ByteArrayOutputStream(512);
	    byte                  buf[] = new byte[512];
	    for (int got = 0 ; (got = in.read(buf)) > 0 ; )
		out.write(buf, 0, got);
	    data = out.toByteArray();
	    in.close();
	} catch (Exception ex) {
	    if (debug) 
		ex.printStackTrace();
	    throw new ClassNotFoundException(file.getAbsolutePath());
	}
	// Define the class:
	return defineClass(null, data, 0, data.length);
    }

    /**
     * Get a new class.
     * @return a Class instance
     * @exception ClassNotFoundException if the Class can't be found
     */
    protected final Class getNewClass(File classfile, 
				      String name, 
				      boolean resolve) 
	throws ClassNotFoundException
    {
	Class c = null;
	c       = loadClassFile(classfile);
	if ( resolve )
	    resolveClass(c);
	classes.put(noext(name), new ServletClassEntry(classfile,c));
	trace(name+": loading new class done.",debug);
	return c;
    }

    /**
     * Load a class.
     * @return a Class instance
     * @exception ClassNotFoundException if the Class can't be found
     */
    protected Class loadClass(String name, boolean resolve)
	throws ClassNotFoundException
    {
	Class c = null;
	trace(name+": loading class",debug);
	// Look for a cached class first:
	c = getCachedClass(name,resolve);
	if (c != null) {
	    return c;
	}

	synchronized (this) {
	    c = getCachedClass(name,resolve);
	    if (c != null) return c;

	    checkPackageAccess(name);
	    
	    File  file    = locateClass(name);
	    
	    if (file == null) {
		// Then look for a system class:
		try {
		    String noext = noext(name);
		    if ((c = findSystemClass(noext)) != null) {
			trace(name+": system class",debug);
			classes.put(noext, new ServletClassEntry(c));
			return c;
		    } else 
			throw new ClassNotFoundException(name);
		} catch (Exception ex) {
		    throw new ClassNotFoundException(name);
		}
	    }
	    // load the class
	    return getNewClass(file,name,resolve);
	}
    }


    public URL getResource(String name) {
	URL resource = getSystemResource(name);
	if (resource != null)
	    return resource;
	String url = context.getServletDirectory().getAbsolutePath();
	// Return the location of that resource (name resolved by j.l.Class)
	if ( File.separatorChar != '/' )
	    url = url.replace(File.separatorChar, '/');
	File file = new File(url,name);
	if (! file.exists())
	    return null;
	try {
	    return new URL("file", "localhost", url+"/"+name);
	} catch (MalformedURLException ex) {
	    // Shouldn't fall here
	    return null;
	}
    }

    /**
     * Get a resource as a stream.
     * @param name The name of the resource to locate.
     */

    public InputStream getResourceAsStream(String name) {
	InputStream in = getSystemResourceAsStream(name);
	if (in != null) 
	    return in;
	URL resource = getResource(name);
	if (resource == null)
	    return null;
	try {
	    URLConnection c = resource.openConnection();
	    return c.getInputStream();
	} catch (IOException ex) {
	    return null;
	}
    }

    protected AutoReloadServletLoader(AutoReloadServletLoader loader) {
	super();
	this.context = loader.context;
	this.classes = loader.classes;
    }

    protected AutoReloadServletLoader(JigsawServletContext context) {
	super();
	this.context = context;
	this.classes = new Hashtable(CLASSES_DEFAULT_SIZE);
    }
}
