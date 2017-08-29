/*
 *  Cons.java
 *
 *  Copyright 1997 Massachusetts Institute of Technology.
 *  All Rights Reserved.
 *
 *  Author: Ora Lassila
 *
 *  $Id: Cons.java,v 1.1 2010/06/15 12:27:51 smhuang Exp $
 */

package org.w3c.tools.sexpr;

import java.io.PrintStream;
import java.util.Enumeration;

/**
 * Basic class for implementing linked lists a la Lisp.
 */
public class Cons implements SExpr {

  private Object car;
  private Object cdr;

  /**
   * Initializes a Cons cell with the left and right "subtrees".
   */
  public Cons(Object left, Object right)
  {
    this.car = left;
    this.cdr = right;
  }

  /**
   * Initializes a Cons cell with a left subtree only.
   * Right subtree will be set to <tt>null</tt>.
   */
  public Cons(Object left)
  {
    this.car = left;
    this.cdr = null;
  }

  /**
   * Returns the left subtree (i.e. the head) of a cons cell.
   */
  public Object left()
  {
    return car;
  }

  /**
   * Returns the right subtree (i.e. the tail) of a cons cell.
   */
  public Object right()
  {
    return cdr;
  }

  /**
   * Returns the tail of a cons cell if it is a list.
   * Signals an error otherwise (no dotted pairs allowed).
   *
   * @exception SExprParserException if the tail is not a Cons or <tt>null<tt>
   */
  public Cons rest()
    throws SExprParserException
  {
    Object r = right();
    if (r == null)
      return null;
    else if (r instanceof Cons)
      return (Cons)r;
    else
      throw new SExprParserException("No dotted pairs allowed");
  }

  /*
   * Returns an enumeration of the elements of the list.
   */
  public Enumeration elements()
  {
    return new ConsEnumeration(this);
  }

  public void printExpr(PrintStream stream)
  {
    printList(stream, true);
  }

  private void printList(PrintStream out, boolean first)
  {
    out.print(first ? "(" : " ");
    SimpleSExprStream.printExpr(left(), out);
    Object r = right();
    if (r == null)
      out.print(")");
    else if (r instanceof Cons)
      ((Cons)r).printList(out, false);
    else {
      out.print(". ");
      SimpleSExprStream.printExpr(r, out);
      out.print(")");
    }
  }

}

class ConsEnumeration implements Enumeration {

  private Cons current;

  public ConsEnumeration(Cons head)
  {
    this.current = head;
  }

  public boolean hasMoreElements()
  {
    return current != null;
  }

  public Object nextElement()
  {
    Object element = null;
    try {
      element = current.left();
      current = current.rest();
    }
    catch (SExprParserException e) {
      // current is a dotted pair
      current = null;
    }
    return element;
  }

}
