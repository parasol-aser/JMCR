/*
 *  Readtable.java
 *
 *  Copyright 1997 Massachusetts Institute of Technology.
 *  All Rights Reserved.
 *
 *  Author: Ora Lassila
 *
 *  $Id: Readtable.java,v 1.1 2010/06/15 12:27:50 smhuang Exp $
 */

package org.w3c.tools.sexpr;

/**
 * An interface for read tables.
 */
public interface Readtable {

  /**
   * Find the parser associated with the <i>key</i> dispatch character.
   */
  public SExprParser getParser(char key);

  /**
   * Associate a parser with the <i>key</i> dispatch character.
   */
  public SExprParser addParser(char key, SExprParser parser);

}
