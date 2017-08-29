// RepositoryFinder.java
// $Id: RepositoryFinder.java,v 1.1 2010/06/15 12:21:50 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 2002.
// Please first read the full copyright statement in file COPYRIGHT.html


package org.w3c.tools.offline.browse ;

import org.w3c.tools.resources.serialization.Serializer;
import org.w3c.tools.resources.Resource ;
import org.w3c.tools.resources.serialization.ResourceDescription ;
import org.w3c.tools.resources.serialization.AttributeDescription ;
import org.w3c.tools.resources.serialization.xml.XMLSerializer;

import org.w3c.tools.resources.AttributeHolder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.MalformedPatternException;

import java.util.Hashtable;


/**
 * <p>The finder that handles a given repository file.
*/

public class RepositoryFinder {

	/** 
	 * The relative directory name of the repository in the store 
	 */
	protected 	String 	repository_dir 	= null ;

	/** 
	 * The repository name in the repository directory 
	 */
	protected	String	repository_name = null ;

	/** 
	 * The store directory (relative to current path)
	 */
	protected	String	store_dir 		= null ;

	/** 
	 *  A resource lister instance that handles the repository resources
	 */
	protected 	ResourceLister	rl		= null ;


	protected 	File 	file			= null ;
	protected	Serializer	serializer 	= null ;
	private		Hashtable	containers 	= null ;
	private		AttributeHolder[]	holders 	= null;
	private 	Reader	reader			= null ;
	private 	Writer	writer			= null ;

	protected static Perl5Matcher pmatcher = new Perl5Matcher() ; 
	protected static Perl5Compiler pcompiler = new Perl5Compiler() ;


	/** 
	*  Initializes the finder.
	*  opens the file in the right directory, adds a resource lister 
	*  and a XML serializer, finds the containers.
	* @param storedir the store directory path
	* @param repname the repository filename.
	*/
	public RepositoryFinder(String storedir, String repname) throws InvalidStoreException {

//		System.out.println("I am repository finder for" +repname);
		int slash = repname.indexOf('/',0);
        if (slash != -1){
            this.repository_dir = repname.substring(0, slash);
            this.repository_name = repname.substring(slash+1,repname.length());
		} else {
			this.repository_dir	= "" ;
			this.repository_name	= repname ;
		}
		this.store_dir = storedir ;
		this.rl = new ResourceLister() ;
		this.containers = new Hashtable();

		serializer = 
			new org.w3c.tools.resources.serialization.xml.XMLSerializer();
		File file = new File(store_dir+"/"+repository_dir,repository_name);
		// System.out.println("reading repository "+repository_name+"...\n");

        try {
			this.reader	= new BufferedReader(new FileReader(file)) ;
       		this.holders = serializer.readAttributeHolders(reader);
			for (int i = 0 ; i < holders.length ; i++){
				// rl.printAttributeHolder(holders[i]);
				if ( rl.getKeyFromHolder(holders[i]) != null ){
					// store containers in a dedicated hashtable with key 
					containers.put(rl.getKeyFromHolder(holders[i]),holders[i]);
				}
			}	
        } catch (IOException ex) {
            System.out.println("Unable to read repository");
			throw new InvalidStoreException("inexistent repository");
        }
	}

	/**
	* find a given resource
	* @param identifier the name of the resource to find
	* @return an AttributeHolder.
	*/
	public AttributeHolder getAttributeHolder(String identifier){
		for (int i = 0 ; i < holders.length ; i++){
			if (rl.getIdentFromHolder(holders[i]).compareTo(identifier)==0){
				return holders[i] ;	
			}
		}  	
		return null ;
	}

	public AttributeHolder[] getAttributeHolders(){
		return holders ;	
	}  

	public String getRep(){
		return (repository_dir+"/"+repository_name) ;
	}

	/**
	* write changes into a tmp file and then commit to the real file 
	*/
	public void writeHolders(){
		File tmp =new File(store_dir+"/"+repository_dir,repository_name+".tmp");
		File bak =new File(store_dir+"/"+repository_dir,repository_name+".bak");

		File orig= new File(store_dir+"/"+repository_dir,repository_name);

		try {
			this.writer	= new BufferedWriter(new FileWriter(tmp)) ;
		//	this.writer	= new OutputStreamWriter( out );
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Unable to open temp rep for write");
        }
		try{
			serializer.writeResources(holders, writer);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Unable to write repository");
        }
		
		if ( !orig.renameTo(bak) ) { 
			System.out.println("unable to rename "+orig+" to "+bak); 
		} else {
 			if ( !tmp.renameTo(orig) ) { 
				bak.renameTo(orig) ; 
				System.out.println("unable to rename "+tmp+" to "+orig); 
			} else {
				// System.out.println("Repository saved");
			}
		}

	}
}

