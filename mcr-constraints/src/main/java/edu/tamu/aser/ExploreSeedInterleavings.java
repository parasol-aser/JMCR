package edu.tamu.aser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.Map.Entry;
import edu.tamu.aser.config.Configuration;
import edu.tamu.aser.constraints.ConstraintsBuildEngine;
import edu.tamu.aser.trace.AbstractNode;
import edu.tamu.aser.trace.IMemNode;
import edu.tamu.aser.trace.LockNode;
import edu.tamu.aser.trace.ReadNode;
import edu.tamu.aser.trace.Trace;
import edu.tamu.aser.trace.UnlockNode;
import edu.tamu.aser.trace.WriteNode;

/**
 * The MCRTest class implements maximal causal model based systematic
 * testing algorithm based on constraint solving. 
 * The events in the trace are loaded and processed window 
 * by window with a configurable window size. 
 * 
 * @author jeffhuang and shiyou huang
 *
 */
public class ExploreSeedInterleavings {
	private Queue<List<String>> schedules;

	private static int schedule_id;
	public static String output = "#Read, #Constraints, SolvingTime(ms)\n";
    public static HashSet<Object> races = new HashSet<Object>();
	private static boolean isfulltrace =false;
	private static ConstraintsBuildEngine iEngine;
	
	//prefix-setOfEquivalentPrefixes_map
	static HashMap<Vector<String>, Set<Vector<String>>> mapPrefixEquivalent = new HashMap<>();
	static long memUsed = 0;


	 ExploreSeedInterleavings(Queue<List<String>> exploreQueue) {
		this.schedules = exploreQueue;
	}

	/**
	 * Trim the schedule to show the last 100 only entries
	 *
	 */
	private static Vector<String> trim(Vector<String> schedule)
	{
		if(schedule.size()>100)
		{
			Vector<String> s = new Vector<>();
			s.add("...");
			for(int i=schedule.size()-100;i<schedule.size();i++)
				s.add(schedule.get(i));
			return s;
		}
		else
			return schedule;
	}
	
	/**
	 * The method for generating causally different schedules. 
	 * Each schedule enforces a read to be matched with a write that writes
	 * a different value.
	 * @param engine : the engine is for building the constraints and call the solver to solve them
	 * @param trace  : trace collected from the current execution, which only contains the events which appear after the
	 *               prior prefix
	 * @param schedule_prefix : the prefix that leads to this execution
	 */
	private void genereteCausallyDifferentSchedules(ConstraintsBuildEngine engine, Trace trace, Vector<String> schedule_prefix)
	{
		//OMCR
		Vector<HashMap<String, Set<Vector<String>>>> vReadValuePrefixes =
				new Vector<>();
		/*
		 * for each shared variable, find all the reads and writes to this variable
		 * group the writes based on the value written to this variable
		 * consider each read to check if it can see a different value
		 */
		for (String addr : trace.getIndexedThreadReadWriteNodes().keySet()) {

			//the dynamic memory location
			//get the initial value on this address
			final String initVal = trace.getInitialWriteValueMap().get(addr);

			//get all read nodes on the address
			Vector<ReadNode> readnodes = trace.getIndexedReadNodes().get(addr);

			//get all write nodes on the address
			Vector<WriteNode> writenodes = trace.getIndexedWriteNodes().get(addr);

			//skip if there is no write events to the address
			if (writenodes == null || writenodes.size() < 1)
				continue;

			//check if local variable
			if (trace.isLocalAddress(addr))
				continue;

			HashMap<String, ArrayList<WriteNode>> valueMap = new HashMap<String, ArrayList<WriteNode>>();
			//group writes by their value
			for (WriteNode wnode : writenodes) {
				String v = wnode.getValue();
				ArrayList<WriteNode> list = valueMap.get(v);
				if (list == null) {
					list = new ArrayList<>();
					valueMap.put(v, list);
				}
				list.add(wnode);
			}

			//check read-write
			if (readnodes != null) {
				for (ReadNode readnode : readnodes) {

					HashMap<String, Set<Vector<String>>> mValuesPrefixes = new HashMap<>();
					//if isfulltrace, only consider the read nodes that happen after the prefix
					if (isfulltrace && readnode.getGID() <= schedule_prefix.size())
						continue;

					String rValue = readnode.getValue();
					//1. first check if the rnode can read from the initial value which is different from rValue
					boolean success = false;
					if (initVal == null && !rValue.equals("0")
							|| initVal != null && !initVal.equals(rValue)) {
						success = checkInitial(engine, trace, schedule_prefix, writenodes,
								readnode, initVal, mValuesPrefixes);
					}

					//2. then check if it can read from a particular write
					for (final String wValue : valueMap.keySet()) {
						if (!wValue.equals(rValue)) {
							//if it already reads from the initial value, then skip it
							if (wValue.equals(initVal) && success) {
								continue;
							}
							checkReadWrite(engine, trace, schedule_prefix, valueMap, readnode, wValue, mValuesPrefixes);
						}
					}
					//for each read, add the values and the corresponding prefixes to the vector
					if (!mValuesPrefixes.isEmpty()) {
						vReadValuePrefixes.add(mValuesPrefixes);
					}
				} //end for check read write
			}
		}  //end while
		
		memUsed += memSize(vReadValuePrefixes);
		
		if (Configuration.OMCR) {
			//local
			HashMap<Vector<String>, Set<Vector<String>>> localMapPrefixEquClass =
					new HashMap<>();
			//compute the equivalent prefixes
			computeEquPrefixes(vReadValuePrefixes,trace, localMapPrefixEquClass);
			memUsed += memSize(localMapPrefixEquClass);
			//
			Set<Vector<String>> equPrefixes = null;
			if (mapPrefixEquivalent.containsKey(schedule_prefix)) {
				equPrefixes = mapPrefixEquivalent.get(schedule_prefix);
			}		
			//check each new prefix
			//for each read
            for (HashMap<String, Set<Vector<String>>> valuePrefixes : vReadValuePrefixes) {
                //for each value
                for (Set<Vector<String>> setPrefixes : valuePrefixes.values()) {
                    //choose the prefix with max equivalent prefixes
                    int num = 0;
                    Iterator<Vector<String>> itPrefix = setPrefixes.iterator();
                    Vector<String> prefix = null;

                    //for each prefix that make the read return the value
                    while (itPrefix.hasNext()) {
                        Vector<String> tmp = itPrefix.next();
                        Vector<String> prefix1 = new Vector<>();
                        for (String xi : tmp) {
                            long gid = Long.valueOf(xi.substring(1));
                            long tid = trace.getNodeGIDTIdMap().get(gid);
                            String name = trace.getThreadIdNameMap().get(tid);
                            prefix1.add(name);
                        }

                        int flag = 0;
                        if (equPrefixes != null) {
                            //it may not in the same order
                            for (Vector<String> p : equPrefixes) {
                                Vector<String> copy = new Vector<>(p);
                                Collections.sort(copy);
                                Collections.sort(prefix1);
                                if (copy.equals(prefix1)) {
//									System.err.println("test");
                                    flag = 1;
                                    break;
                                }
                            }
                        }

                        if (flag == 1) {
                            continue;
                        }

                        if (localMapPrefixEquClass.containsKey(tmp)) {
                            if (localMapPrefixEquClass.get(tmp).size() > num) {
                                num = localMapPrefixEquClass.get(tmp).size();
                                prefix = tmp;
                            }
                        } else if (prefix == null) {
                            prefix = tmp;
                        }
                    }

                    if (prefix != null) {
                        omcrGenSchedule(trace, prefix, schedule_prefix, localMapPrefixEquClass);
                    }
                }
            }
		}
	}
	
	
	private boolean checkInitial(ConstraintsBuildEngine engine, Trace trace,
			Vector<String> schedule_prefix, Vector<WriteNode> writenodes,
			ReadNode rnode, String initVal,
			HashMap<String, Set<Vector<String>>> mValuesPrefixes) {
		//construct constraints and generate schedule
		HashSet<AbstractNode> depNodes = engine.getDependentNodes(trace,rnode);
		
		HashSet<AbstractNode> readDepNodes = new HashSet<AbstractNode>();
		//OMCR
		HashMap<String, Set<Vector<String>>> mValuePrefix = new HashMap<>();
		Set<Vector<String>> prefix = new HashSet<Vector<String>>();
		
		if(isfulltrace && schedule_prefix.size()>0)
			depNodes.addAll(trace.getFullTrace().subList(0, schedule_prefix.size()));
		
		
		depNodes.add(rnode);
		readDepNodes.add(rnode);

        StringBuilder sb;
        sb = engine.constructFeasibilityConstraints(trace,depNodes,readDepNodes, rnode, null);
        StringBuilder sb2;
        sb2 = engine.constructReadInitWriteConstraints(rnode,depNodes, writenodes);

        sb.append(sb2);
		//@alan
		//adding rnode.getGid() as a parameter
		Vector<String> schedule = engine.generateSchedule(sb,rnode.getGID(),rnode.getGID(),isfulltrace?schedule_prefix.size():0);
		
		output = output + Configuration.numReads + " " +
				Configuration.rwConstraints + " " +
				Configuration.solveTime + "\n";
			
		if(schedule!=null){
			if (Configuration.OMCR) {
				prefix.add(schedule);
				mValuesPrefixes.put(initVal, prefix);
//				vReadValuePrefixes.add(mValuePrefix);
				return true;
			}
			else{
				generateSchedule(schedule,trace,schedule_prefix,rnode.getID(),rnode.getValue(),initVal,-1);
				return true;
			}
			
		}
		return false;
	}

	/**
	 * check if a read can read from a particular write
	 */
	private void checkReadWrite(
			ConstraintsBuildEngine engine, 
			Trace trace,
			Vector<String> schedule_prefix,
			HashMap<String, ArrayList<WriteNode>> valueMap, 
			ReadNode rnode,
			String wValue,
			HashMap<String, Set<Vector<String>>> mValuesPrefixes) 
	{	
		Vector<AbstractNode> otherWriteNodes = new Vector<AbstractNode>();
		
		//OMCR
		
		Set<Vector<String>> prefix = new HashSet<Vector<String>>();

        for (Entry<String, ArrayList<WriteNode>> entry : valueMap.entrySet()) {
            if (!entry.getKey().equals(wValue))
                otherWriteNodes.addAll(entry.getValue());
        }

		ArrayList<WriteNode> wnodes = valueMap.get(wValue);
		Vector<Long> tids = new Vector<>();

        for (WriteNode wnode : wnodes) {
            if (tids.contains(wnode.getTid())) {
                continue;
            }
            if (rnode.getTid() != wnode.getTid()) {
                //check whether possible for read to match with write
                //can reach means a happens before relation
                //the first if-condition is a little strange
                if (rnode.getGID() > wnode.getGID() || !engine.canReach(rnode, wnode)) {

                    //for all the events that happen before the target read and chosen write
                    HashSet<AbstractNode> depNodes = new HashSet<>();

                    //only for all the events that happen before the target read
                    HashSet<AbstractNode> readDepNodes = new HashSet<>();

                    if (isfulltrace && schedule_prefix.size() > 0)
                        depNodes.addAll(trace.getFullTrace().subList(0, schedule_prefix.size()));

                    //1. first find all the dependent nodes
                    depNodes.add(rnode);
                    depNodes.add(wnode);

                    readDepNodes.add(rnode);

					/*
					 * After using static analysis, some reads can be removed
					 * but they cannot be removed, otherwise the order calculated will be wrong
					 * it just needs to ignore the feasibility constraints of these reads
					 * @author Alan
					 */
                    HashSet<AbstractNode> nodes1 = engine.getDependentNodes(trace, rnode);
                    HashSet<AbstractNode> nodes2 = engine.getDependentNodes(trace, wnode);

                    depNodes.addAll(nodes1);
                    depNodes.addAll(nodes2);

                    readDepNodes.addAll(nodes1);

                    //construct feasibility constraints
                    StringBuilder sb =
                            engine.constructFeasibilityConstraints(trace, depNodes, readDepNodes, rnode, wnode);

                    //construct read write constraints, namely, all other writes either happen before the Write
                    //or after the Read.
                    StringBuilder sb3 =
                            engine.constructReadWriteConstraints(depNodes, rnode, wnode, otherWriteNodes);

                    sb.append(sb3);

                    Vector<String> schedule =
                            engine.generateSchedule(sb, rnode.getGID(), wnode.getGID(), isfulltrace ? schedule_prefix.size() : 0);

                    //each time compute a causal schedule, record the information of #read, #constraints, time
                    output = output + Long.toString(Configuration.numReads) + " " +
                            Long.toString(Configuration.rwConstraints) + " " +
                            Long.toString(Configuration.solveTime) + "\n";

                    if (schedule != null) {
                        if (Configuration.OMCR) {
                            //TODO: compute the equivalent class of prefixes
                            prefix.add(schedule);
                            tids.add(wnode.getTid());

                        } else {
                            //rnode.getID or GID??
                            generateSchedule(schedule, trace, schedule_prefix, rnode.getID(), rnode.getValue(), wValue, wnode.getID());
                            break;
                        }
                    }
                }
            }
        }// end for_writes
		
		//add the equivalent class to the whole vector
		if (Configuration.OMCR && !prefix.isEmpty()) {
			mValuesPrefixes.put(wValue, prefix);
		}		
	}


    /**
     * Among the new prefix generated, check if any two of them could lead to redundant executions
     */
	private void computeEquPrefixes(
	        Vector<HashMap<String, Set<Vector<String>>>> schedules,
			Trace trace,
			HashMap<Vector<String>, Set<Vector<String>>> localMapPrefixEquClass)
	{
		//iterate over reads
		for (int i = 0; i < schedules.size() - 1; i++) {
			HashMap<String, Set<Vector<String>>> mVauePrefix = schedules.get(i);
			//iterate each value that this read can return
			for(Entry<String, Set<Vector<String>>> entryA : mVauePrefix.entrySet()){
				String vA = entryA.getKey();
				Set<Vector<String>> prefixes = entryA.getValue();
				//get prefix A
				for (Vector<String> pA : prefixes){
					//compare with prefix B
					for (int j = i+1; j < schedules.size(); j++){
						HashMap<String, Set<Vector<String>>> mVauePrefix2 = schedules.get(j);
						for(Entry<String, Set<Vector<String>>> entryB : mVauePrefix2.entrySet()){
							String vB = entryB.getKey();
							Set<Vector<String>> prefixes2 = entryB.getValue();
							for (Vector<String> pB : prefixes2){
								Vector<String> pAB = new Vector<>(); 
								if (checkEquivalence(trace, pAB, pA, vA, pB, vB)){
									//add the equivalent prefix to the class
//									addToEquivalentClass(trace, pA, pAB, localMapPrefixEquClass);
									if (localMapPrefixEquClass.containsKey(pA)) {
										localMapPrefixEquClass.get(pA).add(pAB);
									} else {
										Set<Vector<String>> equClass = new HashSet<Vector<String>>();
										equClass.add(pAB);
										localMapPrefixEquClass.put(pA, equClass);
									}
									
								}
							}
						}
					}
				}
			}
		}
		
	}
	
	private static boolean checkEquivalence(Trace trace, Vector<String> pAB, 
			Vector<String> pA, String vA, Vector<String> pB, String vB) {
		Vector<String> pBA = new Vector<>();
		String rA = pA.lastElement();
		String rB = pB.lastElement();
		if (pA.contains(rB) || pB.contains(rA)) {
			return false;
		}

        return combineTwoPrefixes(trace, pAB, pA, pB, rB, vB) &&
                combineTwoPrefixes(trace, pBA, pB, pA, rA, vA);
    }
	
	private static boolean combineTwoPrefixes(Trace trace, Vector<String> pAB, Vector<String> pA, Vector<String> pB, 
			String rB, String vB){
//		for (int i = 0; i < pA.size(); i++) {
//			pAB.add(pA.get(i));
//		}
		
		//needs to consider about the synchronizations
		//e.g., A: lock-read_x
		//      B: lock-read_y
		//lock-r_x-lock-read_y is infeasible
		long gidrB = Long.valueOf(rB.substring(1));
		long tidrB = trace.getNodeGIDTIdMap().get(gidrB);
		
		Stack<AbstractNode> stLocks = new Stack<AbstractNode>();
		Vector<AbstractNode> aTrace = trace.getFullTrace();
        for (String aPA : pA) {
            long index = Long.valueOf(aPA.substring(1)) - 1;
            AbstractNode aNode = aTrace.get((int) index);
            if (aNode instanceof LockNode) {
                stLocks.push(aNode);
            } else if (aNode instanceof UnlockNode) {
                if (!stLocks.isEmpty()) {
                    stLocks.pop();
                }
            }
        }
		
		//if stLock is not empty, it means that the unlocks do not appear in the prefix
		if (!stLocks.isEmpty()) {
			HashMap<String, LockNode> mAddrTid = new HashMap<>();
			while(!stLocks.isEmpty()){
				LockNode l = (LockNode) stLocks.pop();
				String addr = l.getAddr();
				mAddrTid.put(addr, l);
			}

            for (String aPB : pB) {
                long index = Long.valueOf(aPB.substring(1)) - 1;
                AbstractNode aNode = aTrace.get((int) index);
                if (aNode instanceof LockNode) {
                    String addr = ((LockNode) aNode).getAddr();
                    if (mAddrTid.containsKey(addr)) {
                        LockNode l = mAddrTid.get(addr);
                        Vector<AbstractNode> tidTrace = trace.getThreadNodesMap().get(l.getTid());
                        int index1 = tidTrace.indexOf(l);
                        if (index1 != -1) {
                            for (int j = index1 + 1; j < tidTrace.size(); j++) {
                                AbstractNode absNode = tidTrace.get(j);
                                String choice = "x" + absNode.getGID();
                                if (!pA.contains(choice)) {
                                    pAB.add(choice);
                                    if (absNode instanceof UnlockNode) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
		}

        for (String aPB : pB) {
            if (!pA.contains(aPB)) {
                pAB.add(aPB);
            }
        }
		long gid = Long.valueOf(rB.substring(1));
//		Vector<AbstractNode> tAbstractNodes = trace.getFullTrace();
//		AbstractNode node1 = tAbstractNodes.get((int) gid);
		ReadNode rNodeB = (ReadNode) trace.getFullTrace().get((int) gid - 1);
		String addr = rNodeB.getAddr();
		for (int j = pAB.size() - 2; j >=0; j--){
			long _gid = Long.valueOf(pAB.get(j).substring(1));
			AbstractNode node = trace.getFullTrace().get((int) _gid - 1);
			if (node instanceof WriteNode) {
				if (((WriteNode) node).getAddr().equals(addr)) {
					//write to the same address
                    return ((WriteNode) node).getValue().equals(vB);
				}
			}
		}
		int k;
		for (k = pA.size() - 2; k >=0; k--){
			long _gid = Long.valueOf(pA.get(k).substring(1));
			AbstractNode node = trace.getFullTrace().get((int) _gid -1 );
			if (node instanceof WriteNode) {
				if (((WriteNode) node).getAddr().equals(addr)) {
					//write to the same address
                    return ((WriteNode) node).getValue().equals(vB);
				}
			}
		}

        return k < 0;

    }
	
	private void omcrGenSchedule(Trace trace, Vector<String> schedule,
			Vector<String> schedule_prefix, 
			HashMap<Vector<String>, Set<Vector<String>>> localMapPrefixEquClass){
		
		Vector<String> schedule_a = new Vector<String>();	
		int start_index = 0;		
		for (int i=start_index; i<schedule.size(); i++)
		{	
			String xi = schedule.get(i);
			long gid = Long.valueOf(xi.substring(1));
			long tid = trace.getNodeGIDTIdMap().get(gid);
			String name = trace.getThreadIdNameMap().get(tid);
			schedule_a.add(name);
		}
			
		//debugging
//		System.out.println("prefix: " + schedule_a);
		
		if(!isfulltrace) {
		    //add the schedule prefix to the head of the new schedule to make it a complete schedule
			schedule_a.addAll(0, schedule_prefix);
		} 
//		System.out.println("causal schedule: " + schedule_a);
		schedules.add(schedule_a);
		//update the map_prefix_equivalent
		if (localMapPrefixEquClass.containsKey(schedule)) {
			Set<Vector<String>> equPrefixes = localMapPrefixEquClass.get(schedule);
			Set<Vector<String>> valuePrefixes = new HashSet<>();
			for (Vector<String> p : equPrefixes){
				Vector<String> v = new Vector<>();
                for (String xi : p) {
                    long gid = Long.valueOf(xi.substring(1));
                    long tid = trace.getNodeGIDTIdMap().get(gid);
                    String name = trace.getThreadIdNameMap().get(tid);
                    v.add(name);
                }
				valuePrefixes.add(v);
			}
			mapPrefixEquivalent.put(schedule_a, valuePrefixes);
		}
		
	}

	/**
	 * Generate the causal schedule
	 */
	private void generateSchedule(
	        Vector<String> schedule,
            Trace trace,
			Vector<String> schedule_prefix,
            int rGid,
            String rValue,
            String wValue,
            int wID)
	{
		{	
			Vector<String> schedule_a = new Vector<String>();
			
			//get the first start event
			//note that in the first execution, there might be some events before the start event
			//but for the next execution, these events will not be executed
			
			//but for RV example, these events are executed for the next execution
			//for the implementation, just make all the assignments under main
			
			//@Alan
			int start_index = 0;
			for (int i=start_index; i<schedule.size(); i++)
			{
				String xi = schedule.get(i);
				long gid = Long.valueOf(xi.substring(1));
				long tid = trace.getNodeGIDTIdMap().get(gid);
				String name = trace.getThreadIdNameMap().get(tid);	
				
				//add addr info to the schedule 
				//the addr information is needed when replay yhe TSO/PSO schedule
				if (Objects.equals(Configuration.mode, "TSO") || Objects.equals(Configuration.mode, "PSO"))
				{
					String addr="";
					AbstractNode node = trace.getFullTrace().get((int) (gid-1));
					if(node instanceof ReadNode || node instanceof WriteNode){
						addr = ((IMemNode) node).getAddr();
						if(!Objects.equals(addr.split("\\.")[0], addr))
							addr = addr.split("\\.")[1];					
					}			
					if(Objects.equals(addr, "")){
						addr=""+node.getType();
					}
					name = name + "_" + addr;
				}
				
				schedule_a.add(name);
			}
				
			//debugging
//			System.out.println("prefix: " + schedule_a);
			
			if(!isfulltrace) {
			    //add the schedule prefix to the head of the new schedule to make it a complete schedule
				schedule_a.addAll(0, schedule_prefix);
			}
//			else {
//				System.out.println(" USING FULL TRACE************");
//			}
//			System.out.println("causal schedule: " + schedule_a);
			schedules.add(schedule_a);
			
			if(Configuration.DEBUG)
			{
				//report the schedules
				String msg_header = "************* causal schedule "+(++schedule_id)+" **************\n";
				String msg_rwmeta = "Read-Write: "+trace.getStmtSigIdMap().get(rGid)+" -- "+(wID<0?"init":trace.getStmtSigIdMap().get(wID))+"\n";
				String msg_rwvalue = "Values: ("+rValue+"-"+wValue+")     ";
				String msg_schedule = "Schedule: "+trim(schedule_a)+"\n";
				String msg_footer = "**********************************************\n";
				
				report(msg_header+msg_rwmeta+msg_rwvalue+msg_schedule+msg_footer,MSGTYPE.STATISTICS);
			}
		}	
	}

	private static void report(String msg, MSGTYPE type)
	{
		switch(type)
		{
		case REAL:
		case STATISTICS:
			System.err.println(msg);
			break;
		default: break;
		}
	}
	public enum MSGTYPE
	{
		REAL,POTENTIAL,STATISTICS
	}
	private static ConstraintsBuildEngine getEngine(String name)
	{
		if(iEngine==null){
	        Configuration config = new Configuration();
	        config.tableName = name;
	        config.constraint_outdir = "tmp" + System.getProperty("file.separator") + "smt";

	        iEngine = new ConstraintsBuildEngine(config);//EngineSMTLIB1
		}
		
			return iEngine;
	}
	
	/**
	 * start exploring the trace
	 * @param trace: the trace generated by re-execution
	 * @param schedule_prefix: prefix that leads to the generation of this trace
	 */
	 void execute(Trace trace, Vector<String> schedule_prefix) {
		
		Configuration.numReads = 0;
		Configuration.rwConstraints = 0;
		Configuration.solveTime = 0;
			
		//OPT: if #sv==0 or #shared rw ==0 continue	
		if(trace.hasSharedVariable())
		{
			//engine is used for constructing constraints model
			ConstraintsBuildEngine engine = getEngine(trace.getApplicationName());
			
			//pre-process the trace
			//build the initial happen before relation for some optimization
			engine.preprocess(trace);
		
			//generate causal prefixes
			genereteCausallyDifferentSchedules(engine,trace,schedule_prefix);
			
		}		
	}
	
	//compute the memory used
	static int memSize(Object o){
        try{
//            System.out.println("Index Size: " + ((ByteArrayOutputStream) o).size());
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            ObjectOutputStream oos=new ObjectOutputStream(baos);
            oos.writeObject(o);
            oos.close();
//            System.out.println("Data Size: " + baos.size() + "bytes.");
            return baos.size();
        }catch(IOException e){
            e.printStackTrace();
            return -1;
        }
        
        
	}
}
