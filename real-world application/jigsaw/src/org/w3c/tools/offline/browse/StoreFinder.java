// StoreFinder.java
// $Id: StoreFinder.java,v 1.1 2010/06/15 12:21:50 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 2002.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.offline.browse ;

import org.w3c.tools.resources.serialization.Serializer;
import org.w3c.tools.resources.Resource ;
import org.w3c.tools.resources.Attribute ;
import org.w3c.tools.resources.serialization.ResourceDescription ;
import org.w3c.tools.resources.serialization.AttributeDescription ;
import org.w3c.tools.resources.serialization.xml.XMLSerializer;
import org.w3c.tools.resources.AttributeHolder;

import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.MalformedPatternException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

import java.lang.String;

import java.util.Hashtable;
import java.util.Stack;


/**
 * <p>The finder that handles a given jigsaw store
 */

public class StoreFinder {

	public static final String ROOT_REP = "root.xml";
	public static final String INDEX_F = "index.xml";

	/**
	 *  The store directory (relative to current path)
	 */ 
	public String store_dir = null ;

	/**
	 * The server name (e.g. http-server) 
	 */ 
	public String server_name = null ;

	/**
	 * A resource lister instance that handles the repository resources 
	 */ 
	protected ResourceLister rl = null ;
	
	protected Serializer serializer = null;

	/* Hashtable that associates key/repository */
	protected Hashtable index_table = new Hashtable();

	/* Hashtable that associates repository/RepositoryFinder */
	protected Hashtable finders = new Hashtable();

	/* Current stack (for recursive actions) */
	private Stack cur_stack = new Stack();

	/* Resource & repository stack */
	private Stack res_stack = new Stack(); // the resource stack
	private Stack rep_stack = new Stack(); // the equivalent repository stack

	/**
	 * The working repository
	 */ 
	private String working_rep = null;

	protected static Perl5Matcher pmatcher = new Perl5Matcher() ;
	protected static Perl5Compiler pcompiler = new Perl5Compiler() ;

	/**
	* Initializes the finder
	*/
	public StoreFinder(String store_dir, String server_name) {
		this.store_dir		= store_dir ;
		this.server_name	= server_name ;
		this.rl = new ResourceLister() ;
	}


	/**
	* Reads the index file of the store.
	* opens the file and initialize index_table, launch one finder for
	* each repository.
	*/
	public void readStoreIndex() throws IOException {
		serializer = 
			new org.w3c.tools.resources.serialization.xml.XMLSerializer();
		File index = new File(store_dir,server_name+"-"+INDEX_F);
		System.out.println("Reading index file...");

		try {
			Reader reader = new BufferedReader(new FileReader(index)) ;
       		AttributeHolder[] atth = serializer.readAttributeHolders(reader);

			for (int i = 0 ; i < atth.length ; i++) {
	//			rl.printAttributeHolder(atth[i]);
				Integer key = rl.getKeyFromHolder(atth[i]);
				if ( key != null ){
					String rep = rl.getRepositoryFromHolder(atth[i]);	
					index_table.put(key,rep);
					if ( ! finders.containsKey(rep) ){
						RepositoryFinder rf = new RepositoryFinder(store_dir,
																rep);
						finders.put(rep,rf);
					}
				}	
        	}
		}
		catch (InvalidStoreException e){
			System.out.println("Invalid store");
			throw new IOException();
		} catch (IOException e){
			System.out.println("can't read index file");
			throw new IOException();
		}
	}


	/**
	* Reads the root file of the store.
	* opens the file and initialize working repository and stacks, 
	* lists all the store recurively.
	*/
	public void readStoreRoot() throws IOException {

		serializer = 
			new org.w3c.tools.resources.serialization.xml.XMLSerializer();
		File root = new File(store_dir,ROOT_REP);

		/* initialize the working repository and stack to root rep */
		working_rep = ROOT_REP ;
		res_stack.push(ROOT_REP);
		RepositoryFinder rf = (RepositoryFinder)finders.get(ROOT_REP);
		rep_stack.push(rf);

		/* read the attribute holders (=resources) */	
		System.out.println("reading root file...\n");
		Reader reader = new BufferedReader(new FileReader(root)) ;
       	AttributeHolder[] atth = serializer.readAttributeHolders(reader);
		for (int i = 0 ; i < atth.length ; i++) {
			// print the resource name
			System.out.println(rl.getIdentFromHolder(atth[i])); 
			Integer key = rl.getKeyFromHolder(atth[i]);
			if (key != null){
				// get the repository where the container is, from the key
				String repository = (String)index_table.get(key);
				//System.out.println("container: "+key+"is in: "+repository);

				// read the container recursively
				if (repository.compareTo(ROOT_REP)!=0){
					recursiveReadContainer(repository,1);
				}
			}
		}
	}


	/**
	* Recursive read of containers 
	* @param repname the repository file name
	* @param deep recursivity depth level.
	*/
	private void recursiveReadContainer(String repname, int deep){
		//	System.out.println("recurse in repository "+repname);
		RepositoryFinder rf = (RepositoryFinder)finders.get(repname);
		AttributeHolder[] atth = rf.getAttributeHolders();
		// calculate display tabbing from depth 
		String depthtab = "";
		for ( int j = 0 ; j < deep ; j++ ){
			depthtab = depthtab+"\t" ;
		}
		// print and recurse if a key is found (container)
		for ( int i = 0 ; i < atth.length ; i++ ){
			System.out.println(depthtab+"|-"+ rl.getIdentFromHolder(atth[i]));
			Integer key = rl.getKeyFromHolder(atth[i]);
			if ( key != null){
				if (index_table.containsKey(key)){
					String chrep = (String)index_table.get(key) ;
					// System.out.println(" recurse for "+key);
					recursiveReadContainer(chrep,deep+1);
				} else {
					System.out.println("WARNING! "
							+ rl.getIdentFromHolder(atth[i])
							+ " is a container but its key "
							+ key + " does not exist in store index");
				}
			}
		} 
	}

	/**
	* Execute an action on the store.
	* launch a try for the given action on the current repository.
	* @param action the action string
	* @param identifier the target of action (a Perl5 regexp)
	* @param recursive a boolean flag (true = recursive action).
	*/
	public void finderAction(String action,String identifier,boolean recursive){
		// System.out.println(action+" "+identifier);
		
		if (working_rep.compareTo(ROOT_REP) == 0){ 
			System.out.println(working_rep);
			tryAction (action, working_rep, identifier,recursive,1);
		} else {
			RepositoryFinder rf = (RepositoryFinder)rep_stack.peek();
			System.out.println(working_rep);
			tryAction (action, rf.getRep(), identifier,recursive,1);
		}
	}

	/**
	* the real try method 
	*/
	private void tryAction(String action, String repname, 
					String identifier,boolean recursive, int deep){
//		System.out.println("tryAction: "+action+"-"+repname +"-"+identifier);

		
		RepositoryFinder rf = (RepositoryFinder)finders.get(repname);
		AttributeHolder[] atth = rf.getAttributeHolders();
		Pattern iPattern = null ;

		boolean toWrite = false ;

		try{
			iPattern = pcompiler.compile("^"+identifier+"$",
											Perl5Compiler.DEFAULT_MASK) ;
			for (int i = 0 ; i < atth.length ; i++) {
				cur_stack.push(rl.getIdentFromHolder(atth[i]));
				if (pmatcher.matches(rl.getIdentFromHolder(atth[i]),iPattern)){
					rl.performAction(action,atth[i],deep);
					rl.performActionOnFrames(action,atth[i],deep);
					toWrite =true ;
					Integer key = rl.getKeyFromHolder(atth[i]);
					if (key != null && recursive == true){
//						System.out.println("recursive perform");
						String chrep = (String)index_table.get(key) ;
						if (chrep.compareTo(ROOT_REP)!=0){
							recursivePerformAction(action,chrep,deep+1);
						}
					} 
//					System.out.println(cur_stack.toString());
				} else {
					Integer key = rl.getKeyFromHolder(atth[i]);
					if (key != null && recursive == true){
						String chrep = (String)index_table.get(key) ;
						if (chrep.compareTo(ROOT_REP)!=0){
//							System.out.println("try in :"+chrep);
							tryAction(action,chrep,identifier,recursive,deep+1);
						}
					}
				}
				cur_stack.pop();
				if (toWrite){
//					System.out.println("write holders in "+rf.getRep());
					rf.writeHolders(); // commit changes
					toWrite =false;
				}
		
			} 
		} catch (MalformedPatternException ex){
			System.out.println ("malformed expression "+identifier);
		}
	}

	/* the real action, called by tryAction when target is found */
	private void recursivePerformAction(String action,
										String repname, int deep){
		// get the RepositoryFinder instance
		RepositoryFinder rf = (RepositoryFinder)finders.get(repname);
		// perform action and recurse
		AttributeHolder[] atth = rf.getAttributeHolders();
		for (int i = 0 ; i < atth.length ; i++) {
			rl.performAction(action,atth[i],deep);
			rl.performActionOnFrames(action,atth[i],deep);
			Integer key = rl.getKeyFromHolder(atth[i]);
			// recursive call if it is a container 
			if (key != null){
				String chrep = (String)index_table.get(key) ;
				if (chrep.compareTo(ROOT_REP)!=0)
					recursivePerformAction(action,chrep,deep+1);
			}
		} 
		// System.out.println("write holders in "+rf.getRep());
		rf.writeHolders(); // commit changes
	}

	/**
	* Get the working repository
	* @return a string that is the repository file name.
	*/
	public String getWorkingRep(){
		return (working_rep) ;
	}

	/**
	* Print the working repository
	*/
	public void printWorkingRep(){
		System.out.println(working_rep);
	}

	/**
	* Set the working repository
	* @param newrep a string that is the container name or ".." to 
	* go back in the stack.
	*/
	public void setWorkingRep(String newrep){
		if (newrep.compareTo("..")==0){
			res_stack.pop();
			rep_stack.pop();
			working_rep = (String)res_stack.peek();
		} else {
			try {
				RepositoryFinder rf = (RepositoryFinder)rep_stack.peek();
				AttributeHolder[] atth = rf.getAttributeHolders();
				for (int i = 0 ; i < atth.length ; i++) {
					Integer key = rl.getKeyFromHolder(atth[i]);
					if (rl.getIdentFromHolder(atth[i]).compareTo(newrep)==0
						 							&& key != null ){
						working_rep = newrep ;
						res_stack.push(rl.getIdentFromHolder(atth[i]));
						String reps = (String)index_table.get(key);
						rep_stack.push((RepositoryFinder)finders.get(reps));
						// System.out.println(res_stack.toString());
					} 
				}
				if (working_rep.compareTo(newrep)!=0){
					System.out.println("container not found or not a container");	
				}
			}
			catch (java.lang.NullPointerException ex){
				ex.printStackTrace();
			}
		}	
	}

}

