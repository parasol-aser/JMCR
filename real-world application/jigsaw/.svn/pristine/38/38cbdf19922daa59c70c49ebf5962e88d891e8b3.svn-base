// URLDecoder.java
// $Id: URLDecoder.java,v 1.1 2010/06/15 12:29:14 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.forms ;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import java.util.Hashtable ;
import java.util.Enumeration ;

/**
 * Form data decoder.
 * This class takes an InputStream and decodes it in compliance to the
 * <b>application/x-www-form-urlencoded</b> MIME type.
 */

public class URLDecoder {
    public final static String EMPTY_VALUE = "" ;

    int         ch       = -1 ;
    Hashtable   values   = null ;
    byte        buffer[] = new byte[1024] ;
    int         bsize    = 0 ;
    Reader      in       = null ;
    boolean     overide  = true ;
    String      enc      = null ;
    
    private void append (int c) {
	if ( bsize+1 >= buffer.length ) {
	    byte nb[] = new byte[buffer.length*2] ;
	    System.arraycopy (buffer, 0, nb, 0, buffer.length) ;
	    buffer = nb ;
	}
	buffer[bsize++] = (byte) c ;
    }

    /**
     * Get an enumeration of the variable names.
     * @return An enumeration continaing one element per key.
     */

    public Enumeration keys() {
	return values.keys() ;
    }

    /**
     * Define a new variable in this data set.
     * @param name The name of the variable.
     * @param value Its value.
     */

    protected void addVariable (String var, String val) {
	if ( overide ) {
	    values.put (var, val) ;
	} else {
	    Object value = values.get (var) ;
	    if ( value == null ) {
		values.put (var, val) ;
	    } else if ( value instanceof String[] ) {
		String olds[] = (String[]) value ;
		String vals[] = new String[olds.length+1] ;
		System.arraycopy (olds, 0, vals, 0, olds.length) ;
		vals[olds.length] = val ;
		values.put (var, vals) ;
	    } else if ( value instanceof String ) {
		String vals[] = new String[2] ;
		vals[0] = (String) value ;
		vals[1] = val ;
		values.put (var, vals) ;
	    }
	}
    }

    /**
     * Get the values of the variable, as an array.
     * Use this method when you have turned off the <em>overide</em> flag
     * in the constructor of this object. This will always return either an
     * array of Strings or <strong>null</strong>.
     * <p>I use this in the PICS label bureau, and I pretty sure this is not a
     * good reason to have it here.
     * @param name The name of the variable to look for.
     * @return An String[] having one entry per variable's value, or <strong>
     * null</strong> if none was found.
     */

    public String[] getMultipleValues (String name) {
	if ( overide )
	    throw new RuntimeException (this.getClass().getName()
					+ "[getMultipleValues]: "
					+ " overide not set !") ;
	Object value = values.get (name) ;
	if ( value instanceof String[] ) {
	    return (String[]) value ;
	} else {
	    String vals[] = new String[1] ;
	    vals[0] = (String) value ;
	    values.put (name, vals) ;
	    return vals ;
	}
    }

    /**
     * Get the value of a variable.
     * If you have allowed the decoder to accumulate multiple values for the
     * same variable, this method casts of the value to a String may fail
     * <em>at runtime</em>.
     * @param name The name of the variable whose value is to be fetched.
     * @return Its values, which is always provided as a String, or null.
     */

    public String getValue (String name) {
	Object value = values.get(name) ;
	if ( (value != null) && ! (value instanceof String) )
	    throw new RuntimeException (this.getClass().getName()
					+ "[getValue]:"
					+ " use getMultipleValues in:\n\t"
					+ name + " " + value) ;
	return (String) value ;
    }

    private String stringify(byte[] bytes, int offset, int length)
       throws URLDecoderException
    {
	String __s = null;
	if (enc != null) {
	    try {
		__s = new String(bytes, offset, length, enc);
	    } catch (UnsupportedEncodingException uex) {
		throw new URLDecoderException ("Unsupported Encoding: "+enc);
	    }
	} else {
	    __s = new String(bytes, offset, length);
	}
	return __s;
    }
    
    /**
     * Parse our input stream following the
     * <b>application/x-www-form-urlencoded</b> specification.
     * @return The raw bindings obtained from parsing the stream, as a 
     * Hashtable instance.
     * @exception IOException When IO error occurs while reading the stream.
     * @exception URLDecoderException If the format is invalid.
     */

    public Hashtable parse () 
	throws IOException, URLDecoderException
    {
	String  key    = null ;

    read_loop:
	ch = in.read() ;
	while ( true ) {
	    switch (ch) {
	      case '+':
		  append (' ') ;
		  break ;
	      case '%':
		  int hi, lo ;
		  if ((hi = ch = in.read()) == -1)
		      throw new URLDecoderException ("Invalid escape seq.") ;
		  if ((lo = ch = in.read()) == -1)
		      throw new URLDecoderException ("Invalid escape seq.") ;
		  hi = (Character.isDigit((char) hi) 
			? (hi - '0')
			: 10 + (Character.toUpperCase((char) hi) - 'A')) ;
		  lo = (Character.isDigit((char) lo) 
			? (lo - '0')
			: 10 + (Character.toUpperCase((char) lo) - 'A')) ;
		  append ((char)(((byte) lo) | (((byte) hi) << 4))) ;
		  break ;
	      case '&':
		  if ( key == null ) {		      
		      // We only get a simple key (with no value)
			  addVariable(stringify(buffer,0,bsize), EMPTY_VALUE);
		      bsize = 0 ;
		  } else {
		      addVariable (key, stringify(buffer, 0, bsize)) ;
		      key   = null ;
		      bsize = 0 ;
		  }
		  break ;
	    case ';': // HTML4.0: appendix b2.2:  use of ";" in place of "&"
		  if ( key == null ) {
		      // We only get a simple key (with no value)
		      addVariable(stringify(buffer,0,bsize), EMPTY_VALUE);
		      bsize = 0 ;
		  } else {
		      addVariable (key, stringify(buffer, 0, bsize)) ;
		      key   = null ;
		      bsize = 0 ;
		  }
		  break ;
	      case '=':
		  if ( key != null ) {
		      append(ch);
		  } else {
		      key   = stringify(buffer, 0, bsize) ;
		      bsize = 0 ;
		  }
		  break ;
	      case -1:
		  // Same as '&', except that we return
		  if ( key == null ) {
		      // We only get a simple key (with no value)
		      addVariable(stringify(buffer,0,bsize), EMPTY_VALUE);
		      bsize = 0 ;
		  } else {
		      addVariable (key, stringify(buffer, 0, bsize)) ;
		      key   = null ;
		      bsize = 0 ;
		  }
		  return values ;
	      default:
		  append (ch) ;
		  break ;
	    }
	    ch = in.read() ;
	}
    }

    /**
     * Create an URLDecoder for the given stream.
     * @param in The input stream to be parsed.
     * @param list Tells how to handle multiple settings of the same variable.
     *    When <strong>false</strong>, mutiple settings to the same variable
     *    will accumulate the value into an array, returned by getValue(). 
     *    Otherwise, the last assignment will overide any previous assignment.
     * @param encoding The character encoding used.
     */

    public URLDecoder (Reader in, boolean overide, String encoding) {
	this.values  = new Hashtable (23) ;
	this.in      = in ;
	this.ch      = -1 ;
	this.overide = overide ;
	this.enc     = encoding ;
    }

    /**
     * Create an URLDecoder for the given stream.
     * @param in The input stream to be parsed.
     * @param list Tells how to handle multiple settings of the same variable.
     *    When <strong>false</strong>, mutiple settings to the same variable
     *    will accumulate the value into an array, returned by getValue(). 
     *    Otherwise, the last assignment will overide any previous assignment.
     */

    public URLDecoder (Reader in, boolean overide) {
	    this (in, overide, null);
    }

    /**
     * Create an URLDecoder for the given stream.
     * @param in The input stream to be parsed.
     * @param list Tells how to handle multiple settings of the same variable.
     *    When <strong>false</strong>, mutiple settings to the same variable
     *    will accumulate the value into an array, returned by getValue(). 
     *    Otherwise, the last assignment will overide any previous assignment.
     */

    public URLDecoder (Reader in) {
       this(in, true, null);
    }

    /**
     * Create an URLDecoder for the given stream.
     * @param in The input stream to be parsed.
     * @param list Tells how to handle multiple settings of the same variable.
     *    When <strong>false</strong>, mutiple settings to the same variable
     *    will accumulate the value into an array, returned by getValue(). 
     *    Otherwise, the last assignment will overide any previous assignment.
     * @param encoding The character encoding used.
     */

    public URLDecoder (InputStream in, boolean overide, String encoding) {
	this.values  = new Hashtable (23) ;
	this.in      = new InputStreamReader(in) ;
	this.ch      = -1 ;
	this.overide = overide ;
	this.enc     = encoding;
    }

    /**
     * Create an URLDecoder for the given stream.
     * @param in The input stream to be parsed.
     * @param list Tells how to handle multiple settings of the same variable.
     *    When <strong>false</strong>, mutiple settings to the same variable
     *    will accumulate the value into an array, returned by getValue(). 
     *    Otherwise, the last assignment will overide any previous assignment.
     */

    public URLDecoder (InputStream in, boolean overide) {
	this (in, overide, null);
    }

    /**
     * Create an URLDecoder for the given stream.
     * Default constructor, which will keep track only of the last setting
     * of the same variable (if ever it gets assigned multiply).
     * @param in The input stream to be parsed.
     */

    public URLDecoder (InputStream in) {
	this (in, true, null) ;
    }

}
