/*
 *  SExprParser.java
 *
 *  Copyright 1997 Massachusetts Institute of Technology.
 *  All Rights Reserved.
 *
 *  Author: Ora Lassila
 *
 *  $Id: SExprParser.java,v 1.1 2010/06/15 12:27:50 smhuang Exp $
 */

package org.w3c.tools.sexpr;

import java.io.IOException;

/**
 * An interface for all dispatched "sub-parsers."
 */
public interface SExprParser {

  /**
   * Dispatched on character <i>first</i>, parse an object from the stream.
   *
   * @exception SExprParserException on syntax error
   * @exception IOException on I/O related problems (e.g. end of file)
   */
  public Object parse(char first, SExprStream stream)
    throws SExprParserException, IOException;

}
