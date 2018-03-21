// PageCompileProp.java
// $Id: PageCompileProp.java,v 1.2 2010/06/15 17:53:14 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.pagecompile;

import java.io.File;

import org.w3c.jigsaw.config.PropertySet;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FileAttribute;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.jigsaw.http.httpd;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class PageCompileProp extends PropertySet {

    /**
     * Our property name.
     */
    protected static String PAGE_COMPILE_PROP_NAME = "PageCompileProps";

    /**
     * Name of the property indicating the generated class directory.
     */
    protected static String PAGE_COMPILED_DIR =
	"org.w3c.jigsaw.pagecompile.dir";

    /**
     * Name of the property indicating the compiler class name
     */
    protected static String PAGE_COMPILER_CLASS =
	"org.w3c.jigsaw.pagecompile.compiler";

    /**
     * Attribute index - The index for our generated class directory.
     */
    protected static int ATTR_PAGE_COMPILED_DIR = -1 ;

    /**
     * Attribute index - The index for our compiler class name.
     */
    protected static int ATTR_PAGE_COMPILER_CLASS = -1;

    static {
	Class cls = null;
	Attribute a = null;

	try {
	    cls = Class.forName("org.w3c.jigsaw.pagecompile.PageCompileProp");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// The generated class directory:
	a = new FileAttribute(PAGE_COMPILED_DIR,
			      null,
			      Attribute.EDITABLE);
	ATTR_PAGE_COMPILED_DIR = AttributeRegistry.registerAttribute(cls, a);
	//The compiler class name:
	a = new StringAttribute(PAGE_COMPILER_CLASS,
				"org.w3c.jigsaw.pagecompile.JDKCompiler",
				Attribute.EDITABLE);
	ATTR_PAGE_COMPILER_CLASS = AttributeRegistry.registerAttribute(cls, a);
    }

    protected File getDefaultCompiledPageDirectory() {
	File root = server.getRootDirectory();
	File def  = new File(root, "compiledPage");
	if (! def.exists())
	    def.mkdir();
	return def;
    }

    protected File getCompiledPageDirectory() {
	File dir = (File)getValue(ATTR_PAGE_COMPILED_DIR, null);
	if (dir == null) {
	    dir = getDefaultCompiledPageDirectory();
	    setValue(ATTR_PAGE_COMPILED_DIR, dir);
	}
	return dir;
    }

    protected String getCompilerClassName() {
	return (String)getValue(ATTR_PAGE_COMPILER_CLASS, null);
    }

    PageCompileProp(httpd server) {
	super(PAGE_COMPILE_PROP_NAME, server);
    }
}
