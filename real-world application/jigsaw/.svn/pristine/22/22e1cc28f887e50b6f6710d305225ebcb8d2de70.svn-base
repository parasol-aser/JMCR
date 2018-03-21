// InvalidLabelFileException.java
// $Id: InvalidLabelFileException.java,v 1.1 2010/06/15 12:25:27 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.pics ;

import java.io.File ;

public class InvalidLabelFileException extends InvalidLabelException {

    public InvalidLabelFileException (String msg) {
        super (msg) ;
    }

    public InvalidLabelFileException (File file, int lineno, String msg) {
        this (file.getAbsolutePath()
              + "[" + lineno + "]"
              + ": " + msg) ;
    }
}
