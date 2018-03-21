/*
 *  SExprParserException.java
 *
 *  Copyright 1997 Massachusetts Institute of Technology.
 *  All Rights Reserved.
 *
 *  Author: Ora Lassila
 *
 *  $Id: SExprParserException.java,v 1.1 2010/06/15 12:27:49 smhuang Exp $
 */

package org.w3c.tools.sexpr;

/**
 * An exception class for syntax errors detected during s-expression parsing.
 */
public class SExprParserException extends Exception {

  /**
   * Initialize the exception with an explanatory message.
   */
  public SExprParserException(String explanation)
  {
    super(explanation);
  }

  /**
   * Initialize the exception with a message about an illegal character.
   */
  public SExprParserException(char illegalChar)
  {
    super("Invalid character '" + illegalChar + "'");
  }

}
