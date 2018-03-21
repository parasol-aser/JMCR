// CommandLine.java
// $Id: CommandLine.java,v 1.1 2010/06/15 12:26:30 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 2002.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.offline.command;

import java.io.StringReader;
import java.io.StreamTokenizer;
import java.io.IOException;
import java.util.Vector;


import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.MalformedPatternException;

/**
 * <p>The jigshell command line class.
 */
public class CommandLine {

	/** static members: jigshell commands syntax */
	public static String WHERE = "where";
	public static String LIST = "list";
	public static String GO = "go" ;
	public static String REC = "r" ;
	public static String ATTR = "a" ;
	public static String UP = ".." ;

	public static String NO_OPT = "none" ;

	/* private class members */
	private String cmd = null ;
	private String action = null ;
	private String target = null ;
	private String option = NO_OPT ;

	/** 
	 * a Vector to handle elements of a parsed command
	 */
	protected Vector parsedCmd ;

	/* protected static members */
	protected static Perl5Matcher pmatcher = new Perl5Matcher() ;
	protected static Perl5Compiler pcompiler = new Perl5Compiler() ;
	protected static Pattern srPattern  ;


   /** 
	*  Initialize a CommandLine instance.  
	* @param s the command line.
	*/
	public CommandLine(String s){
		try {
			cmd = s ;
			parsedCmd = new Vector ();
			srPattern = pcompiler.compile(
			"^s/[\\w|=|\\*|\\-|\\\\/]+?/[\\w|\\-|\\\\/]+/$",
						Perl5Compiler.DEFAULT_MASK);
		}
		catch (org.apache.oro.text.regex.MalformedPatternException ex){
			ex.printStackTrace();
		}
	}


   /** 
	* Parse a CommandLine instance.  
	*/
	public void parse() throws CommandParseException {

		StringReader r = new StringReader(cmd) ;
		StreamTokenizer st = new StreamTokenizer(r);

		st.ordinaryChar('.');
	//equivalent to (ascii codes):	st.wordChars(33,44)
		st.wordChars('!',',');
	//equivalent to:	st.wordChars(46,47)
		st.wordChars('.','/');
		st.wordChars('=','=');
	//equivalent to:	st.wordChars(63,64)
		st.wordChars('?','@');
	//equivalent to:	st.wordChars(91,96)
		st.wordChars('[','`');

		try {
			while (st.nextToken() != st.TT_EOF){
				if (st.ttype == st.TT_WORD){
					parsedCmd.addElement(new String(st.sval));
				}
				if (st.ttype == '-'){
					parsedCmd.addElement(new String("-"));
				}
				if (st.ttype == st.TT_NUMBER){
				}
			}
		}
		catch (IOException e){
			throw new CommandParseException() ;
		}

		switch (parsedCmd.size()){
			case 0:
				break;
			case 1: // simple command
				action = (String)parsedCmd.elementAt(0);
				if (action.compareTo(LIST) ==0 ||
					action.compareTo(WHERE) == 0){
					target = ".*";	
				}
				else {
					throw new CommandParseException() ;
				}
				break;

			default : // more than 1 element in the command line
				action = (String)parsedCmd.elementAt(0);
				if ( isaReplaceAction(action) == true ||
					action.compareTo(LIST)==0 ||
					action.compareTo(GO)==0){

					boolean isOption = false ;
					for (int i = 1 ; i < parsedCmd.size() ; i++){
						String curWord = (String)parsedCmd.elementAt(i);
				//		System.out.println(curWord+"  "+option);
						if (isOption){
							/* we already met an option modifier, we're 
							waiting for an option */
							isOption = false ;
							if (curWord.compareTo(REC) == 0 
										|| curWord.compareTo(ATTR) == 0 
										|| curWord.compareTo(REC+ATTR) == 0
										|| curWord.compareTo(ATTR+REC) == 0 ){
								option = curWord ;
							} else {
								// unknown option
								System.out.println("option discarded "+curWord);
							}
						} else {
								/* beginning of an option (modifier) */
							if (curWord.compareTo("-")==0){
								isOption = true ;
							} else { 
								/* we're not waiting for an option so
								it's the command target */
								target = curWord ;
								break ;
							}
						}
					}
				}
				else {
					throw new CommandParseException() ;
				}
				if (target == null){
					throw new CommandParseException() ;
				}
		}
	}

	/**
	 * Get the command line action
	 * @return the string action (should be a jigshell action).
	 */
	public String getAction(){ 
		return (action) ;
	}

	/**
	 * Get the command target 
	 * @return the string target (should be a name or regexp).
	 */
	public String getTarget(){
		return (target) ;
	}

	/**
	 * Get the command option 
	 * @return the command option ("none" if no option specified in
	 * the command line).
	 */
	public String getOption(){
		return (option) ;
	}

	/* check whether a string is a s/truc/chose/ regexp. */
	private boolean isaReplaceAction(String s){
		
		
		if (pmatcher.matches(s, srPattern)){
			return true ;
		}
		return false ;
	}
}
