/*
 *  SimpleReadtable.java
 *
 *  Copyright 1997 Massachusetts Institute of Technology.
 *  All Rights Reserved.
 *
 *  Author: Ora Lassila
 *
 *  $Id: SimpleReadtable.java,v 1.1 2010/06/15 12:27:50 smhuang Exp $
 */

package org.w3c.tools.sexpr;

/**
 * Basic implementation of the Readtable interface, a dispatch table.
 */
public class SimpleReadtable implements Readtable {

  private SExprParser parsers[];

  /**
   * Initializes an empty dispatch table (no associations).
   */
  public SimpleReadtable()
  {
    this.parsers = new SExprParser[256];
  }

  /**
   * Copy constructor.
   */
  public SimpleReadtable(SimpleReadtable table)
  {
    this.parsers = new SExprParser[256];
    for (int i = 0; i < 256; i++)
      this.parsers[i] = table.parsers[i];
  }

  public SExprParser getParser(char key)
  {
    return parsers[(int)key];
  }

  public SExprParser addParser(char key, SExprParser parser)
  {
    parsers[(int)key] = parser;
    return parser;
  }

}
