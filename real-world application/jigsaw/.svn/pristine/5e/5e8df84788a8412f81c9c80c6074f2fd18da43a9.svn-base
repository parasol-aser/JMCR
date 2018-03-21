/*
 *  SimpleSExprStream.java
 *
 *  Copyright 1997 Massachusetts Institute of Technology.
 *  All Rights Reserved.
 *
 *  Author: Ora Lassila
 *
 *  $Id: SimpleSExprStream.java,v 1.1 2010/06/15 12:27:49 smhuang Exp $
 */

package org.w3c.tools.sexpr;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.PushbackInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Basic implementation of the SExprStream parser interface.
 */
public class SimpleSExprStream extends PushbackInputStream
                               implements SExprStream {

  private StringBuffer buffer;
  private Dictionary symbols;
  private boolean noSymbols;
  private Readtable readtable;
  private boolean listsAsVectors;

  /**
   * Initializes the parser with no read table and no symbol table assigned.
   * Parsed lists will be represented as Cons cells.
   */
  public SimpleSExprStream(InputStream input)
  {
    super(input);
    this.buffer = new StringBuffer();
    this.symbols = null;
    this.noSymbols = false;
    this.readtable = null;
    this.listsAsVectors = false;
  }

  /**
   * Accesses the symbol table of the parser.
   * If no symbol table has been assigned, creates an empty table.
   */
  public Dictionary getSymbols()
  {
    if (!noSymbols && symbols == null)
      symbols = new Hashtable();
    return symbols;
  }

  /**
   * Assigns a symbol table to the parser.
   * Assigning <tt>null</tt> will prevent an empty symbol table to be created
   * in the future.
   */
  public Dictionary setSymbols(Dictionary symbols)
  {
    if (symbols == null)
      noSymbols = true;
    return this.symbols = symbols;
  }

  /**
   * Accesses the read table of the parser.
   * If no read table has been assigned, creates an empty table.
   */
  public Readtable getReadtable()
  {
    if (readtable == null)
      readtable = new SimpleReadtable();
    return readtable;
  }

  /**
   * Assigns a new read table to the parser.
   */
  public Readtable setReadtable(Readtable readtable)
  {
    return this.readtable = readtable;
  }

  /**
   * Checks whether lists should be parsed as Vectors or Cons cells.
   */
  public boolean getListsAsVectors()
  {
    return listsAsVectors;
  }

  /**
   * Controls whether lists are represented as Vectors or Cons cells.
   */
  public boolean setListsAsVectors(boolean listsAsVectors)
  {
    return this.listsAsVectors = listsAsVectors;
  }

  /**
   * Accesses an empty string buffer available temporary storage.
   * This buffer can be used by sub-parsers as a scratch area. Please note
   * that the buffer is not guarded in any way, so multithreaded and reentrant
   * programs must worry about this themselves.
   */
  public StringBuffer getScratchBuffer()
  {
    buffer.setLength(0);
    return buffer;
  }

  /**
   * Parses a single object from the underlying input stream.
   *
   * @exception SExprParserException if syntax error was detected
   * @exception IOException if any other I/O-related problem occurred
   */
  public Object parse()
    throws SExprParserException, IOException
  {
    return parse(readSkipWhite(), this);
  }

  /**
   * Parses a single object started by the character <i>c</i>.
   * Implements the SExprParser interface.
   *
   * @exception SExprParserException if syntax error was detected
   * @exception IOException if any other I/O-related problem occurred
   */
  public Object parse(char c, SExprStream stream)
    throws SExprParserException, IOException
  {
    SExprParser parser = getReadtable().getParser(c);
    if (parser != null)
      return parser.parse(c, this);
    else if (c == '(') {
      if (getListsAsVectors())
        return parseVector(new Vector(), ')');
      else
        return parseList();
    }
    else if (c == '"')
      return parseString();
    else if (isAtomChar(c, true))
      return parseAtom(c);
    else
      throw new SExprParserException(c);
  }

  /**
   * Parses a list (as Cons cells) sans first character.
   *
   * @exception SExprParserException if syntax error was detected
   * @exception IOException if any other I/O-related problem occurred
   */
  protected Cons parseList()
    throws SExprParserException, IOException
  {
    char c = readSkipWhite();
    if (c == ')')
      return null;
    else {
      unread(c);
      return new Cons(parse(), parseList());
    }
  }

  /**
   * Parses a list (as a Vector) sans first character.
   * In order to parse list-like structures delimited by other characters
   * than parentheses, the delimiting (ending) character has to be provided.
   *
   * @exception SExprParserException if syntax error was detected
   * @exception IOException if any other I/O-related problem occurred
   */
  protected Vector parseVector(Vector vector, char delimiter)
    throws SExprParserException, IOException
  {
    char c = readSkipWhite();
    if (c == delimiter)
      return vector;
    else {
      unread(c);
      vector.addElement(parse());
      return parseVector(vector, delimiter);
    }
  }

  /**
   * Parses an atom (a number or a symbol).
   * Since anything that is not a number is a symbol, syntax errors are not
   * possible.
   *
   * @exception SExprParserException not signalled but useful for the protocol
   * @exception IOException if an I/O problem occurred (e.g. end of file)
   */
  protected Object parseAtom(char c)
    throws SExprParserException, IOException
  {
    StringBuffer b = getScratchBuffer();
    do {
      b.append(c);
    } while (isAtomChar(c = (char)read(), false));
    unread(c);
    String s = b.toString();
    try {
      return makeNumber(s);
    }
    catch (NumberFormatException e) {
      return Symbol.makeSymbol(s, getSymbols());
    }
  }

  /**
   * Parses a double-quote -delimited string (sans the first character).
   * Please note: no escape-character interpretation is performed. Override
   * this method for any escape character handling.
   *
   * @exception SExprParserException not signalled but useful for the protocol
   * @exception IOException any I/O problem (including end of file)
   */
  public String parseString()
    throws SExprParserException, IOException
  {
    int code;
    StringBuffer b = getScratchBuffer();
    while (true) {
      switch (code = read()) {
        case (int)'"':
          return new String(b);
        case -1:
          throw new EOFException();
        default:
          b.append((char)code);
          break;
      }
    }
  }

  /**
   * Predicate function for checking if a chahracter can belong to an atom.
   *
   * @param first if true means that c is the first character of the atom
   */
  protected boolean isAtomChar(char c, boolean first)
  {
    return !(Character.isSpace(c)
             || c == '(' || c == ')' || c == '"' || c == '}' || c == '{');
  }

  /**
   * Reads from the stream, skipping whitespace and comments.
   *
   * @exception IOException if an I/O problem occurred (including end of file)
   */
  public char readSkipWhite()
    throws IOException
  {
    char c;
    do {
      c = (char)read();
      if (c == ';') // skip comments
        do {} while ((c = (char)read()) != '\n' && c != '\r');
      if (c == -1)
        throw new EOFException();
    } while (Character.isSpace(c));
    return c;
  }

  /**
   * Attempts to parse a number from the string.
   *
   * @exception NumberFormatException the string does not represent a number
   */
  protected Number makeNumber(String s)
    throws NumberFormatException
  {
    try {
      return Integer.valueOf(s);
    }
    catch (NumberFormatException e) {
      return DoubleFix.valueOf(s);
    }
  }

  /**
   * Associates a dispatch character with a parser in the read table.
   */
  public SExprParser addParser(char key, SExprParser parser)
  {
    return getReadtable().addParser(key, parser);
  }

  /**
   * Produces a printed representation of an s-expression.
   */
  public static void printExpr(Object expr, PrintStream out)
  {
    if (expr == null)
      out.print("nil");
    else if (expr instanceof Number)
      out.print(expr);
    else if (expr instanceof String) {
      out.print('"');
      out.print(expr);
      out.print('"');
    }
    else if (expr instanceof Vector) {
      out.print("(");
      for (int i = 0; i < ((Vector)expr).size(); i++) {
        if (i != 0)
          out.print(" ");
        printExpr(((Vector)expr).elementAt(i), out);
      }
      out.print(")");
    }
    else if (expr instanceof SExpr)
      ((SExpr)expr).printExpr(out);
    else
      out.print("#<unknown " + expr + ">");
  }

  public static void main(String args[])
    throws SExprParserException, IOException
  {
    SExprStream p = new SimpleSExprStream(System.in);
    Object e = p.parse();
    SimpleSExprStream.printExpr(e, System.out);
    System.out.println();
  }

}
