// SampleLabel.java
// $Id: SampleLabel.java,v 1.1 2010/06/15 12:25:29 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.pics ;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberInputStream;
import java.io.PrintStream;
import java.io.StringBufferInputStream;

// I like writing these silly kind of parsers !
// (its probably the X)

class LabelParser { 
    File file = null ;
    LineNumberInputStream in = null ;
    int         ch = -1 ;
    byte buffer[] = null ;
    int  bufptr = 0 ;

    /**
     * Append the given char in the internal buffer
     */

    public void append (int ch) {
	if ( bufptr + 1 >= buffer.length) {
	    // resize buffer 
	    byte nbuf[] = new byte[buffer.length * 2] ;
	    System.arraycopy (buffer, 0, nbuf, 0, bufptr) ;
	    buffer = nbuf ;
	}
	buffer[bufptr++] = (byte) ch ;
    }

    /**
     * Get the token from our internale buffer.
     */

    public String getToken (boolean clear) {
	String tok = new String (buffer, 0, 0, bufptr) ;
	if ( clear )
	    bufptr = 0 ;
	return tok ;
    }

    /**
     * Parser expects given character.
     */

    void expect (int c) 
	throws InvalidLabelException
    {
	if ( ch == c )
	    return ;
	String msg = ("expected " 
		      + (new Character((char) c)).toString()
		      + "[" + c + "]"
		      + " got " 
		      + (new Character((char) ch)).toString() 
		      + "[" + ch + "]");
	if (file == null)
	  throw new InvalidLabelException(in.getLineNumber(), msg) ;
	else
	  throw new InvalidLabelFileException (file, in.getLineNumber(), msg) ;
    }

    String parseVariableName () 
	throws IOException
    {
	while ((ch != '=') && (ch != '\n') && (ch != -1)) {
	    append (Character.toLowerCase((char)ch)) ;
	    ch = in.read() ;
	}
	return getToken(true) ;
    }

    String parseVariableValue() 
	throws IOException
    {
	while ( (ch != -1) && (ch != '\n') ) {
	    append (ch) ;
	    ch = in.read() ;
	}
	return getToken(true) ;
    }

    SampleLabel parse (SampleLabel into)
	throws InvalidLabelException, InvalidLabelFileException
    {
	try {
	    while ( true ) {
		switch (ch) {
		  case -1:
		      // we are done.
		      return into ;
		  case ' ': case '\t': case '\n':
		      ch = in.read() ;
		      continue ;
		  case '#':
		      while ( ((ch = in.read()) != '\n') && (ch != -1) )
			  ;
		      continue ;
		  default:
		      String name = parseVariableName() ;
		      expect ('=') ; ch = in.read() ;
		      String value = parseVariableValue() ;
		      if (ch != -1) { // Pb -1 instead of \n
			expect ('\n') ; 
			ch = in.read() ;
		      }
		      into.addBinding (name, value) ;
		      continue ;
		}
	    }
	} catch (IOException e) {
	  if (file == null)
	    throw new InvalidLabelException( "IO exception.") ;
	  else
	    throw new InvalidLabelFileException (file.getAbsolutePath()
						 + ": IO exception.") ;
	}
    }

    LabelParser (File file) 
	throws InvalidLabelFileException
    {
	this.file = file ;
	try {
	    this.in = (new LineNumberInputStream 
		       (new BufferedInputStream
			(new FileInputStream (file)))) ;
	    this.buffer = new byte[32] ;
	    this.ch = in.read() ;
	} catch (IOException e) {
	    throw new InvalidLabelFileException (file.getAbsolutePath()
						 + ": IO exception.") ;
	}
    }

    LabelParser (String string) 
	throws InvalidLabelException
    {
	try {
	    this.in = (new LineNumberInputStream 
		       (new BufferedInputStream
			(new StringBufferInputStream (string)))) ;

	    this.buffer = new byte[32] ;
	    this.ch = in.read() ;
	} catch (IOException e) {
	    throw new InvalidLabelException( "IO exception.") ;
	}
    }

}

/**
 * Label internal representation.
 * The server has to know something about labels. In this implementation, I try
 * to reduce this knowledge as fas as I could. Here, a label is a set of
 * assignements to variables (options in PICS terms), and a rating.
 * The syntax for writing label files is the following:
 * <variable>=<value>\n
 * <p>Comments are allowed through the '#' at beginning of line.
 * With the special variable 'ratings' being mandatory.
 */

public class SampleLabel implements LabelInterface {
    // Default variables array size
    private static final int VARSIZE = 8 ;

    String vars[] = null ;
    String vals[] = null ;

    int    varptr = 0 ;

    void addBinding (String var, String val) {
	if ( varptr + 1 >= vars.length ) {
	    // resize arrays 
	    String nvars[] = new String[vars.length*2] ;
	    String nvals[] = new String[vals.length*2] ;
	    System.arraycopy (vars, 0, nvars, 0, vars.length) ;
	    System.arraycopy (vals, 0, nvals, 0, vals.length) ;
	    vars = nvars ;
	    vals = nvals ;
	}
	vars[varptr] = var ;
	vals[varptr] = val ;
	varptr++ ;
    }

    /**
     * Debugging: print the given options of this label.
     * @param out The PrintStream to print to.
     */

    public void print (PrintStream out) {
	for (int i = 0 ; i < varptr ; i++) {
	    System.out.println ("\t" + vars[i] + " = " + vals[i]) ;
	}
    }

    /**
     * Does this label defines the given option ?
     * @param option The option to look for.
     * @return <strong>true</strong> if the label defines the given option.
     */

    public boolean hasOption (String option) {
	for (int i = 0 ; i < varptr ; i++) {
	    if ( vars[i].equals (option) )
		return true ;
	}
	return false ;
    }

    /**
     * Get an option index.
     * This allows for fast acces to label options. There is no guarantee that
     * the same option will get the same index across labels.
     * @param option The option to look for.
     * @return An integer, which is the option index if the option was found
     *    or <strong>-1</strong> if the option isn't defined for this label.
     */

    public int getOptionIndex (String option) {
	for (int i = 0 ; i < varptr ; i++) {
	    if ( vars[i].equals (option) )
		return i;
	}
	return -1 ;
    }

    /**
     * Get an option value, by index.
     * This, along with the <strong>getOptionIndex</strong> method provides
     * a fast access to option values.
     * @param idx The index of the option, as gotten from 
     *    <strong>getOptionIndex</strong>.
     * @return A String instance, providing the option value.
     */

    public String getOptionValue (int idx) {
	if ( (idx < 0) || (idx >= varptr) ) 
	    throw new RuntimeException (this.getClass().getName()
					+ "[getOptionValue]: "
					+ " invalid index.") ;
	return vals[idx] ;
    }

    /**
     * Get an option value, by option name.
     * @param option The option that you want the value of.
     * @return A String instance giving the option value, or 
     *    <strong>null</strong>, if the option isn't defined for this label.
     */

    public String getOptionValue (String option) {
	for (int i = 0 ; i < varptr ; i++) {
	    if ( vars[i].equals (option) ) 
		return vals[i] ;
	}
	return null ;
    }

    /**
     * Is this label generic ?
     * @return <strong>true</strong> if the label if generic.
     */

    public boolean isGeneric () {
	int idx = getOptionIndex ("generic") ;
	if ( idx >= 0 )
	    return (new Boolean(getOptionValue (idx))).booleanValue() ;
	return false ;
    }

    /**
     * Emit the given option to the output.
     * If the option is not defined for this label, skip it (don't emit). If 
     * possible, and according to the format, emit the short option name.
     * @param space Should we emit a leading space.
     * @param option The option name.
     * @return <strong>true</strong> if the option was successfully emited,
     *    <strong>false</strong> otherwise.
     */

    private boolean emit (boolean space
			  , StringBuffer into
			  , String option, int format) {
	String value = getOptionValue (option) ;
	if ( value != null ) {
	    switch (format) {
	      case LabelBureauInterface.FMT_MINIMAL:
	      case LabelBureauInterface.FMT_SHORT:
		  // emit short option name
		  if ( space )
		      into.append (" ") ;
		  into.append (option+" "+value) ;
		  break ;
	      default:
		  // emit full option name
		  if ( space )
		      into.append (" ") ;
		  into.append (option+" "+value) ;
		  break ;
	    }
	    return true ;
	}
	return false ;
    }

    /**
     * Emit the given option to the output.
     * If possible, and according to the format, emit the short option name.
     * @param space Should we emit a leading space.
     * @param into The StringBuffer to dump this label to.
     * @param option The option name.
     */

    private void emit (boolean space, StringBuffer into, int idx, int format) {
	switch (format) {
	  case LabelBureauInterface.FMT_MINIMAL:
	  case LabelBureauInterface.FMT_SHORT:
	      // emit short option name
	      if ( space )
		  into.append (" ") ;
	      into.append (vars[idx]+" "+vals[idx]) ;
	      break ;
	  default:
	      if ( space )
		  into.append (" ") ;
	      into.append (vars[idx]+" "+vals[idx]) ;
	}
    }

    /**
     * Dump this label according to the given format.
     * The dump should take place <em>after</em> the service has dump itself.
     * @param into A StringBuffer to dump the labels to.
     * @param format The format in which the dump should be done. This can
     *    be one of the four format described in the PICS specification.
     */

    public void dump (StringBuffer into, int format) {
	boolean space = false ; ;
	switch (format) {
	  case LabelBureauInterface.FMT_MINIMAL:
	      // emit just the ratings, except if generic
	      if ( isGeneric() ) {
		  // emit the generic and for options (FIXME errors ?)
		  emit (space,into,"generic",LabelBureauInterface.FMT_MINIMAL);
		  space = true ;
		  emit (space,into,"for",LabelBureauInterface.FMT_MINIMAL) ;
		  emit (space,into,"ratings",LabelBureauInterface.FMT_MINIMAL);
	      } else {
		  emit (space,into,"ratings",LabelBureauInterface.FMT_MINIMAL);
	      }
	      break ;
	  case LabelBureauInterface.FMT_SHORT:
	      // emit ratings, and full, which I deem appropriate
	      if ( isGeneric() ) {
		  // emit the generic and for options:
		  emit (space,into, "generic", LabelBureauInterface.FMT_SHORT);
		  space = true ;
		  emit (space,into, "for", LabelBureauInterface.FMT_SHORT) ;
		  emit (space,into, "full", LabelBureauInterface.FMT_SHORT) ;
		  emit (space,into, "ratings", LabelBureauInterface.FMT_SHORT);
	      } else {
		  emit (space, into, "full", LabelBureauInterface.FMT_SHORT) ;
		  space = true ;
		  emit (space,into,"ratings",LabelBureauInterface.FMT_SHORT) ;
	      }
	      break ;
	  case LabelBureauInterface.FMT_FULL:
	      // Emit all options, rating at the end
	      for (int i = 0 ; i < varptr ; i++) {
		  if ( vars[i].equals("ratings") )
		      continue ;
		  emit (space, into, i, LabelBureauInterface.FMT_FULL) ;
		  space = true ;
	      }
	      emit (space, into, "ratings", LabelBureauInterface.FMT_FULL) ;
	      break ;
	  case LabelBureauInterface.FMT_SIGNED:
	      throw new RuntimeException (this.getClass().getName()
					  + "[dump]: "
					  + "SIGNED format unsupported.") ;
	      
	      // not reached
	  default:
	      throw new RuntimeException (this.getClass().getName()
					  + "[dump]: "
					  + " invalid format "
					  + "\"" + format + "\"") ;
	}
    }

    /**
     * Create a new label from the given stream.
     * @param file The file inwhich the label options are described.
     */

    SampleLabel (File file) 
	throws InvalidLabelException
    {
      try {
	this.vars   = new String[VARSIZE] ;
	this.vals   = new String[VARSIZE] ;
	this.varptr = 0 ;
	LabelParser p = new LabelParser (file) ;
	p.parse (this) ;
      } catch (InvalidLabelException e) {
	throw new InvalidLabelFileException(e.getMessage());
      }
    }

    SampleLabel (String string) 
	throws InvalidLabelException
    {
	this.vars   = new String[VARSIZE] ;
	this.vals   = new String[VARSIZE] ;
	this.varptr = 0 ;
	LabelParser p = new LabelParser (string) ;
	p.parse (this) ;
    }
    /**
     * Create a new label from a set of options.
     * This constructor takes two arrays, one of option names, and one
     * of option values. It builds out of them the internal
     * representation for this label.
     * <p>The given arrays are used as is (no copy), and might side-effected
     * by the new instance.
     * @param optnames Names of option for this label.
     * @param optvalues Values of option for this label.  
     */

    public SampleLabel (String optnames[], String optvals[]) {
	if ( optnames.length != optvals.length )
	    throw new RuntimeException (this.getClass().getName()
					+ " invalid constructor params:"
					+ " bad length.") ;
	this.vars   = optnames ;
	this.vals   = optvals ;
	this.varptr = optnames.length ;
    }

    // Testing only

    public static void main (String args[]) {
	if ( args.length != 1 ) {
	    System.out.println ("Label <file>") ;
	    System.exit (1) ;
	}
	try {
	    SampleLabel label = new SampleLabel (new File (args[0])) ;
	    StringBuffer sb = new StringBuffer() ;
	    label.dump (sb, LabelBureauInterface.FMT_MINIMAL) ;
	    System.out.println (sb.toString()) ;
	} catch (InvalidLabelException e) {
	    System.out.println (e.getMessage()) ;
	    System.exit (1) ;
	}
	System.exit (0) ;
    }

}


