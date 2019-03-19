package edu.tamu.aser.runtime;

import java.util.HashMap;
import java.util.Vector;

//import com.sun.java.util.jar.pack.Package.Class.Field;
import java.lang.reflect.Field;

//import edu.tamu.aser.instrumentation.RVConfig;
import edu.tamu.aser.graph.Queue;
import edu.tamu.aser.instrumentation.Instrumentor;
import edu.tamu.aser.trace.AbstractNode;
import edu.tamu.aser.trace.JoinNode;
import edu.tamu.aser.trace.LockNode;
import edu.tamu.aser.trace.NotifyNode;
import edu.tamu.aser.trace.ReadNode;
import edu.tamu.aser.trace.StartNode;
import edu.tamu.aser.trace.Trace;
import edu.tamu.aser.trace.UnlockNode;
import edu.tamu.aser.trace.WaitNode;
import edu.tamu.aser.trace.WriteNode;
import edu.tamu.aser.reex.Scheduler;
import edu.tamu.aser.scheduling.strategy.MCRStrategy;

public class  RVRunTime {

    private static final String mode = Instrumentor.memModel;

	/**
	 * createing hashmap to use as a buffer 
	 * @Alan
	 */
    public static HashMap<Long, Queue<Vector<String>>> 
            storeBuffer = new HashMap<Long, Queue<Vector<String>>>();
    
    public static HashMap<Long, Long>
            tidNameMap = new HashMap<Long, Long>();
    
    //for PSO
    public static HashMap<Long, HashMap<String, Queue<Vector<String>>>>
            storeBufferPso = new HashMap<Long, HashMap<String, Queue<Vector<String>>>>();
    //end 
    
    public static HashMap<Long, String> threadTidNameMap;
	public static HashMap<Long, Integer> threadTidIndexMap;
	final static String MAIN_NAME = "0";
	public static long globalEventID;
    public static int currentIndex = 0;
    
    public static Vector<String> failure_trace = new Vector<String>();

    private static HashMap<Integer, Object> staticObjects= new HashMap<Integer,Object>();

    private static Object getObject(int SID)
    {
        Object o = staticObjects.get(SID);
        if(o==null)
        {
            o = new Object();
            staticObjects.put(SID,o);
        }
        return o;
    }

	public static void init() {
		long tid = Thread.currentThread().getId();
		threadTidNameMap = new HashMap<Long, String>();
		threadTidNameMap.put(tid, MAIN_NAME);
		threadTidIndexMap = new HashMap<Long, Integer>();
		threadTidIndexMap.put(tid, 1);
		globalEventID = 0;
	}

	/**
	 * @Alan
	 */
	public static void flush(Queue<Vector<String>> q, int constTid) {
	    
        
	    while(!q.isEmpty()){
            Vector<String> vs = q.peek();
            int SID = Integer.parseInt(vs.get(3));
            
            int currentIndex = RVRunTime.currentIndex;
            String thd_Field = MCRStrategy.schedulePrefix.get(currentIndex);
            String expectField = thd_Field.split("_")[1];
            
            int curTid = Integer.parseInt(thd_Field.split("_")[0]);
            if(curTid != constTid){
                break;
            }
            
            if (SID == Integer.parseInt(expectField)) {  //update
              //String desc = vs.get(0);
              String owner = vs.get(0);
              String name = vs.get(1);
              //int opcode = Integer.parseInt(vs.get(3));
              int ID  = Integer.parseInt(vs.get(2));
              int value = Integer.parseInt(vs.get(4));
              
              Object v = (Integer) value;
              
              try{
                  Class<?> c = Class.forName(owner);
                  Object instance = c.newInstance();
                  //c o = new c();
                  Field f = c.getDeclaredField(name);
                  f.setAccessible(true);
                  f.set(instance, v);
                  
                  logFieldAcc(ID, null, SID, v, true);
                  
              } catch (NoSuchFieldException x) {
                  x.printStackTrace();
              } catch (IllegalAccessException x) {
                  x.printStackTrace();
              } catch (ClassNotFoundException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
              } catch (InstantiationException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
              }
              
              q.dequeue();   //dequue the store buffer
            }
            else{
                break;
            }
        }
       
    }
	
	public static void bufferEmpty(){
	    /*
	     * only when out of the schedule prefix do the flush
	     * since within the prefix, the order is always right
	     */
	    if (currentIndex >= MCRStrategy.schedulePrefix.size()) {
	        if (mode == "TSO") {
	            memBar(Thread.currentThread().getId());
	        }
	        else if (mode == "PSO") {
	            memBarPSO(Thread.currentThread().getId());
	        }
        }
	    
	    
	}
	
	public static void memBarPSO(long tid){
	    if(tidNameMap.get(tid) != null){
            long tidName = tidNameMap.get(tid);
            HashMap<String, Queue<Vector<String>>> mapAddrInsn = storeBufferPso.get(tidName);
            if (mapAddrInsn != null) {
                for(String addr : mapAddrInsn.keySet()){
                    Queue<Vector<String>> q = mapAddrInsn.get(addr);
                    if (q != null) {
                        while(!q.isEmpty()){
                            Vector<String> vs = q.dequeue();                
                            String owner = vs.get(0);
                            String name = vs.get(1);
                          //int opcode = Integer.parseInt(vs.get(3));
                            int SID = Integer.parseInt(vs.get(3));
                          int ID  = Integer.parseInt(vs.get(2));
                          int value = Integer.parseInt(vs.get(4));
                          long threadId = Integer.parseInt(vs.get(5));
                          
                          Object v = (Integer) value;
                          
                          try{
                              Class<?> c = Class.forName(owner);
                              Object instance = c.newInstance();
                              //c o = new c();
                              Field f = c.getDeclaredField(name);
                              f.setAccessible(true);
                              f.set(instance, v);
                              
                              updateFieldAcc(ID, null, SID, v, true, threadId);
                              
                          } catch (NoSuchFieldException x) {
                              x.printStackTrace();
                          } catch (IllegalAccessException x) {
                              x.printStackTrace();
                          } catch (ClassNotFoundException e) {
                              // TODO Auto-generated catch block
                              e.printStackTrace();
                          } catch (InstantiationException e) {
                              // TODO Auto-generated catch block
                              e.printStackTrace();
                          }

                        }    //end while
                    }
                }
            }
            
	    }
	}
	
	//when a sync happens, empty the buffer
    public static void memBar(long tid){  
        if(tidNameMap.get(tid) != null){
            long tidName = tidNameMap.get(tid);
            Queue<Vector<String>> q = storeBuffer.get(tidName);
            if (q != null) {
                while(!q.isEmpty()){
                    Vector<String> vs = q.dequeue();                
                    String owner = vs.get(0);
                    String name = vs.get(1);
                  //int opcode = Integer.parseInt(vs.get(3));
                    int SID = Integer.parseInt(vs.get(3));
                  int ID  = Integer.parseInt(vs.get(2));
                  int value = Integer.parseInt(vs.get(4));
                  long threadId = Integer.parseInt(vs.get(5));
                  
                  Object v = (Integer) value;
                  
                  try{
                      Class<?> c = Class.forName(owner);
                      Object instance = c.newInstance();
                      //c o = new c();
                      Field f = c.getDeclaredField(name);
                      f.setAccessible(true);
                      f.set(instance, v);
                      
                      updateFieldAcc(ID, null, SID, v, true, threadId);
                      
                  } catch (NoSuchFieldException x) {
                      x.printStackTrace();
                  } catch (IllegalAccessException x) {
                      x.printStackTrace();
                  } catch (ClassNotFoundException e) {
                      // TODO Auto-generated catch block
                      e.printStackTrace();
                  } catch (InstantiationException e) {
                      // TODO Auto-generated catch block
                      e.printStackTrace();
                  }

                }    //end while
            }
        }
        //long tid = Thread.currentThread().getId();
       
    }
    
    public static void updateStorePSO(){
        while(MCRStrategy.schedulePrefix.size()>RVRunTime.currentIndex){
            //long tid = Thread.currentThread().getId();
            String s = MCRStrategy.schedulePrefix.get(RVRunTime.currentIndex);
            //might be some problem
            String thdName = s.split("_")[0]; 
            String addr = s.split("_")[1];
            //this is so weird, when type of tid is int, map.get is null
            long tid = Integer.parseInt(thdName);
            
            HashMap<String, Queue<Vector<String>>> mapAddrInsn = storeBufferPso.get(tid);
            
            //flush(q, constTid);
            if(mapAddrInsn == null) break;
            
            if(mapAddrInsn.isEmpty())break;
            
            //String addr = 
            
            Queue<Vector<String>> q = mapAddrInsn.get(addr);
            if (q == null) break;
            if (q.isEmpty()) break;
                
            
            Vector<String> vs = q.dequeue();
            int SID = Integer.parseInt(vs.get(3)); //the SID in the buffer
//            
//            //the SID the schedule indicates:
//            String thd_Field = MCRStrategy.schedulePrefix.get(currentIndex);
//            String expectField = thd_Field.split("_")[1];
//            
//            int field = Integer.parseInt(expectField);
//            
//            if(SID==field){ //update
            String owner = vs.get(0);
            String name = vs.get(1);
            //int opcode = Integer.parseInt(vs.get(3));
            int ID  = Integer.parseInt(vs.get(2));
            int value = Integer.parseInt(vs.get(4));
            
            long threadId = Integer.parseInt(vs.get(5));
            
            Object v = (Integer) value;
            
            try{
                Class<?> c = Class.forName(owner);
                Object instance = c.newInstance();
                //c o = new c();
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                f.set(instance, v);
                
                updateFieldAcc(ID, null, SID, v, true, threadId);
                
            } catch (NoSuchFieldException x) {
                x.printStackTrace();
            } catch (IllegalAccessException x) {
                x.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
                
//                q.dequeue();   //dequue the store buffer
//            }
//            else
//                break;
            
                
            
            }
    }
    
    public static boolean isInteger( String input ) {
        try {
            Integer.parseInt( input );
            return true;
        }
        catch( Exception e ) {
            return false;
        }
    }

    
    public static void updateStore(){
      //comparing the field with the SID of the variable in the head of the Queue
        
        while(MCRStrategy.schedulePrefix.size()>RVRunTime.currentIndex){
            //long tid = Thread.currentThread().getId();
            String s = MCRStrategy.schedulePrefix.get(RVRunTime.currentIndex);
            s = s.split("_")[0]; 
            
            //this is so weird, when type of tid is int, map.get is null
            long tid = Integer.parseInt(s);
            
            Queue<Vector<String>> q = storeBuffer.get(tid);
            
            //flush(q, constTid);
            if(q == null) break;
            
            if(q.isEmpty())break;
            Vector<String> vs = q.peek();
            int SID = Integer.parseInt(vs.get(3)); //the SID in the buffer
            
            //the SID the schedule indicates:
            String thd_Field = MCRStrategy.schedulePrefix.get(currentIndex);
            String expectField = thd_Field.split("_")[1];
            
            //it might not be an integer, e.g. LOCK
            //judge first
            int field = 0;
            if (isInteger(expectField)) {
                field = Integer.parseInt(expectField);
            }
            else
                break;
            
            
            if(SID==field){ //update
                String owner = vs.get(0);
                String name = vs.get(1);
                //int opcode = Integer.parseInt(vs.get(3));
                int ID  = Integer.parseInt(vs.get(2));
                int value = Integer.parseInt(vs.get(4));
                
                long threadId = Integer.parseInt(vs.get(5));
                
                Object v = (Integer) value;
                
                try{
                    Class<?> c = Class.forName(owner);
                    Object instance = c.newInstance();
                    //c o = new c();
                    Field f = c.getDeclaredField(name);
                    f.setAccessible(true);
                    f.set(instance, v);
                    
                    //log operation
                    updateFieldAcc(ID, null, SID, v, true, threadId);
                    
                } catch (NoSuchFieldException x) {
                    x.printStackTrace();
                } catch (IllegalAccessException x) {
                    x.printStackTrace();
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                q.dequeue();   //dequue the store buffer
            }
            else
                break;
            
                
            
            }
    }
        
    
    //update the buffer
//    public static void updateStore(){
//        //comparing the field with the SID of the variable in the head of the Queue
//        
//        if(MCRStrategy.schedulePrefix.size()>RVRunTime.currentIndex){
//            long tid = Thread.currentThread().getId();
//
//            String s = MCRStrategy.schedulePrefix.get(RVRunTime.currentIndex-1);
//            s = s.split("_")[0];
//            int constTid = Integer.parseInt(s);
//            
//            Queue<Vector<String>> q = storeBuffer.get(tid);
//            
//            if(q!=null)
//                flush(q, constTid);
//        }
//        
//        
//    }
    
    //pso buffer store
    public static void bufferStorePSO(final Object v, String info){
        String[] split = info.split(":");
        
        String owner = split[0].replace("/", ".");
        String name = split[1];
        String ID = split[2];
        String SID = split[3];
        
        String value = Integer.toString((Integer) v);

        int id = Integer.parseInt(ID);
        int sid = Integer.parseInt(SID);
        
        if(MCRStrategy.schedulePrefix.size() <= RVRunTime.currentIndex){
            //before executing, need to empty the buffer, 
            //otherwise a reordering of w-w might happen
              
              memBar(Thread.currentThread().getId());
              
            //the initial run just the same as the running under SC    
              try{
                  Class<?> c = Class.forName(owner);
                  Object instance = c.newInstance();
                  //c o = new c();
                  Field f = c.getDeclaredField(name);
                  f.setAccessible(true);
                  f.set(instance, v);
                  
                  logFieldAcc(id, null, sid, v, true);
                  
              } catch (NoSuchFieldException x) {
                  x.printStackTrace();
              } catch (IllegalAccessException x) {
                  x.printStackTrace();
              } catch (ClassNotFoundException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
              } catch (InstantiationException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
              }
                          
               
          }
          else {
              /**
               * compare the field to decide buffer the store or not
               */         
              int currentIndex = RVRunTime.currentIndex;
              String thd_Field = MCRStrategy.schedulePrefix.get(currentIndex);
              String expectField = thd_Field.split("_")[1];
              
              //System.out.println(expectField);
              int field = 0;
              try {
                  field = Integer.parseInt(expectField);
              } catch (Exception e) {
              }
              //int field = Integer.parseInt(expectField);
              
              //execute directly
              
              if(field == sid){  
                  try{
                      Class<?> c = Class.forName(owner);
                      Object instance = c.newInstance();
                      //c o = new c();
                      Field f = c.getDeclaredField(name);
                      f.setAccessible(true);
                      f.set(instance, v);  //put value to field
                      
                      logFieldAcc(id, null, sid, v, true);
                      
                      updateStorePSO();  //after a write, we should check updating
                      
                  } catch (NoSuchFieldException x) {
                      x.printStackTrace();
                  } catch (IllegalAccessException x) {
                      x.printStackTrace();
                  } catch (ClassNotFoundException e) {
                      // TODO Auto-generated catch block
                      e.printStackTrace();
                  } catch (InstantiationException e) {
                      // TODO Auto-generated catch block
                      e.printStackTrace();
                  }
              }
              else{  //buffer the store
                  
                  Vector<String> insnInfo = new Vector<String>();
                  insnInfo.add(owner);
                  insnInfo.add(name);
                  insnInfo.add(ID);
                  insnInfo.add(SID);
                  insnInfo.add(value);
                  
                  long threadId = Thread.currentThread().getId();
                  insnInfo.add(Integer.toString((int) threadId));
                  
                  //long tid = Thread.currentThread().getId();
                  long tid = Integer.parseInt(thd_Field.split("_")[0]);
               
                  if(storeBufferPso.get(tid)==null){
                      HashMap<String, Queue<Vector<String>>> mapAddrInfo = new HashMap<String, Queue<Vector<String>>>();
                      //if()    
                      Queue<Vector<String>> q = new Queue<Vector<String>>();
                      q.enqueue(insnInfo);
                      mapAddrInfo.put(SID, q);
                      storeBufferPso.put(tid, mapAddrInfo);
                  }
                  else{
                      HashMap<String, Queue<Vector<String>>> mapAddrInfo = storeBufferPso.get(tid);
                      if(mapAddrInfo.get(SID) == null){
                          Queue<Vector<String>> q = new Queue<Vector<String>>();
                          q.enqueue(insnInfo);
                          mapAddrInfo.put(SID, q);
                          //storeBufferPso.put(tid, mapAddrInfo);
                      }
                      else {
                          mapAddrInfo.get(SID).enqueue(insnInfo);
                      }
                      //storeBuffer.get(tid).enqueue(insnInfo);
                  }
                  
                  //store the tia-name map
                  if(tidNameMap.get(threadId)==null){
                      tidNameMap.put(threadId, tid);
                  }
              }
          }
        
        
    }
    
    //buffer the store
    public static void bufferStore(final Object v, String info) throws IllegalArgumentException, IllegalAccessException {
        
        //boolean shouldBuffer = true;
        
        String[] split = info.split(":");
        
        String owner = split[0].replace("/", ".");
        String name = split[1];
        String ID = split[2];
        String SID = split[3];
        
        String value = Integer.toString((Integer) v);
        
        int id = Integer.parseInt(ID);
        int sid = Integer.parseInt(SID);
        
        if(MCRStrategy.schedulePrefix.size() <= RVRunTime.currentIndex){
          //before executing, need to empty the buffer, 
          //otherwise a reordering of w-w might happen
            
            memBar(Thread.currentThread().getId());
            
          //the initial run just the same as the running under SC    
            try{
                Class<?> c = Class.forName(owner);
                Object instance = c.newInstance();
                //c o = new c();
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                f.set(instance, v);
                
                logFieldAcc(id, null, sid, v, true);
                
            } catch (NoSuchFieldException x) {
                x.printStackTrace();
            } catch (IllegalAccessException x) {
                x.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
                        
             
        }
        else {
            /**
             * compare the field to decide buffer the store or not
             */
                  
            int currentIndex = RVRunTime.currentIndex;
            String thd_Field = MCRStrategy.schedulePrefix.get(currentIndex);
            String expectField = thd_Field.split("_")[1];
            
            //System.out.println(expectField);
            int field = 0;
            try {
                field = Integer.parseInt(expectField);
            } catch (Exception e) {
            }
            
            
            //execute directly
            
            if(field == sid){  
                try{
                    Class<?> c = Class.forName(owner);
                    Object instance = c.newInstance();
                    //c o = new c();
                    Field f = c.getDeclaredField(name);
                    f.setAccessible(true);
                    f.set(instance, v);
                    
                    logFieldAcc(id, null, sid, v, true);
                    
                    updateStore();
                    
                } catch (NoSuchFieldException x) {
                    x.printStackTrace();
                } catch (IllegalAccessException x) {
                    x.printStackTrace();
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else{  //buffer the store
                
                Vector<String> insnInfo = new Vector<String>();
                insnInfo.add(owner);
                insnInfo.add(name);
                insnInfo.add(ID);
                insnInfo.add(SID);
                insnInfo.add(value);
                
                long threadId = Thread.currentThread().getId();
                insnInfo.add(Integer.toString((int) threadId));
                
                //long tid = Thread.currentThread().getId();
                long tid = Integer.parseInt(thd_Field.split("_")[0]);
                if(storeBuffer.get(tid)==null){
                    Queue<Vector<String>> q = new Queue<Vector<String>>();
                    q.enqueue(insnInfo);
                    storeBuffer.put(tid, q);
                }
                else{
                    storeBuffer.get(tid).enqueue(insnInfo);
                }
                
                //store the tia-name map
                if(tidNameMap.get(threadId)==null){
                    tidNameMap.put(threadId, tid);
                }
            }
        }

        //System.out.println(InsnInfo);     
    }
	
	/**
	 * end
	 */
    
    //Considering that after a read/write operation
    //we can update the store in other threads
    //this will make a mistake
    public static void updateFieldAcc(int ID, final Object o, int SID,
            final Object v, final boolean write, long tid) {

        //Scheduler.beforeFieldAccess(!write, "owner", "name", "desc");

        Trace trace = MCRStrategy.getTrace();
        // Use <= instead of < because currentIndex is increased after this
        // function call
        if ( MCRStrategy.schedulePrefix.size() <= currentIndex++|| MCRStrategy.fullTrace) {
            // Already reached the end of prefix

            //Alan
            StackTraceElement frame = Thread.currentThread().getStackTrace()[2];
            String fileName = frame.getFileName();
            int line = frame.getLineNumber();
            String label = fileName+":"+Integer.toString(line);
            
            globalEventID++;
            if (isPrim(v)) {
                if (write) {
                    WriteNode writeNode = new WriteNode(globalEventID, tid,
                              ID, o == null ? "." + SID
                            : System.identityHashCode(o) + "." + SID, v + "",
                            AbstractNode.TYPE.WRITE,
                            label);
                    trace.addRawNode(writeNode);
                    // db.saveEventToDB(tid, ID,
                    // o==null?"."+SID:hashcode_o+"."+SID,
                    // isPrim(v)?v+"":System.identityHashCode(v)+"_",
                    // write?db.tracetypetable[2]: db.tracetypetable[1]);
                } else {              
                    ReadNode readNode = new ReadNode(globalEventID, tid,
                             ID, o == null ? "." + SID
                            : System.identityHashCode(o) + "." + SID, v + "",
                            AbstractNode.TYPE.READ,
                            label);
                    trace.addRawNode(readNode);
                }
            } else {
                if (write) {
                    WriteNode writeNode = new WriteNode(globalEventID, tid,
                             ID, o == null ? "_."
                            + SID : System.identityHashCode(o) + "_." + SID,
                            System.identityHashCode(v) + "_",
                            AbstractNode.TYPE.WRITE,
                            label);
                    trace.addRawNode(writeNode);
                    // db.saveEventToDB(tid, ID,
                    // o==null?"_."+SID:hashcode_o+"_."+SID,
                    // isPrim(v)?v+"":System.identityHashCode(v)+"_",
                    // write?db.tracetypetable[2]: db.tracetypetable[1]);
                } else {
                    
                 
                    
                    ReadNode readNode = new ReadNode(globalEventID, tid,
                            ID, o == null ? "_."
                            + SID : System.identityHashCode(o) + "_." + SID,
                            System.identityHashCode(v) + "_",
                            AbstractNode.TYPE.READ,
                            label);
                    trace.addRawNode(readNode);
                }
            }

        } else {
            // Not added to trace but update initial memory write.
            if (write) {
                if (isPrim(v)) {
                    
                    trace.updateInitWriteValueToAddress(o == null ? "." + SID
                            : System.identityHashCode(o) + "." + SID, v + "");
                } else {
                    trace.updateInitWriteValueToAddress(o == null ? "_." + SID
                            : System.identityHashCode(o) + "_." + SID,
                            System.identityHashCode(v) + "_");
                }
            }
        }
    }
    
	
	/**
	 * 
	 * @param ID
	 * @param o
	 * @param SID
	 * @param v
	 * @param write
	 */
	public static void logFieldAcc(int ID, final Object o, int SID, final Object v, final boolean write) {

		Trace trace = MCRStrategy.getTrace();
		
		StackTraceElement frame = Thread.currentThread().getStackTrace()[2];
        String fileName = frame.getFileName();
        int line = frame.getLineNumber();
        String label = fileName+":"+Integer.toString(line);
        
        String threadName = Thread.currentThread().getName().toString();
        String type = null;
        if (write) {
            type="write";
        }
        else
            type = "read";
        
        RVRunTime.failure_trace.add(threadName + "_" + label + ":" + type);
	
//        RVRunTime.failure_trace.add(label);
		
		// Use <= instead of < because currentIndex is increased after this
		// function call
		if ( MCRStrategy.schedulePrefix.size() <= currentIndex++|| MCRStrategy.fullTrace) 
		{
//		    StackTraceElement frame = Thread.currentThread().getStackTrace()[2];
//	        String fileName = frame.getFileName();
//	        int line = frame.getLineNumber();
//	        String label = fileName+":"+Integer.toString(line);
	        
		    
			globalEventID++;
			if (isPrim(v)) 
			{
				if (write) 
				{
					WriteNode writeNode = new WriteNode(globalEventID, Thread.currentThread().getId(), ID, 
					        o == null ? "." + SID: System.identityHashCode(o) + "." + SID, 
					        v + "",AbstractNode.TYPE.WRITE,label);
					trace.addRawNode(writeNode);

				} else {	    
					ReadNode readNode = new ReadNode(globalEventID, 
					        Thread.currentThread().getId(), ID, o == null ? "." + SID
							: System.identityHashCode(o) + "." + SID, v + "",
							AbstractNode.TYPE.READ,
							label);
					trace.addRawNode(readNode);
//					if (o==null)
//					    System.out.println(readNode.toString());
				}
			} 
			else {
				if (write) {
					WriteNode writeNode = new WriteNode(globalEventID, 
					        Thread.currentThread().getId(), 
					        ID, 
					        o == null ? "_."+ SID : System.identityHashCode(o) + "_." + SID,
							System.identityHashCode(v) + "_",
							AbstractNode.TYPE.WRITE,
							label);
					trace.addRawNode(writeNode);
					// db.saveEventToDB(tid, ID,
					// o==null?"_."+SID:hashcode_o+"_."+SID,
					// isPrim(v)?v+"":System.identityHashCode(v)+"_",
					// write?db.tracetypetable[2]: db.tracetypetable[1]);
				} else {
					ReadNode readNode = new ReadNode(
					        globalEventID,              //index of this event in the trace
					        Thread.currentThread().getId(),
					        ID,               //id of the variable
					        o == null ? "_."+ SID : System.identityHashCode(o) + "_." + SID,   //addr
							System.identityHashCode(v) + "_",           //value
							AbstractNode.TYPE.READ,
							label);
					trace.addRawNode(readNode);
//					System.out.println(readNode.toString());
				}
			}

		} 
		else {
			// Not added to trace but update initial memory write.
			if (write) {
				if (isPrim(v)) {
				    
					trace.updateInitWriteValueToAddress(o == null ? "." + SID
							: System.identityHashCode(o) + "." + SID, v + "");
				} else {
					trace.updateInitWriteValueToAddress(o == null ? "_." + SID
							: System.identityHashCode(o) + "_." + SID,
							System.identityHashCode(v) + "_");
				}
			}
		}
	}

	public static void logInitialWrite(int ID, final Object o, int SID, final Object v) 
	{

//	    while (MCRStrategy.getTrace() == null);
	    
//	    System.out.println("====================\n\n");
//	    StackTraceElement []aElements = Thread.currentThread().getStackTrace();
//	    for (int i = 0; i < aElements.length; i++) {
//	        System.err.println(aElements[i].toString());
//        }
	    

	    Trace trace = MCRStrategy.getTrace();
	    if (trace ==null) {
	        //when the tested class contains some initializations
	        //the trace is null when mcr is run from the terminal
            return;
        }
//	    while(trace == null){
//	        try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//	        Thread.yield();
//	        trace = MCRStrategy.getTrace();
//	    }
	    if (isPrim(v)) {
            
            trace.updateInitWriteValueToAddress(o == null ? "." + SID
                    : System.identityHashCode(o) + "." + SID, v + "");
        } else {
//            String addr = o == null ? "_." + SID : System.identityHashCode(o) + "_." + SID;
//            String value = System.identityHashCode(v) + "_";
            trace.updateInitWriteValueToAddress(o == null ? "_." + SID
                    : System.identityHashCode(o) + "_." + SID,
                    System.identityHashCode(v) + "_");
        }
	}

	public static void logBranch(int ID) {
	}

	public static void logThreadBegin()
	{
	    Scheduler.beginThread();	  
	    
	}
    public static void logThreadEnd()
    {
        Scheduler.endThread();
    }
	/**
	 * When starting a new thread, a consistent unique identifier of the thread
	 * is created, and stored into a map with the thread id as the key. The
	 * unique identifier, i.e, name, is a concatenation of the name of the
	 * parent thread with the order of children threads forked by the parent
	 * thread.
	 *
	 * @param ID
	 * @param o
	 */
	public static void logBeforeStart(int ID, final Object o) {


	       Scheduler.beforeForking((Thread) o);


		// long tid = Thread.currentThread().getId();
		// Thread t = (Thread) o;
		// long tid_t = t.getId();
		//
		// String name = threadTidNameMap.get(tid);
		// // it's possible that name is NULL, because this thread is started
		// from
		// // library: e.g., AWT-EventQueue-0
		// if (name == null) {
		// name = Thread.currentThread().getName();
		// threadTidIndexMap.put(tid, 1);
		// threadTidNameMap.put(tid, name);
		// }
		//
		// int index = threadTidIndexMap.get(tid);
		//
		// if (name.equals(MAIN_NAME))
		// name = "" + index;
		// else
		// name = name + "." + index;
		//
		// threadTidNameMap.put(tid_t, name);
		// threadTidIndexMap.put(tid_t, 1);
		//
		// index++;
		// threadTidIndexMap.put(tid, index);
		//
		// db.saveThreadTidNameToDB(tid_t, name);
		//
		// db.saveEventToDB(tid, ID, "" + tid_t, "", db.tracetypetable[7]);

		long tid = Thread.currentThread().getId();
		Thread t = (Thread) o;
		long tid_t = t.getId();

		String name = threadTidNameMap.get(tid);
		// it's possible that name is NULL, because this thread is started from
		// library: e.g., AWT-EventQueue-0
		if (name == null) {
			name = Thread.currentThread().getName();
			threadTidIndexMap.put(tid, 1);
			threadTidNameMap.put(tid, name);
		}

		int index = threadTidIndexMap.get(tid);

		if (name.equals(MAIN_NAME))
			name = "" + index;
		else
			name = name + "." + index;

		threadTidNameMap.put(tid_t, name);
		threadTidIndexMap.put(tid_t, 1);

		index++;
		threadTidIndexMap.put(tid, index);

		// db.saveEventToDB(tid, ID, ""+tid_t, "", db.tracetypetable[7]);
		StackTraceElement frame = Thread.currentThread().getStackTrace()[2];
        String fileName = frame.getFileName();
        int line = frame.getLineNumber();
        String label = fileName+":"+Integer.toString(line);
        
        String threadName = Thread.currentThread().getName().toString();
        
        RVRunTime.failure_trace.add(threadName + "_" + label + ":start");
		

        if (MCRStrategy.schedulePrefix.size() <= currentIndex++ || MCRStrategy.fullTrace) {
            Trace trace = MCRStrategy.getTrace();
            globalEventID++;
            StartNode startNode = new StartNode(globalEventID, tid, ID, ""
                    + tid_t, AbstractNode.TYPE.START);
            trace.addRawNode(startNode);
        }
	}

	public static void logAfterStart(int ID, final Object o) {
		Scheduler.afterForking((Thread) o);

		long tid = Thread.currentThread().getId();
		Thread t = (Thread) o;
		long tid_t = t.getId();
		
		StackTraceElement frame = Thread.currentThread().getStackTrace()[2];
        String fileName = frame.getFileName();
        int line = frame.getLineNumber();
        String label = fileName+":"+Integer.toString(line);
        
        String threadName = Thread.currentThread().getName().toString();
        
        RVRunTime.failure_trace.add(threadName + "_" + label + ":after_start");
		
		if (MCRStrategy.schedulePrefix.size() <= currentIndex++ || MCRStrategy.fullTrace) {
			Trace trace = MCRStrategy.getTrace();
			globalEventID++;
			StartNode startNode = new StartNode(globalEventID, tid, ID, ""
					+ tid_t, AbstractNode.TYPE.START);
			trace.addRawNode(startNode);
		}
	}

	public static void logSleep()
	{
	       try {
	            Scheduler.performSleep();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	}
	public static void logJoin(int ID, final Object o) {
		// db.saveEventToDB(Thread.currentThread().getId(), ID, "" + ((Thread)
		// o).getId(), "", db.tracetypetable[8]);
	    
	    StackTraceElement frame = Thread.currentThread().getStackTrace()[2];
        String fileName = frame.getFileName();
        int line = frame.getLineNumber();
        String label = fileName+":"+Integer.toString(line);
        
        String threadName = Thread.currentThread().getName().toString();
        
        

		try {
			Scheduler.performJoin((Thread) o);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		RVRunTime.failure_trace.add(threadName + "_" + label + ":thread_join");
		
		if (mode == "TSO") {
		    //first empty the buffer of current thread 
		    memBar(Thread.currentThread().getId());
		    
            memBar(((Thread) o).getId());
        }
		else if (mode == "PSO") {
		    memBarPSO(Thread.currentThread().getId());
            
            memBarPSO(((Thread) o).getId());
        }

		// if (MCRStrategy.schedulePrefix.size() <
		// MCRStrategy.choicesMade
		// .size()) {
		if (MCRStrategy.schedulePrefix.size() <= currentIndex++ || MCRStrategy.fullTrace) {
			Trace trace = MCRStrategy.getTrace();
			globalEventID++;
			JoinNode joinNode = new JoinNode(globalEventID, Thread
					.currentThread().getId(), ID, "" + ((Thread) o).getId(),
					AbstractNode.TYPE.JOIN);
			trace.addRawNode(joinNode);
		}
	}

	//the original version
	public static void logWait(int ID, final Object o) {
	    
//	    int wait = 0;
//        try {
//            wait = Scheduler.performOnlyWait(o);       //wait for notify
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
	    
		if (MCRStrategy.schedulePrefix.size() <= currentIndex || MCRStrategy.fullTrace) {
			Trace trace = MCRStrategy.getTrace();
			globalEventID++;
			WaitNode waitNode = new WaitNode(globalEventID, Thread
					.currentThread().getId(), ID, ""
					+ System.identityHashCode(o), AbstractNode.TYPE.WAIT);
			trace.addRawNode(waitNode);
		}
		
		StackTraceElement frame = Thread.currentThread().getStackTrace()[2];
        String fileName = frame.getFileName();
        int line = frame.getLineNumber();
        String label = fileName+":"+Integer.toString(line);
        
        String threadName = Thread.currentThread().getName().toString();
        
        RVRunTime.failure_trace.add(threadName + "_" + label + ":wait");

		int wait = 0;
		try {
			wait = Scheduler.performOnlyWait(o);       //wait for notify
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		currentIndex++;
//		System.err.println("wait be notified");
		
//		System.err.println(" after perform only wait");

		/*
		 * there is a problem here
		 * what if another lock event gets this lock first???
		 * Because this lock is a invisible lock when it first executed
		 */
//		System.err.println(RVRunTime.currentIndex);
		Scheduler.performLock(o, wait);  //acquire the release signal
//		System.err.println(RVRunTime.currentIndex);
		
//		if (Configuration.DEBUG) {
//            System.err.println("Log the lock after the wait!");
//        }
		
		if (MCRStrategy.schedulePrefix.size() <= currentIndex++ || MCRStrategy.fullTrace) {
			Trace trace = MCRStrategy.getTrace();
			globalEventID++;
			LockNode lockNode = new LockNode(globalEventID, Thread
					.currentThread().getId(), ID, ""
					+ System.identityHashCode(o), AbstractNode.TYPE.LOCK);
			trace.addRawNode(lockNode);
		}
	}

	public static void logNotify(int ID, final Object o) {
	    
	    
		long notifiedThreadId = Scheduler.performNotify(o);
		
		StackTraceElement frame = Thread.currentThread().getStackTrace()[2];
        String fileName = frame.getFileName();
        int line = frame.getLineNumber();
        String label = fileName+":"+Integer.toString(line);
        
        String threadName = Thread.currentThread().getName().toString();
        
        RVRunTime.failure_trace.add(threadName + "_" + label + ":notify");
//		System.err.println("notify " + RVRunTime.currentIndex);

		if (MCRStrategy.schedulePrefix.size() <= currentIndex++ || MCRStrategy.fullTrace) {
			Trace trace = MCRStrategy.getTrace();
			globalEventID++;
			NotifyNode notifyNode = new NotifyNode(globalEventID, Thread
					.currentThread().getId(), ID, ""
					+ System.identityHashCode(o), notifiedThreadId, AbstractNode.TYPE.NOTIFY);
			trace.addRawNode(notifyNode);
		}
	}

	public static void logNotifyAll(int ID, final Object o) {

	    long notifiedThreadId = Scheduler.performNotifyAll(o);
	    
	    addEventToTrace();

		if (MCRStrategy.schedulePrefix.size() <= currentIndex++ || MCRStrategy.fullTrace) {
			Trace trace = MCRStrategy.getTrace();
			globalEventID++;
			//TODO: Check how to do for notifyAll
			NotifyNode notifyNode = new NotifyNode(globalEventID, 
			        Thread.currentThread().getId(), 
			        ID, 
			        ""+ System.identityHashCode(o), 
			        notifiedThreadId, 
			        AbstractNode.TYPE.NOTIFY);
			trace.addRawNode(notifyNode);
		}
	}

	public static void logStaticSyncLock(int ID, int SID) {

        Scheduler.performLock(getObject(SID));
        
        StackTraceElement frame = Thread.currentThread().getStackTrace()[2];
        String fileName = frame.getFileName();
        int line = frame.getLineNumber();
        String label = fileName+":"+Integer.toString(line);
        
        String threadName = Thread.currentThread().getName().toString();
        
        RVRunTime.failure_trace.add(threadName + "_" + label + ":StaticSyncLock");

		if (MCRStrategy.schedulePrefix.size() <= currentIndex++ || MCRStrategy.fullTrace) {
			Trace trace = MCRStrategy.getTrace();
			globalEventID++;
			LockNode lockNode = new LockNode(globalEventID, Thread
					.currentThread().getId(), ID, "" + SID,
					AbstractNode.TYPE.LOCK);
			trace.addRawNode(lockNode);
		}
	}

	public static void logStaticSyncUnlock(int ID, int SID) {

        Scheduler.performUnlock(getObject(SID));
        
        StackTraceElement frame = Thread.currentThread().getStackTrace()[2];
        String fileName = frame.getFileName();
        int line = frame.getLineNumber();
        String label = fileName+":"+Integer.toString(line);
        
        String threadName = Thread.currentThread().getName().toString();
        
        RVRunTime.failure_trace.add(threadName + "_" + label + ":StaticSyncUnlock");
        
		if (MCRStrategy.schedulePrefix.size() <= currentIndex++ || MCRStrategy.fullTrace) {
			Trace trace = MCRStrategy.getTrace();
			globalEventID++;
			UnlockNode unlockNode = new UnlockNode(globalEventID, Thread
					.currentThread().getId(), ID, "" + SID,
					AbstractNode.TYPE.UNLOCK);
			trace.addRawNode(unlockNode);
		}
	}
	
	/**
	 * log the lock events
	 * @param ID
	 * @param lock
	 */
	
	public static void logLock(int ID, final Object lock)
	{
	    //why this blocks the thread if the thread can acquire the lock???
	    Scheduler.performLock(lock);

	    StackTraceElement frame = Thread.currentThread().getStackTrace()[2];
        String fileName = frame.getFileName();
        int line = frame.getLineNumber();
        String label = fileName+":"+Integer.toString(line);
        
        String threadName = Thread.currentThread().getName().toString();
        
        RVRunTime.failure_trace.add(threadName + "_" + label + ":Lock");
	    
//	    if (Configuration.DEBUG) {
//            System.err.println("Log the lock by thread: "+ Thread.currentThread().getId());
//        }

        if (MCRStrategy.schedulePrefix.size() <= currentIndex++ || MCRStrategy.fullTrace) {
            Trace trace = MCRStrategy.getTrace();
            globalEventID++;
            LockNode lockNode = new LockNode(globalEventID, Thread
                    .currentThread().getId(), ID, ""
                    + System.identityHashCode(lock), AbstractNode.TYPE.LOCK);
            trace.addRawNode(lockNode);
        }
	}
	public static void logUnlock(int ID, final Object lock)
	{
	      Scheduler.performUnlock(lock);
	      
	      StackTraceElement frame = Thread.currentThread().getStackTrace()[2];
	        String fileName = frame.getFileName();
	        int line = frame.getLineNumber();
	        String label = fileName+":"+Integer.toString(line);
	        
	        String threadName = Thread.currentThread().getName().toString();
	        
	        RVRunTime.failure_trace.add(threadName + "_" + label + ":UnLock");
	       
//	       if (Configuration.DEBUG) {
//            System.err.println("log unlock by thread: "+ Thread.currentThread().getId());
//	       }

        if (MCRStrategy.schedulePrefix.size() <= currentIndex++ || MCRStrategy.fullTrace) {
            Trace trace = MCRStrategy.getTrace();
            globalEventID++;
            UnlockNode unlockNode = new UnlockNode(globalEventID, Thread
                    .currentThread().getId(), ID, ""
                    + System.identityHashCode(lock), AbstractNode.TYPE.UNLOCK);
            trace.addRawNode(unlockNode);
        }
	}

	public static void logArrayAcc(int ID, final Object o, int index,
			final Object v, final boolean write) {

	       //Scheduler.beforeArrayAccess(!write);
		Trace trace = MCRStrategy.getTrace();
        String label = addEventToTrace();
       
		if (MCRStrategy.schedulePrefix.size() <= currentIndex++ || MCRStrategy.fullTrace) {
			// Already reached the end of prefix
		    
			globalEventID++;
			if (isPrim(v)) {
				if (write) {
					WriteNode writeNode = new WriteNode(globalEventID, Thread
							.currentThread().getId(), ID,
							System.identityHashCode(o) + "_" + index, v + "",
							AbstractNode.TYPE.WRITE,
							label);
					trace.addRawNode(writeNode);
				} else {
					ReadNode readNode = new ReadNode(globalEventID, Thread
							.currentThread().getId(), ID,
							System.identityHashCode(o) + "_" + index, v + "",
							AbstractNode.TYPE.READ,
							label);
					trace.addRawNode(readNode);
				}
			} else {
				if (write) {
					WriteNode writeNode = new WriteNode(globalEventID, Thread
							.currentThread().getId(), ID,
							System.identityHashCode(o) + "_" + index,
							System.identityHashCode(v) + "_",
							AbstractNode.TYPE.WRITE,
							label);
					trace.addRawNode(writeNode);
				} else {
					ReadNode readNode = new ReadNode(globalEventID, Thread
							.currentThread().getId(), ID,
							System.identityHashCode(o) + "_" + index,
							System.identityHashCode(v) + "_",
							AbstractNode.TYPE.READ,
							label);
					trace.addRawNode(readNode);
				}
			}

		} else {
			// Not added to trace but update initial memory write.
			if (write) {
				if (isPrim(v)) {
					trace.updateInitWriteValueToAddress(
							System.identityHashCode(o) + "_" + index, v + "");
				} else {
					trace.updateInitWriteValueToAddress(
							System.identityHashCode(o) + "_" + index,
							System.identityHashCode(v) + "_");
				}
			}
		}

		// db.saveEventToDB(tid, ID, System.identityHashCode(o)+"_"+index,
		// isPrim(v)?v+"":System.identityHashCode(v)+"_",
		// write?db.tracetypetable[2]: db.tracetypetable[1]);

	}

	private static boolean isPrim(Object o) {
		if (o instanceof Integer || o instanceof Long || o instanceof Byte
				|| o instanceof Boolean || o instanceof Float
				|| o instanceof Double || o instanceof Short
				|| o instanceof Character)
			return true;

		return false;
	}	
	
	//add the event to the trace, so that we can print the trace when a failure happens
	public static String addEventToTrace() {
	    
	    StackTraceElement frame = Thread.currentThread().getStackTrace()[2];
	    String fileName = frame.getFileName();
	    int line = frame.getLineNumber();
	    String label = fileName+":"+Integer.toString(line);
	    
	    String threadName = Thread.currentThread().getName().toString();
	    
	    RVRunTime.failure_trace.add(threadName + "_" + label);
	    
	    return label;
    }
}
