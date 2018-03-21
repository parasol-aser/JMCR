// JDKCompiler.java
// $Id: JDKCompiler.java,v 1.1 2010/06/15 12:29:29 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.pagecompile;

import java.io.OutputStream;
import java.io.PrintStream;

import sun.tools.javac.Main;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class JDKCompiler implements PageCompiler {

    /**
     * compile some files.
     * @param args The compiler arguments (files+options)
     * @param out The outputStream, the compiler will write its output
     * in it.
     * @return false if compilation failed.
     */
    public boolean compile(String args[], OutputStream out) {
	if (out == null)
	    out = System.out;
	int len = args.length;
	String newargs[] = new String[len+2];
	System.arraycopy(args, 0, newargs, 0, len);
	newargs[len] = "-classpath";
	newargs[len+1] = System.getProperty("java.class.path");
	return (new Main(out, "compiler")).compile(newargs);
    }

    //testing only
    public static void main(String args[]) {
	(new JDKCompiler()).compile(args, null);
    }

}
