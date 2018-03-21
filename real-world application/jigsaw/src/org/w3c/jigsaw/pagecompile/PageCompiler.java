// Compiler.java
// $Id: PageCompiler.java,v 1.1 2010/06/15 12:29:29 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.pagecompile;

import java.io.OutputStream;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public interface PageCompiler {

    /**
     * compile some files.
     * @param args The compiler arguments (files+options)
     * @param out The outputStream, the compiler will write its output
     * in it.
     * @return false if compilation failed.
     */
    public boolean compile(String args[], OutputStream out);

}
