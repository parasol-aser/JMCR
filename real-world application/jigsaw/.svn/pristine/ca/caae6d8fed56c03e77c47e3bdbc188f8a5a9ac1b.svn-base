// CommandInterpreter.java
// $Id: CommandInterpreter.java,v 1.1 2010/06/15 12:26:29 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 2002.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.offline.command;

import org.w3c.tools.offline.browse.StoreFinder ;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Vector;


/**
 * <p>The jigshell interpreter class.
 */
public class CommandInterpreter {

	/**
	 * The StoreFinder instance to handle the store files.
	 */
	protected static StoreFinder sf = null ;

	/**
	 * A boolean to switch to batch mode when a script file is given
	 */
	protected static boolean interactive = true ;

	/**
	 * A reader for commands input. 
	 */
	protected static BufferedReader reader = null ;

	
	/**
	 * A CommandInterpreter initializer.
	 * @param args arguments provided when launching jigshell
	 */
    public CommandInterpreter(String [] args) {
		boolean interactive = true ;
	    if(args.length < 2 ) {
    	    String[] arg = {"config/stores","http-server"};
			System.out.println("assuming arguments are `config/stores http-server`");
        	sf = new org.w3c.tools.offline.browse.StoreFinder(arg[0],arg[1]);
    	} else {
			if (args.length > 3)  {
				System.out.println("usage :");
				System.out.print("java org.w3c.tools.offline.command.Main ");
				System.out.print("stores_directory server [batch_file]\n");
				System.exit(1) ;
			}
			if (args.length == 3){
				System.out.println("entering batch mode...");
				interactive=false;
			} 
        	sf = new org.w3c.tools.offline.browse.StoreFinder(args[0],args[1]);
    	}
		try {
			sf.readStoreIndex() ;
		}
		catch (IOException e){
			System.out.println("Can't read store. Exiting.");
			System.exit(2);
		}
		try {
			sf.readStoreRoot() ;
		}
		catch (Exception e){
			System.out.println("can't find root file");
			System.exit(3);
		}
		if (interactive) runInteractive();
		else runScriptFile(args[2]);
	}

	/**
	* Run a script file (batch mode)
	* @param batchFile the script file name.
	*/
	private void runScriptFile(String batchFile){
		interactive = false ;
		try {
			reader = new BufferedReader(new FileReader(batchFile)); 
		} 
		catch (java.io.FileNotFoundException e) { 
			System.out.println("batch not found: " + batchFile); 
			System.exit(4);
		}
		run() ;
	}

	/**
	* Run the interactive mode.
	*/
	private void runInteractive(){
		interactive = true ;
	    reader = new BufferedReader(new InputStreamReader(System.in), 1);
		run() ;
	}


	/**
	* Run the interpreter
	*/
	private void run(){
	
		while (true) try {

			if ( interactive ) { 
				System.out.print(prompt()); 
				System.out.flush(); 
			}

			String theLine = reader.readLine(); 
			if (theLine == null) break;  
			if (theLine.equals("exit")) break; 
			CommandLine cl = new CommandLine (theLine) ;
			this.ProcessCommand(cl);
		} 
		catch (IOException e) { 
			System.err.println(e); 
		}
    }

	
	private String prompt (){
		return ("jigshell-alpha> ");
	}


	/**
	* Process a command line instance and initiate the action
	* through the StoreFinder.
	* @param cl the CommandLine.
	*/
	protected void ProcessCommand(CommandLine cl){
		try {
			cl.parse() ;
			String action = cl.getAction();
			boolean recursive = false ;
			String opt = cl.getOption();
			if (opt.indexOf(cl.REC) != -1){
				recursive = true ;
			//	System.out.println ("recursive action"+recursive);
			}
			if (opt.indexOf(cl.ATTR) != -1 && action.compareTo(cl.LIST)==0){
				action = "listatt" ;
			//	System.out.println ("list with attributes");
			}
			if (action != null){
				if (action.compareTo(cl.WHERE)==0) {
					sf.printWorkingRep();
				} else if (action.compareTo(cl.GO)==0) {
					sf.setWorkingRep(cl.getTarget());	
				} else {
					sf.finderAction(action,cl.getTarget(), recursive);	
				}
			}
		}
		catch (CommandParseException e) {
			System.out.println("syntax error"); 
		}
	}
	
}
