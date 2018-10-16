package edu.tamu.aser.profile;

import edu.tamu.aser.instrumentation.RVConfig;
import edu.tamu.aser.instrumentation.RVGlobalStateForInstrumentation;

import java.util.HashMap;
import java.util.HashSet;

public class ProfileRunTime {

	public static HashSet<Integer> sharedVariableIds;
    public static HashSet<Integer> sharedArrayIds;
    static HashMap<Integer,Long> writeThreadMap;
    static HashMap<Integer,long[]> readThreadMap;
    public static HashMap<Integer,HashSet<Integer>> arrayIdsMap;

    
	static HashMap<Integer,Long> writeThreadArrayMap;
    static HashMap<Integer,long[]> readThreadArrayMap;
    
    static ThreadLocal<HashSet<Integer>> threadLocalIDSet;
    static ThreadLocal<HashSet<Integer>> threadLocalIDSet2;

    
	public static void init() {
		sharedVariableIds = new HashSet<Integer>();
        writeThreadMap = new HashMap<Integer,Long>();
        readThreadMap = new HashMap<Integer,long[]> ();
        
        sharedArrayIds = new HashSet<Integer>();
        arrayIdsMap = new HashMap<Integer,HashSet<Integer>>();
        writeThreadArrayMap = new HashMap<Integer,Long>();
        readThreadArrayMap = new HashMap<Integer,long[]>();
        
        threadLocalIDSet = new ThreadLocal<HashSet<Integer>>() {
			protected HashSet<Integer> initialValue() {
				return new HashSet<Integer>();
    
			}
		};

        threadLocalIDSet2 = new ThreadLocal<HashSet<Integer>>() {
			protected HashSet<Integer> initialValue() {
				return new HashSet<Integer>();
			}
		};

		//add shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				RVGlobalStateForInstrumentation.instance.saveMetaData();
			}
		});

	}

	/**
	   * detect shared variables -- two conditions
	   * 1. the address is accessed by more than two threads 
	   * 2. at least one of them is a write
	   * @param ID -- shared variable id
	   * @param SID -- field id
	   * @param write or read
	   */
	public static  void logFieldAcc(int ID, int SID, final boolean write) {
		long tid = Thread.currentThread().getId();
	      
		{
			if(!threadLocalIDSet.get().contains(ID))
			{
				if(threadLocalIDSet2.get().contains(ID))
					threadLocalIDSet.get().add(ID);
				else
					threadLocalIDSet2.get().add(ID);
	              
	              
				//o is not used...
	              
				//instance-based approach consumes too much memory
	              
				//String sig = o==null?"."+SID:System.identityHashCode(o)+"."+SID;
	    
				if(RVConfig.instance.verbose)
				{
					String readOrWrite = (write?" write":" read");
					//System.out.println("Thread "+tid+" "+readOrWrite+" variable "+SID);
				}
				if(!sharedVariableIds.contains(SID))
				{
					if(writeThreadMap.containsKey(SID))
					{
						if(writeThreadMap.get(SID)!=tid)
						{
							sharedVariableIds.add(SID);
							return;
						}
					}
	                 
					if(write)//write
					{
						if(readThreadMap.containsKey(SID))
						{
							long[] readThreads = readThreadMap.get(SID);
							if(readThreads!=null
									&&(readThreads[0]!=tid||
									(readThreads[1]>0&&readThreads[1]!=tid)))
							{
								sharedVariableIds.add(SID);
								return;
							}
						}
	                     
						writeThreadMap.put(SID, tid);
	                }
					else//read
					{
						long[] readThreads = readThreadMap.get(SID);
						if(readThreads==null)
						{
							readThreads = new long[2];
							readThreads[0]= tid;
							readThreadMap.put(SID, readThreads);
						}
						else
						{
							if(readThreads[0]!=tid)
								readThreads[1]= tid;
	    
						}
					}
				}
			}
		}
	}
	public static  void logArrayAcc(int ID, final Object o, int index, final boolean write) {
		long tid = Thread.currentThread().getId();
	      
//	    StringBuilder builder = new StringBuilder(20);
//	    builder.append(ID).append('.').append(sig);
//	    String identifier = builder.toString();
	      
	      //String identifier = ID+"."+sig;
	      //System.out.println(identifier);
		if(!threadLocalIDSet.get().contains(ID))
	  	{
		  	if(threadLocalIDSet2.get().contains(ID))
			  	threadLocalIDSet.get().add(ID);
		  	else
			  	threadLocalIDSet2.get().add(ID);

		  	Integer sig = System.identityHashCode(o);//+"_"+index;//array

		  	HashSet<Integer> ids = arrayIdsMap.get(sig);
		  	if(ids==null){
			  	ids = new HashSet<Integer>();
			  	arrayIdsMap.put(sig, ids);
		  	}
		  	ids.add(ID);
		  	if(RVConfig.instance.verbose)
		  	{
			  	String readOrWrite = (write?" write":" read");
				//System.out.println("Thread "+tid+" "+readOrWrite+" array "+RVGlobalStateForInstrumentation.instance.getArrayLocationSig(ID));
		  	}
		 	if(!sharedArrayIds.contains(sig))
		 	{
			 	if(writeThreadArrayMap.containsKey(sig))
			 	{
				 	if(writeThreadArrayMap.get(sig)!=tid)
				 	{
					 	sharedArrayIds.add(sig);
					 	return;
				 	}
			 	}

			 	if(write)//write
			 	{
				 	if(readThreadArrayMap.containsKey(sig))
				 	{
					 	long[] readThreads = readThreadArrayMap.get(sig);
					 	if(readThreads!=null
							 &&(readThreads[0]!=tid||
							 (readThreads[1]>0&&readThreads[1]!=tid)))
					 	{
						 	sharedArrayIds.add(sig);
						 	return;
					 	}
				 	}

				 	writeThreadArrayMap.put(sig, tid);
				}
			 	else//read
			 	{
				 	long[] readThreads = readThreadArrayMap.get(sig);

				 	if(readThreads==null)
				 	{
					 	readThreads = new long[2];
					 	readThreads[0]= tid;
					 	readThreadArrayMap.put(sig, readThreads);
				 	}
				 	else
				 	{
					 	if(readThreads[0]!=tid)
						 	readThreads[1]= tid;

				 	}
			 	}
		 	}
	 	}
  	}

	private static boolean isPrim(Object o) {
		if (o instanceof Integer || o instanceof Long || o instanceof Byte
				|| o instanceof Boolean || o instanceof Float
				|| o instanceof Double || o instanceof Short
				|| o instanceof Character)
			return true;

		return false;
	}
}
