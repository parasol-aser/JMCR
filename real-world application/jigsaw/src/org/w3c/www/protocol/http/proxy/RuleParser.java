// RuleParserException.java
// $Id: RuleParser.java,v 1.1 2010/06/15 12:28:31 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.proxy ;

import java.util.StringTokenizer;
import java.util.Vector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

/**
 * A simple Rule parser.
 */

public class RuleParser {

    InputStream in = null;
    RuleNode    root = null;

    /**
     * Add a mapping for the given rule in our rule node.
     * @param lhs The rule left hand side, as a parsed String array.
     * @param rule The mapped rule instance.
     */

    protected void addRule(String lhs[], Rule rule) {
	RuleNode node = root;
	int lhslen    = lhs.length;
	if(!lhs[0].equals("default")) {
	    for (int i = lhslen ; --i >= 0 ; ) {
		RuleNode child = node.lookup(lhs[i]);
		if ( child == null ) 
		    child = node.addChild(lhs[i]);
		node = child;
	    }
	}
	node.setRule(rule);
    }

    /**
     * Create a suitable rule mapping for the tokenized rule.
     * @param tokens The rule tokens, as a String array.
     * @param toklen Number of tokens in above array.
     * @exception RuleParserException if parsing failed.
     */

    protected void parseRule(String tokens[], int toklen)
	throws RuleParserException
    {
	// Get and parse the rule left hand side first:
	StringTokenizer st = new StringTokenizer((String) tokens[0], ".");
	Vector vlhs = new Vector();
	String vls;
	boolean isnum = false;
	while (st.hasMoreTokens()) {
	    isnum = true;
	    vls = st.nextToken();
	    for(int i=0; isnum && (i<vls.length()); i++)
		isnum = (vls.charAt(i)>='0') && (vls.charAt(i)<='9');
	    vlhs.addElement(vls);
	}
	// if numeric, reverse the order of tokens
	if(isnum) {
	    int vs = vlhs.size();
	    for(int i=0; i<vs;i++) {
		vlhs.addElement(vlhs.elementAt(vs-i-1));
		vlhs.removeElementAt(vs-i-1);
	    }
	}
	String slhs[] = new String[vlhs.size()];
	vlhs.copyInto(slhs);
	// Build a rule instance:
	Rule rule = Rule.createRule(tokens, 1, toklen);
	// Install the rule in our root node:
	addRule(slhs, rule);
    }

    /**
     * Parse the our input stream into a RuleNode instance.
     * @exception IOException If reading the rule input stream failed.
     * @exception RuleParserException If some invalid rule syntax was
     * detected.
     */

    public RuleNode parse() 
	throws RuleParserException, IOException
    {
	// Initialize the stream tokenizer:
	boolean         eof = false;
	BufferedReader br = new BufferedReader(new InputStreamReader(in));
	StreamTokenizer st  = new StreamTokenizer(br);
	// do syntax by hand
	st.resetSyntax();
        st.wordChars('a', 'z');
        st.wordChars('A', 'Z');
	st.wordChars('0', '9');
        st.wordChars(128 + 32, 255);
        st.whitespaceChars(0, ' ');
	st.wordChars(33, 128);
	st.commentChar('#');
	st.eolIsSignificant(true);
	st.lowerCaseMode(true);
	// Create the root node, to be returned:
	root = new RuleNode();
	String tokens[] = new String[32];
	int    toklen   = 0;
	// Parse input:
	while ( ! eof ) {
	    // Read one line of input, parse it:
	    while (! eof) {
		int tt =-1;
		switch(tt = st.nextToken()) {
		  case StreamTokenizer.TT_EOF:
		      eof = true;
		      if ( toklen > 0 ) {
			  try {
			      parseRule(tokens, toklen);
			  } catch (RuleParserException ex) {
			      String msg = ("Error while parsing rule file, "
					    + "line "+st.lineno()+": "
					    + ex.getMessage());
			      throw new RuleParserException(msg);
			  }
			  toklen = 0;
		      }
		      break;
		  case StreamTokenizer.TT_EOL:
		      if ( toklen > 0 ) {
			  try {
			      parseRule(tokens, toklen);
			  } catch (RuleParserException ex) {
			      String msg = ("Error while parsing rule file, "
					    + "line "+st.lineno()+": "
					    + ex.getMessage());
			      throw new RuleParserException(msg);
			  }
			  toklen = 0;
		      }
		      break;
		  case StreamTokenizer.TT_WORD:
		      // Add that token:
		      if ( toklen + 1 >= tokens.length ) {
			  String newtok[] = new String[tokens.length+8];
			  System.arraycopy(tokens, 0, newtok, 0, toklen);
			  tokens = newtok;
		      }
		      tokens[toklen++] = st.sval;
		      break;
		  default:
		      throw new RuleParserException("Invalid syntax, line "
                                                    + st.lineno()
						    + ".");
		}
	    }
	}
	return root;
    }

    /**
     * Create a rule parser to parse the given input stream.
     */

    public RuleParser(InputStream in) {
	this.in = in ;
    }

}	


