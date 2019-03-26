
package edu.tamu.aser.constraints;

import edu.tamu.aser.graph.LockSetEngine;
import edu.tamu.aser.graph.ReachabilityEngine;
import edu.tamu.aser.config.Configuration;
import edu.tamu.aser.trace.AbstractNode;
import edu.tamu.aser.trace.IMemNode;
import edu.tamu.aser.trace.ISyncNode;
import edu.tamu.aser.trace.JoinNode;
import edu.tamu.aser.trace.LockNode;
import edu.tamu.aser.trace.LockPair;
import edu.tamu.aser.trace.NotifyNode;
import edu.tamu.aser.trace.ReadNode;
import edu.tamu.aser.trace.StartNode;
import edu.tamu.aser.trace.Trace;
import edu.tamu.aser.trace.UnlockNode;
import edu.tamu.aser.trace.WaitNode;
import edu.tamu.aser.trace.WriteNode;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The engine for constraint construction and solving.
 * 
 * @author jeffhuang and alan huang
 *
 */
public class ConstraintsBuildEngine
{

//    public static int size = 0;

	private ReachabilityEngine reachEngine;//TODO: do segmentation on this
    private HashMap<AbstractNode,AbstractNode> partialOrderMap = new HashMap<>();
	private HashMap<String,Vector<LockPair>> lockPairsMap = new HashMap<>();
	private HashMap<Long,Vector<LockPair>> threadLockPairs = new HashMap<>();
	private HashMap<AbstractNode,HashSet<AbstractNode>> specialDependentNodesMap = new HashMap<>();
	
	//constraints below
	private StringBuilder CONS_DECLARE;
	private StringBuilder CONS_ASSERT_PO;
	private StringBuilder CONS_ASSERT_VALID;
    private final StringBuilder CONS_GETMODEL = new StringBuilder("(check-sat)\n(get-model)\n(exit)");

    protected AtomicInteger id = new AtomicInteger();//constraint id
    protected Configuration config;
	

	
	public ConstraintsBuildEngine(Configuration config)
	{
		this.config = config;
	}

	private String makeVariable(long GID)
	{
		return "x"+GID;
	}
	
	/**
	 * take the constraints as the input
	 * declare every variable in the constraints as an integer variable
	 */
    private void declareVariables(StringBuilder cons){
		
		CONS_DECLARE = new StringBuilder();
		HashSet<String> vars = new HashSet<>();
		for(String s : cons.toString().split(" ")){
			if (s.startsWith("x") && !vars.contains(s)) {
				vars.add(s);
				CONS_DECLARE.append("(declare-const ").append(s).append(" Int)\n");
			}
		}		
	}
	
	/**
	 * This function is for generating constraints for new schedule
	 * it does not need to contain all the nodes, just the nodes dependent
	 * @author shiyou huang
	 * @param trace the trace
	 * @param depNodes dependent events that the target read event depends on
	 */
    private void constructPOConstraintsRMM(Trace trace, HashSet<AbstractNode> depNodes){
		
		CONS_ASSERT_PO = new StringBuilder();

		//get needed data structure
		HashMap<String,HashMap<Long,Vector<IMemNode>>> indexedMap1 = trace.getIndexedThreadReadWriteNodes();
		HashMap<Long,Vector<AbstractNode>> mapthreadIdtoNode1 = trace.getThreadNodesMap();
		
		//reconstruct the map, only contains the nodes in the depNodes
		HashMap<String, HashMap<Long, Vector<IMemNode>>> indexedMap = new HashMap<>();
		HashMap<Long,Vector<AbstractNode>> mapthreadIdtoNode = new HashMap<> ();
		
		Set<String> keys = indexedMap1.keySet();
		for(String key: keys){
			HashMap<Long,Vector<IMemNode>> map = indexedMap1.get(key);
			HashMap<Long,Vector<IMemNode>> newMap = new HashMap<>();
			Set<Long> tidKeys = map.keySet();
			for(Long tid: tidKeys){
				Vector<IMemNode> iMemNodes = map.get(tid);
				Vector<IMemNode> newNodes = new Vector<>();
				for(IMemNode node : iMemNodes){
					if (depNodes.contains(node)) {
						newNodes.addElement(node);
					}
				}
				if (newNodes.size()>0) {
					newMap.put(tid, newNodes);
				}
			}
			if (newMap.size()>0) {
				indexedMap.put(key, newMap);
			}
		}
		
		Set<Long> Tids = mapthreadIdtoNode1.keySet();
		for(Long tid: Tids){
			Vector<AbstractNode> nodes = mapthreadIdtoNode1.get(tid);
			Vector<AbstractNode> newNodes = new Vector<AbstractNode>();
			for(AbstractNode node : nodes){
				if (depNodes.contains(node)) {
					newNodes.addElement(node);
				}
			}
			if (newNodes.size() > 0) {
				mapthreadIdtoNode.put(tid, newNodes);
			}
		}
		
		/*
		 * the nodes with same addr should be consistent
		 */
        for (HashMap<Long, Vector<IMemNode>> map : indexedMap.values()) {
            for (Vector<IMemNode> nodes : map.values()) {
                long lastGID = nodes.get(0).getGID();
                String lastVar = makeVariable(lastGID);
                for (int i = 1; i < nodes.size(); i++) {
                    //AbstractNode thisNode = (AbstractNode) nodes.get(i);
                    //if (depNodes.contains(thisNode))
                    {
                        long thisGID = nodes.get(i).getGID();
                        String var = makeVariable(thisGID);
                        CONS_ASSERT_PO.append("(assert (< ").append(lastVar).append(" ").append(var).append("))\n");

                        reachEngine.addEdge(lastGID, thisGID);

                        lastGID = thisGID;
                        lastVar = var;
                    }
                }
            }
        }  //end constrain the nodes with same addr
		
		/*
		 * for TSO and PSO
		 *  the order of read operations from the same thread can not be relaxed 
		 */
        for (Vector<AbstractNode> nodes : mapthreadIdtoNode.values()) {
            int i;
            //AbstractNode readNode;
            /*
			 * get the first read node in this thread
			 */
            for (i = 0; i < nodes.size(); i++) {
                if (nodes.get(i) instanceof ReadNode)
                    break;
            }

            if (i == nodes.size())
                continue;

            long lastGID = nodes.get(i).getGID();
            String lastVar = makeVariable(lastGID);
            for (int j = i + 1; j < nodes.size(); j++) {
                if (nodes.get(j) instanceof ReadNode) {
                    //if (depNodes.contains(nodes.get(j)))
                    {
                        long thisGID = nodes.get(j).getGID();
                        String var = makeVariable(thisGID);
                        CONS_ASSERT_PO.append("(assert (< ").append(lastVar).append(" ").append(var).append("))\n");

                        //the order is added to reachability engine for quick testing
                        reachEngine.addEdge(lastGID, thisGID);

                        lastGID = thisGID;
                        lastVar = var;
                    }
                }
            }
        }//end constraning r-r
		/*
		 * For TSO, write nodes from the same thread can not be reordered 
		 * For PSO, no this constraint
		 */
		if (Objects.equals(Configuration.mode, "TSO")) {
            for (Vector<AbstractNode> nodes : mapthreadIdtoNode.values()) {
                int i;
                //AbstractNode writeNode;
                for (i = 0; i < nodes.size(); i++) {
                    if (nodes.get(i) instanceof WriteNode)
                        break;
                }

                if (i == nodes.size())
                    continue;

                long lastGID = nodes.get(i).getGID();
                String lastVar = makeVariable(lastGID);
                for (int j = i + 1; j < nodes.size(); j++) {
                    if (nodes.get(j) instanceof WriteNode) {
//						if (depNodes.contains(nodes.get(j))) 
                        {
                            long thisGID = nodes.get(j).getGID();
                            String var = makeVariable(thisGID);
                            CONS_ASSERT_PO.append("(assert (< ").append(lastVar).append(" ").append(var).append("))\n");

                            //the order is added to reachability engine for quick testing
                            reachEngine.addEdge(lastGID, thisGID);

                            lastGID = thisGID;
                            lastVar = var;
                        }
                    }

                }
            }//end constraining w-w
		}

		//the nodes which are not read/write nodes should be consistent
		for (Vector<AbstractNode> nodes : mapthreadIdtoNode.values()) {
			//Profile the sync nodes
			Vector<AbstractNode> nonMemNodes = new Vector<AbstractNode>();

			for (AbstractNode node : nodes) {
				if (node instanceof ReadNode || node instanceof WriteNode) {
					//memNodes.add(nodes.get(i));
				} else {
					if (depNodes.contains(node)) {
						nonMemNodes.add(node);
					}
				}
			}
			if (nonMemNodes.size() == 0)
				continue;

//			if (!depNodes.contains(nonMemNodes.get(0))) {
//				continue;
//			}

			long lastGID = nonMemNodes.get(0).getGID();
			String lastVar = makeVariable(lastGID);
			for (int i = 1; i < nonMemNodes.size(); i++) {
				if (!depNodes.contains(nonMemNodes.get(i)))
					break;
				{
					long thisGID = nonMemNodes.get(i).getGID();
					String thisVar = makeVariable(thisGID);
					CONS_ASSERT_PO.append("(assert (< ").append(lastVar).append(" ").append(thisVar).append("))\n");

					//the order is added to reachability engine for quick testing
					reachEngine.addEdge(lastGID, thisGID);

					lastGID = thisGID;
					lastVar = thisVar;
				}

			}

			/*
			 * for each sync nodes, it needs to consider the memory nodes before and after it
			 * it should be strictly restricted by the happens before order
			 */
			for (int i = 0; i < nonMemNodes.size(); i++) {
				//AbstractNode node = nonMemNodes.get(i);
				int startIndex;
				if (i == 0)
					startIndex = 0;
				else
					startIndex = nodes.indexOf(nonMemNodes.get(i - 1)) + 1;

				int endIndex = nodes.indexOf(nonMemNodes.get(i));
				//if(startIndex == endIndex)
				//	continue;

				long curGID = nonMemNodes.get(i).getGID();
				String curVar = makeVariable(curGID);
				for (int j = startIndex; j < endIndex; j++) {
					if (depNodes.contains(nodes.get(j))) {
						long nodeGID = nodes.get(j).getGID();
						String nodeVar = makeVariable(nodeGID);

						CONS_ASSERT_PO.append("(assert (< ").append(nodeVar).append(" ").append(curVar).append("))\n");

						//the order is added to reachability engine for quick testing
						reachEngine.addEdge(nodeGID, curGID);
					}

				}
				//handle the rw-nodes after this node
				startIndex = endIndex + 1;
				if (i == nonMemNodes.size() - 1)  //the last one
					endIndex = nodes.size();
				else
					endIndex = nodes.indexOf(nonMemNodes.get(i+1));
				//if(startIndex == endIndex)
				//	continue;

				for (int j = startIndex; j < endIndex; j++) {
					if (depNodes.contains(nodes.get(j))) {
						long nodeGID = nodes.get(j).getGID();
						String nodeVar = makeVariable(nodeGID);

						CONS_ASSERT_PO.append("(assert (< ").append(curVar).append(" ").append(nodeVar).append("))\n");

						//the order is added to reachability engine for quick testing
						reachEngine.addEdge(curGID, nodeGID);
					}

				}

			}
		}  //end constraining sync nodes
		
		/*
		 * the read and its next write should be consistent
		 * For PSO, the write can be reordered after this read
		 */
        for (Vector<AbstractNode> nodes : mapthreadIdtoNode.values()) {
            //Profile the read nodes
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i) instanceof ReadNode) {
                    //readNodes.add(nodes.get(i));
                    long lastGID = nodes.get(i).getGID();
                    String lastVar = makeVariable(lastGID);

                    int j;
                    for (j = i + 1; j < nodes.size(); j++) {
                        if (nodes.get(j) instanceof WriteNode) {
                            if (depNodes.contains(nodes.get(j))) {
                                long thisGID = nodes.get(j).getGID();
                                String thisVar = makeVariable(thisGID);
                                CONS_ASSERT_PO.append("(assert (< ").append(lastVar).append(" ").append(thisVar).append("))\n");

                                //the order is added to reachability engine for quick testing
                                reachEngine.addEdge(lastGID, thisGID);
                            }
                            if (Objects.equals(Configuration.mode, "TSO"))
                                break;
                        }
                    }
                    if (j == nodes.size())
                        break;
                }
            }
        }    //end r-w
	}
	
	/**
	 * Construct the Sync. constraints for TSO/PSO
	 * @param trace
	 * @param depNodes
	 */
    private void constructSyncConstraintsRMM(Trace trace, HashSet<AbstractNode> depNodes){
		//construct a new lockset for this segment
		CONS_ASSERT_VALID = new StringBuilder("");

        LockSetEngine lockEngine = new LockSetEngine();
		
		HashMap<String, Vector<ISyncNode>> syncNodesMap = trace.getSyncNodesMap();
		HashMap<Long, AbstractNode> firstNodes = trace.getThreadFirstNodeMap();
		HashMap<Long, AbstractNode> lastNodes = trace.getThreadLastNodeMap();

		//thread first node - last node
        for (Vector<ISyncNode> nodes : syncNodesMap.values()) {
            //during recording
            //should after wait, before notify
            //after lock, before unlock
            for (ISyncNode node1 : nodes) {

                if (!depNodes.contains(node1)) {
                    continue;
                }

                long thisGID = node1.getGID();
                String var = makeVariable(thisGID);
                if (node1 instanceof StartNode) {
                    if (depNodes.contains(node1)) {
                        long tid = Long.valueOf(node1.getAddr());
                        /*
                         * under TSO, the start node should happen before all the nodes that belong to the
                         * thread it starts
                         */
                        if (Configuration.mode.equals("TSO") || Configuration.mode.equals("PSO")) {
                            Vector<AbstractNode> nodesSet = trace.getThreadNodesMap().get(tid);
                            for (AbstractNode aNodesSet : nodesSet) {
                                if (depNodes.contains((AbstractNode) aNodesSet)) {
                                    long nodeGID = aNodesSet.getGID();
                                    String nodeVar = makeVariable(nodeGID);

                                    CONS_ASSERT_VALID.append("(assert (< ").append(var).append(" ").append(nodeVar).append("))\n");

                                    reachEngine.addEdge(thisGID, nodeGID);
                                }

                            }
                        }  //end TSO
                        else {
                            AbstractNode fnode = firstNodes.get(tid);
                            if (fnode != null) {
                                long fGID = fnode.getGID();
                                String fvar = makeVariable(fGID);

                                //start-begin ordering
                                CONS_ASSERT_VALID.append("(assert (< ").append(var).append(" ").append(fvar).append("))\n");

                                reachEngine.addEdge(thisGID, fGID);

                            }

                        }
                    }
                } else if (node1 instanceof JoinNode)  //thread join and the last action of that thread happens in different threads
                {
                    if (depNodes.contains(node1)) {
                        long tid = Long.valueOf(node1.getAddr());
                        /*
                         * all node should happen before the join node which wait for that thread
                         */
                        if (Objects.equals(Configuration.mode, "TSO") ||
                                Objects.equals(Configuration.mode, "PSO")) {
                            Vector<AbstractNode> nodesSet = trace.getThreadNodesMap().get(tid);
                            if (nodesSet != null) {
                                for (AbstractNode aNodesSet : nodesSet) {
                                    if (depNodes.contains(aNodesSet)) {
                                        long nodeGID = aNodesSet.getGID();
                                        String nodeVar = makeVariable(nodeGID);

                                        CONS_ASSERT_VALID.append("(assert (< ").append(nodeVar).append(" ").append(var).append("))\n");

                                        reachEngine.addEdge(nodeGID, thisGID);
                                    }

                                }
                            }

                        }  //end TSO
                        else {
                            AbstractNode lnode = lastNodes.get(tid);
                            if (lnode != null) {
                                long lGID = lnode.getGID();
                                String lvar = makeVariable(lGID);

                                //end-join ordering
                                CONS_ASSERT_VALID.append("(assert (< ").append(lvar).append(" ").append(var).append("))\n");
                                reachEngine.addEdge(lGID, thisGID);

                            }
                        }
                    }
                }
            }// end for
        }//end while for iterating the threads
		
		//lock constraints
        for (Vector<LockPair> LPs : lockPairsMap.values()) {
            //only select the lock pairs that are in depNodes
            Vector<LockPair> lockPairs = new Vector<LockPair>();
            {
                for (LockPair lp : LPs) {
                    if (lp.unlock != null && depNodes.contains(lp.unlock))
                        lockPairs.add(lp);
                    else if (lp.lock == null)
                        //possible lock is null/init, unlock is not null
                        //-- like the deadlock example
                        lockPairs.add(lp);
                    else if (depNodes.contains(lp.lock)) {
                        lockPairs.add(lp);
                        //it's possible lp.unlock is not included
                        if (lp.unlock != null && !depNodes.contains(lp.unlock)) {
                            //add dependent nodes of lp.unlock
                            AbstractNode ul_node = (AbstractNode) lp.unlock;
                            depNodes.add(ul_node);
                            HashSet<AbstractNode> ul_nodes = getDependentNodes(trace, ul_node);
                            depNodes.addAll(ul_nodes);//add all dependent nodes
                        }

                    }


                }
            }

            if (lockPairs.size() < 2) continue;//nothing to do

            //obtain each thread's last lockpair
            HashMap<Long, LockPair> lastLockPairMap = new HashMap<Long, LockPair>();

            for (int i = 0; i < lockPairs.size(); i++) {
                LockPair lp1 = lockPairs.get(i);
                String var_lp1_a;
                String var_lp1_b = "";

                if (lp1.lock == null)//
                    continue;
                else
                    var_lp1_a = makeVariable(lp1.lock.getGID());

                if (lp1.unlock != null)
                    var_lp1_b = makeVariable(lp1.unlock.getGID());


                long lp1_tid = lp1.lock.getTid();
                LockPair lp1_pre = lastLockPairMap.get(lp1_tid);

                ArrayList<LockPair> flexLockPairs = new ArrayList<LockPair>();

                //find all lps that are from a different thread, and have no happens-after relation with lp1
                //could further optimize by consider lock regions per thread
                for (LockPair lp : lockPairs) {
                    if (lp.lock != null) {
                        if (lp.lock.getTid() != lp1_tid &&
                                !canReach((AbstractNode) lp1.lock, (AbstractNode) lp.lock)) {
                            flexLockPairs.add(lp);
                        }
                    } else if (lp.unlock != null) {
                        if (lp.unlock.getTid() != lp1_tid &&
                                !canReach((AbstractNode) lp1.lock, (AbstractNode) lp.unlock)) {
                            flexLockPairs.add(lp);
                        }
                    }
                }
                if (flexLockPairs.size() > 0) {
                    //for each lock pair lp2 in flexLockPairs
                    //it is either before lp1 or after lp1
                    for (LockPair lp2 : flexLockPairs) {
                        if (lp2.unlock == null || lp2.lock == null && lp1_pre != null)//impossible to match lp2
                            continue;

                        String var_lp2_b = "";
                        String var_lp2_a = "";

                        var_lp2_b = makeVariable(lp2.unlock.getGID());

                        if (lp2.lock != null)
                            var_lp2_a = makeVariable(lp2.lock.getGID());


                        String cons_b;

                        //lp1_b==null, lp2_a=null
                        if (lp1.unlock == null || lp2.lock == null) {
                            cons_b = "(> " + var_lp1_a + " " + var_lp2_b + ")";
                            //the trace may not be well-formed due to segmentation
                            if (lp1.lock.getGID() < lp2.unlock.getGID()) cons_b = "";
                        } else {
                            cons_b = "(or (> " + var_lp1_a + " " + var_lp2_b + ") (> " + var_lp2_a + " " + var_lp1_b + "))";
                        }
                        if (!cons_b.isEmpty())
                            CONS_ASSERT_VALID.append("(assert " + cons_b + ")\n");

                    }
                }
                lastLockPairMap.put(lp1.lock.getTid(), lp1);

            }
        }
	}

	/**
	 * return true if node1 can reach node2 from the ordering relation
	 * 
	 * @param node1
	 * @param node2
	 * @return
	 */
	public boolean canReach(AbstractNode node1, AbstractNode node2)
	{
		long gid1 = node1.getGID();
		long gid2 = node2.getGID();
		
		return reachEngine.canReach(gid1, gid2);	
		
	}

	/**
	 * Identify if two nodes has dependencies
	 * @param w1_readnodes
	 * @param destNode
	 * @return
	 */
    private Vector<ReadNode> realReadDeps(Vector<ReadNode> w1_readnodes, AbstractNode destNode){
		
		if (w1_readnodes.size() == 0 || destNode == null) {
			return new Vector<ReadNode>();
		}
		
		Vector<ReadNode> realReadDeps = new Vector<>();
		
		String label="";
		try {
			label = destNode.getLabel();
			if (destNode instanceof WriteNode) {
				label = label + "_" + "modify";
			}
			else{
				label = label + "_" + "reference";
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
				
		Integer dest = Configuration.mapNodeLabelToId.get(label);
		
		//the node the in the initial are not 
		if (dest == null) {
//			System.out.println(label);
//			System.err.println("wrong");
			return realReadDeps;
		}

        for (ReadNode node : w1_readnodes) {
            String l = node.getLabel();
            l = l + "_" + "reference";

            Integer source = Configuration.mapNodeLabelToId.get(l);
            if (source == null) {
                continue;
            }
            //source != dest??
            if (!Objects.equals(source, dest) && Configuration.reachSDG.canReach(source, dest)) {
                realReadDeps.add(node);
            }
        }
		
		return realReadDeps;
		
	}
	
	/**
	 * Construct the data validity constraints
	 * it makes all the reads that happen before the target read return the same value
	 * @param trace
	 * @param depNodes
	 * @param readNodes
	 * @return
	 */
    private String constructDataValidityConstraints(Trace trace,
                                                    HashSet<AbstractNode> depNodes, Vector<ReadNode> readNodes) {

			String CONS_CAUSAL_RW = "true";
			Vector<ReadNode> processedReadNodes = new Vector<ReadNode>();
			
			//make all the reads in the depSet return the same value
			while(readNodes.size()>0) {
				ReadNode rnode = readNodes.remove(0);
				processedReadNodes.add(rnode);
				
				//get all write nodes on the address
				Vector<WriteNode> writenodes = trace.getIndexedWriteNodes().get(rnode.getAddr());
				
				if(writenodes==null||writenodes.size()<1)//
					continue;			

				//get the most recent write node in the same thread
				WriteNode preNode = null;
				
				boolean canMatchInit = true;
				
				//get all write nodes on the address & write the same value
				Vector<WriteNode> writenodes_value_match = new Vector<WriteNode>();
				Vector<WriteNode> writenodes_other = new Vector<WriteNode>();

                for (WriteNode wnode : writenodes) {
                    if (!canReach(rnode, wnode)) {
                        if (wnode.getValue().equals(rnode.getValue())) {
                            if (wnode.getTid() != rnode.getTid())
                                writenodes_value_match.add(wnode);
                            else {
                                if (preNode == null || (preNode.getGID() < wnode.getGID() && wnode.getGID() < rnode.getGID()))
                                    preNode = wnode;//previous write node
                            }

                            //if wnode happens before rnode
                            //it means, rnode can't read from the initial value
                            if (canReach(wnode, rnode))
                                canMatchInit = false;
                        } else {
                            writenodes_other.add(wnode);
                        }
                    }
                }
				
				//possible to read the initial value
				if(canMatchInit)
				{
					//make sure it reads the initial write
					String rValue = rnode.getValue();
					String initValue = trace.getInitialWriteValueMap().get(rnode.getAddr());
					
					//value match or value match un-tracked init
					if(initValue!=null&&rValue.equals(initValue)
							||initValue==null && (rValue.equals("0")||rValue.equals("0_")) ){	
				
						String var_r = makeVariable(rnode.getGID());
						String cons_init = "true";
                        for (WriteNode wnode3 : writenodes_other) {
                            if (wnode3.getTid() != rnode.getTid() && !canReach(rnode, wnode3) && depNodes.contains(wnode3)) {
                                String var_w3 = makeVariable(wnode3.getGID());
                                String cons_e = "(> " + var_w3 + " " + var_r + " )";
                                Configuration.rwConstraints += 1;
                                cons_init = "(and " + cons_e + " " + cons_init + " )\n";
                            }
                        }
						CONS_CAUSAL_RW = "(and "+cons_init+" "+CONS_CAUSAL_RW+" )\n";							
			
					}
					else 
						canMatchInit = false;
				} //end if can reach initial
					
				//if rnode can't match with initial value
				if(!canMatchInit)
				{
					//preNode: the most recent write node in the same thread
					if(preNode!=null)
						writenodes_value_match.add(preNode);			
							
					//the read should read from the corresponding write
					String var_r = makeVariable(rnode.getGID());						
					String cons_b = "";
					String cons_b_end = "";
					/*
					 * Pay attention here
					 * It also needs to guarantee the feasibility of the write nodes
					 */
					for(int j=0;j<writenodes_value_match.size();j++)
					{
						WriteNode wnode1 = writenodes_value_match.get(j);
						String var_w1 = makeVariable(wnode1.getGID());

						String cons_b_ = "(> "+var_r+" "+var_w1+" )\n";		
						
						Configuration.rwConstraints += 1;
						
						//add wnode1 and its dependent nodes
						{
							HashSet<AbstractNode> w1_nodes = getDependentNodes(trace,wnode1);
							w1_nodes.removeAll(depNodes);//remove common dep nodes
							
							depNodes.add(wnode1);//add wnode2 itself
							depNodes.addAll(w1_nodes);//add all dependent nodes
							
							Vector<ReadNode> w1_readnodes = getReadNodes(w1_nodes);
							//add dependent reads node to process
							w1_readnodes.remove(processedReadNodes);
							
							/**
							 * Only need to consider the real dependent read nodes
							 * @author Alan
							 */
							if (Configuration.Optimize){
								Vector<ReadNode> realReadDeps = realReadDeps(w1_readnodes, wnode1);							
								realReadDeps.remove(readNodes);
								Configuration.numReads += realReadDeps.size();
								
								readNodes.addAll(realReadDeps);
							} else{
								
								Configuration.numReads += w1_readnodes.size();
								readNodes.addAll(w1_readnodes);
							}							
						}
												
						//for all other writes
						//they should either happen before the write or the read
						String cons_c = "";	
						String cons_c_end = "";
						String last_cons_d = null;
                        for (WriteNode wnode2 : writenodes_other) {
                            if (!canReach(wnode2, wnode1) && !canReach(rnode, wnode2) && depNodes.contains(wnode2)) {
                                String var_w2 = makeVariable(wnode2.getGID());
                                if (last_cons_d != null) {
                                    cons_c += "(and " + last_cons_d;
                                    cons_c_end += " )";

                                }
                                String cons_w2node = "";
                                //add wnode2 and its dependent nodes
                                {
                                    HashSet<AbstractNode> w2_nodes = getDependentNodes(trace, wnode2);
                                    w2_nodes.removeAll(depNodes);//remove common dep nodes

                                    depNodes.add(wnode2);
                                    depNodes.addAll(w2_nodes);

                                    Vector<ReadNode> w2_readnodes = getReadNodes(w2_nodes);
                                    w2_readnodes.remove(processedReadNodes);

                                    if (w2_readnodes.size() > 0) {
                                        //if using static analysis to identify the real reads that a read depends on
                                        if (Configuration.Optimize) {
                                            Vector<ReadNode> realReadDeps = realReadDeps(w2_readnodes, wnode1);
                                            cons_w2node = constructDataValidityConstraints(trace, depNodes, realReadDeps);
                                        } else
                                            cons_w2node = constructDataValidityConstraints(trace, depNodes, w2_readnodes);
                                    }

                                }

                                //TODO: only add feasibility constraint for wnode2 for "var_w2 < var_w1" case

                                if (cons_w2node.length() > 0) {
                                    last_cons_d =
                                            "(or (> " + var_w2 + " " + var_r + " ) " +
                                                    "(and (> " + var_w1 + " " + var_w2 + " )\n"
                                                    + cons_w2node + " ))\n";
                                } else {
                                    last_cons_d =
                                            "(or (> " + var_w2 + " " + var_r + " )" +
                                                    " (> " + var_w1 + " " + var_w2 + " ))\n";
                                }

                            }
                        }  //end for other_writes
						if(last_cons_d!=null)
						{
							cons_c+=last_cons_d;
						}
						cons_c=cons_c+cons_c_end;
						
						if(cons_c.length()>0)
							cons_b_ ="(and "+cons_b_+" "+cons_c + " )\n";
						
							
						if(j+1<writenodes_value_match.size())
						{
							cons_b+= "(or "+cons_b_;
							cons_b_end +=" )";
							
							//cons_a+= "(and (> "+var_w1+" "+var_r+")\n";
							//cons_a_end +=")";
						}
						else
						{
							cons_b+= cons_b_;
							//cons_a+= "(> "+var_w1+" "+var_r+")\n";
						}
					}
					
					cons_b +=cons_b_end;

					if(cons_b.length()>0){
						Configuration.rwConstraints += 1;
						CONS_CAUSAL_RW = "(and "+cons_b+" "+CONS_CAUSAL_RW+" )\n";					
					}
				
				}// end if can't match the inital value
			}//end while
			
			return CONS_CAUSAL_RW;
	}
	
	/**
	 * lock-mutual-exclusion constraints
	 * thread begin happen before the first event of thread spawned
	 * thread join happen after the last event of that thread
	 * @param trace
	 * @param depNodes
	 */
	public void constructSyncConstraints(Trace trace, HashSet<AbstractNode> depNodes)
	{
		
		CONS_ASSERT_VALID = new StringBuilder("");
		
		//fork-join wait-notify
        for (Entry<AbstractNode, AbstractNode> entry : partialOrderMap.entrySet()) {
            AbstractNode node1 = entry.getKey();
            if (depNodes.contains(node1)) {
                AbstractNode node2 = entry.getValue();
                String var1 = makeVariable(node1.getGID());

                if (node2 == null)//this means the event is not captured in trace
                    continue;
                String var2 = makeVariable(node2.getGID());

                CONS_ASSERT_VALID.append("(assert (< ").append(var2).append(" ").append(var1).append(" ))\n");
            }
        }
		
		//lock constraints
        for (Vector<LockPair> LPs : lockPairsMap.values()) {
            {
                //only select the lock pairs that are in depNodes
                Vector<LockPair> lockPairs = new Vector<LockPair>();
                {
                    for (LockPair lp : LPs) {
                        if (lp.unlock != null && depNodes.contains(lp.unlock))
                            lockPairs.add(lp);
                        else if (lp.lock == null)
                            //possible lock is null/init, unlock is not null
                            //-- like the deadlock example
                            lockPairs.add(lp);
                        else if (lp.lock != null && depNodes.contains(lp.lock)) {
                            lockPairs.add(lp);
                            //it's possible lp.unlock is not included
                            if (lp.unlock != null && !depNodes.contains(lp.unlock)) {
                                //add dependent nodes of lp.unlock
                                AbstractNode ul_node = (AbstractNode) lp.unlock;
                                depNodes.add(ul_node);
                                HashSet<AbstractNode> ul_nodes = getDependentNodes(trace, ul_node);
                                depNodes.addAll(ul_nodes);//add all dependent nodes
                            }

                        }


                    }
                }

                if (lockPairs.size() < 2) continue;//nothing to do

                //obtain each thread's last lockpair
                HashMap<Long, LockPair> lastLockPairMap = new HashMap<Long, LockPair>();

                for (int i = 0; i < lockPairs.size(); i++) {
                    LockPair lp1 = lockPairs.get(i);
                    String var_lp1_a = "";
                    String var_lp1_b = "";

                    if (lp1.lock == null)//
                        continue;
                    else
                        var_lp1_a = makeVariable(lp1.lock.getGID());

                    if (lp1.unlock != null)
                        var_lp1_b = makeVariable(lp1.unlock.getGID());


                    long lp1_tid = lp1.lock.getTid();
                    LockPair lp1_pre = lastLockPairMap.get(lp1_tid);

                    ArrayList<LockPair> flexLockPairs = new ArrayList<LockPair>();

                    //find all lps that are from a different thread, and have no happens-after relation with lp1
                    //could further optimize by consider lock regions per thread
                    for (LockPair lp : lockPairs) {
                        if (lp.lock != null) {
                            if (lp.lock.getTid() != lp1_tid &&
                                    !canReach((AbstractNode) lp1.lock, (AbstractNode) lp.lock)) {
                                flexLockPairs.add(lp);
                            }
                        } else if (lp.unlock != null) {
                            if (lp.unlock.getTid() != lp1_tid &&
                                    !canReach((AbstractNode) lp1.lock, (AbstractNode) lp.unlock)) {
                                flexLockPairs.add(lp);
                            }
                        }
                    }
                    if (flexLockPairs.size() > 0) {

                        //for each lock pair lp2 in flexLockPairs
                        //it is either before lp1 or after lp1
                        for (LockPair lp2 : flexLockPairs) {
                            if (lp2.unlock == null || lp2.lock == null && lp1_pre != null)//impossible to match lp2
                                continue;

                            String var_lp2_b = "";
                            String var_lp2_a = "";

                            var_lp2_b = makeVariable(lp2.unlock.getGID());

                            if (lp2.lock != null)
                                var_lp2_a = makeVariable(lp2.lock.getGID());


                            String cons_b;

                            //lp1_b==null, lp2_a=null
                            if (lp1.unlock == null || lp2.lock == null) {
                                cons_b = "(> " + var_lp1_a + " " + var_lp2_b + " )";
                                //the trace may not be well-formed due to segmentation
                                if (lp1.lock.getGID() < lp2.unlock.getGID()) cons_b = "";
                            } else {
                                cons_b = "(or (> " + var_lp1_a + " " + var_lp2_b + " ) (> " + var_lp2_a + " " + var_lp1_b + " ))";
                            }
                            if (!cons_b.isEmpty())
                                CONS_ASSERT_VALID.append("(assert " + cons_b + " )\n");

                        }
                    }
                    lastLockPairMap.put(lp1.lock.getTid(), lp1);

                }
            }
        }
		
	}
	
	/**
	 * construct the program order constraitns based on the program model
	 * @param trace
	 * @param depNodes
	 */
	public void constructPOConstraints(Trace trace, HashSet<AbstractNode> depNodes)
	{
		
		CONS_ASSERT_PO = new StringBuilder("");
		HashMap<Long,Vector<AbstractNode>> map = trace.getThreadNodesMap();

        for (Vector<AbstractNode> nodes : map.values()) {
            long lastGID = nodes.get(0).getGID();
            for (int i = 1; i < nodes.size(); i++) {
                AbstractNode thisNode = nodes.get(i);
                //TODO: optimize performance here by maintain index
                if (depNodes.contains(thisNode)) {
                    String lastVar = makeVariable(lastGID);
                    long thisGID = thisNode.getGID();
                    String var = makeVariable(thisGID);
                    CONS_ASSERT_PO.append("(assert (< ").append(lastVar).append(" ").append(var).append(" ))\n");

                    lastGID = thisGID;
                } else
                    break;//next thread nodes
            }
        }

	}
	
	/**
	 * construct feasibility constraints, namely, all the reads should return the same value
	 * to guarantee the reachability of an event
	 * @param trace the current trace
	 * @param depNodes :  all the events that happen before the target read and chosen write
	 * @param readDepNodes : all the events that happen before the target read (cur_rnode here)
	 * @param cur_rnode the given read event
	 * @param wnode the write that r reads from
	 * @return the read-from constraints
	 * 
	 * @author Alan
	 */
	public StringBuilder constructFeasibilityConstraints(
			Trace trace,
			HashSet<AbstractNode> depNodes,   
			HashSet<AbstractNode> readDepNodes,   
			AbstractNode cur_rnode,   
			AbstractNode wnode) {
		
		//get the readnodes
		Vector<ReadNode> readNodes = getReadNodes(depNodes);		
//		HashSet<AbstractNode> otherVariableNodes = new HashSet<AbstractNode>();	
		
		readNodes.remove(cur_rnode);  
		readDepNodes.remove(cur_rnode);  
		
		String CONS_CAUSAL_RW = null;		
		
		//if using the static analysis to reduce the reads that cur_rnode depends on
		if (Configuration.Optimize){
			Vector<ReadNode> realReadDeps = realReadDeps(readNodes, wnode);
			Vector<ReadNode> tmp;
			if (Configuration.plus) {
				 tmp = getReadNodes(readDepNodes);
			}
			else{
				 tmp = realReadDeps(readNodes, cur_rnode);
			}
			
			tmp.removeAll(realReadDeps);
			realReadDeps.addAll(tmp);
			
			Configuration.numReads += realReadDeps.size();	
			CONS_CAUSAL_RW = constructDataValidityConstraints(trace,depNodes,realReadDeps);
		}
		else{			
			Configuration.numReads += readNodes.size();
			CONS_CAUSAL_RW= constructDataValidityConstraints(trace,depNodes,readNodes);
		}
		
		if (Configuration.mode.equals("TSO")|| Configuration.mode.equals("PSO")) 
		{
			//under TSO, I explicitly revoke addConstraints to the trace
			constructSyncConstraintsRMM(trace, depNodes);
			constructPOConstraintsRMM(trace, depNodes);
		}
		else{
			constructSyncConstraints(trace,depNodes);
			constructPOConstraints(trace,depNodes);
		}
		

		return new StringBuilder("(assert "+CONS_CAUSAL_RW+")\n\n");

	}
	public StringBuilder constructReadInitWriteConstraints(ReadNode rnode, HashSet<AbstractNode> depNodes,Vector<WriteNode> writenodes)
	{
		StringBuilder CONS_CAUSAL_RW = new StringBuilder("");  //return value
		
		String var_r = makeVariable(rnode.getGID());						
		
		String cons_c = "";	
		String cons_c_end = "";
		String last_cons_d = null;
        for (WriteNode wnode2 : writenodes) {
            //if this write does not exist in the depNodes, no need to consider
            if (depNodes.contains(wnode2) && !canReach(rnode, wnode2)) {
                String var_w2 = makeVariable(wnode2.getGID());

                if (last_cons_d != null) {
                    cons_c += "(and " + last_cons_d;
                    cons_c_end += " )";

                }
                last_cons_d = "(> " + var_w2 + " " + var_r + " )\n";
            }
        }
		
		if(last_cons_d!=null)
		{
			cons_c+=last_cons_d;
		}
		cons_c=cons_c+cons_c_end;
		
		if(cons_c.length()>0)
			CONS_CAUSAL_RW.append("(assert \n"+cons_c+ " )\n\n");
		
		return CONS_CAUSAL_RW;

	}
	
	/**
	 * construct the read-write constraints to guarantee the read-write consistency
	 * @param depNodes
	 * @param rnode: should read from the wnode
	 * @param wnode
	 * @param writenodes: other writes that write different values from wnode
	 * @return
	 */
	public StringBuilder constructReadWriteConstraints(
			HashSet<AbstractNode> depNodes, AbstractNode rnode, AbstractNode wnode, Vector<AbstractNode> writenodes)
	{
		StringBuilder CONS_CAUSAL_RW = new StringBuilder("");
		String var_w1 = makeVariable(wnode.getGID());
		String var_r = makeVariable(rnode.getGID());						
		
		String cons_b_ = "(> "+var_r+" "+var_w1+" )\n";	
		String cons_c = "";	
		String cons_c_end = "";
		String last_cons_d = null;
        for (AbstractNode wnode2 : writenodes) {
            if (wnode != wnode2
                    && depNodes.contains(wnode2)//no need to consider the non-dependent write node
                    && !canReach(wnode2, wnode)
                    && !canReach(rnode, wnode2)) {
                String var_w2 = makeVariable(wnode2.getGID());

                if (last_cons_d != null) {
                    cons_c += "(and " + last_cons_d;
                    cons_c_end += " )";

                }

                //add constraints for the feasibility of var_w2 when it is before var_w1
                String cons_sb = "(> " + var_w1 + " " + var_w2 + " )";
                last_cons_d =
                        "(or (> " + var_w2 + " " + var_r + " ) " +
                                cons_sb + " )\n";
            }
        }
		
		if(last_cons_d!=null)
		{
			cons_c+=last_cons_d;
		}
		cons_c=cons_c+cons_c_end;
		
		if(cons_c.length()>0)
			cons_b_ ="(and "+cons_b_+" "+cons_c + " )\n";
		
		CONS_CAUSAL_RW.append("(assert \n"+cons_b_+" )\n\n");
		return CONS_CAUSAL_RW;
	}
	
	/**
	 * Taking the constraints as input to generate new schedule
	 * the schedule ends with the target read and,
	 * all the events in the schedule just happen before the read
	 * @param gid:   GID of the read
	 * @param wgid:   GID of the write
	 * @param gid_prefix
	 * @return
	 */
	
	public Vector<String> generateSchedule(StringBuilder causalConstraint, long gid, Long wgid, long gid_prefix)
	{
		id.incrementAndGet();
		ConstraintsSolving task = new ConstraintsSolving(config,id.get());
		
		/*
		 * I will declare the constraints variables here
		 * for all the varaibles that appear in the constraints
		 */
		declareVariables(CONS_ASSERT_PO.append(causalConstraint).append(CONS_ASSERT_VALID));

        String CONS_SETLOGIC = "(set-logic QF_IDL)\n";
        task.sendMessage(CONS_SETLOGIC + CONS_DECLARE + CONS_ASSERT_VALID + CONS_ASSERT_PO + causalConstraint + CONS_GETMODEL,makeVariable(gid),makeVariable(wgid), makeVariable(gid_prefix),reachEngine, causalConstraint.toString(), config);
		
		return task.schedule;
	}
	
	
	private Vector<ReadNode> getReadNodes(HashSet<AbstractNode> depNodes)
	{
		Vector<ReadNode> readnodes = new Vector<ReadNode>();
		//traverse depNodes to find all read nodes

        for (AbstractNode node : depNodes) {
            if (node instanceof ReadNode)
                readnodes.add((ReadNode) node);
        }
				
				return readnodes;
	}
	
	/**
	 * return the set of nodes in the trace which
	 * rnode has data or control dependence
	 */
	public HashSet<AbstractNode> getDependentNodes(Trace trace,
			AbstractNode rnode) {
		
		HashSet<AbstractNode> depNodes = new HashSet<AbstractNode>();
		
		Long tid = rnode.getTid();
		
		Vector<AbstractNode> nodes = trace.getThreadNodesMap().get(tid);
		int index_o = nodes.indexOf(rnode);
		if(index_o>0)
		{			
			for(int i = index_o-1;i>=0;i--)//not including itself
			{
				AbstractNode node = nodes.get(i);
				//
				if(partialOrderMap.containsKey(node))
				{
					HashSet<AbstractNode> depNodes2 = specialDependentNodesMap.get(node);
					if(depNodes2==null)
					{
						AbstractNode node2 = partialOrderMap.get(node);
						if(node2!=null)
						{
							depNodes2 = getDependentNodes(trace,node2);
							depNodes2.add(node2);
							specialDependentNodesMap.put(node, depNodes2);	
						}
					}
					
					if(depNodes2!=null)depNodes.addAll(depNodes2);
				}
				
				/*
				 * Under TSO the read may not dependent on the earlier write
				 * 
				 */
				//assume I add all the nodes in front of current node to the dependent node
//				if(mode=="TSO"){
//					if(rnode instanceof ReadNode && node instanceof WriteNode){
//						if(((ReadNode) rnode).getAddr() != ((WriteNode)node).getAddr()){
//							continue;
//						}
//					}
//				}

				depNodes.add(node);//depends on the memory model			
			}
			
		}
		else
		{
		//check thread fork
			AbstractNode node2 = partialOrderMap.get(rnode);
			if(node2!=null)
			{
				HashSet<AbstractNode> depNodes2 = specialDependentNodesMap.get(rnode);
				if(depNodes2==null)
				{
					depNodes2 = getDependentNodes(trace,node2);
					depNodes2.add(node2);
					specialDependentNodesMap.put(rnode, depNodes2);	
				}
				
				depNodes.addAll(depNodes2);
			}
			
		}
		
		return depNodes;
	}

	/**
	 * building the happens before relation among the events in the single thread
	 * and first event happen after the thread start event
	 * and last event happen before the thread join event 
	 * @param trace
	 */
	public void preprocess(Trace trace) {
		
		//create reachability engine
		reachEngine = new ReachabilityEngine();
		HashMap<Long,Vector<AbstractNode>> map = trace.getThreadNodesMap();
		

		if (Objects.equals(Configuration.mode, "TSO") || Objects.equals(Configuration.mode, "PSO")) {
			//do nothing here
		}
		else{
            for (Vector<AbstractNode> nodes : map.values()) {
                long lastGID = nodes.get(0).getGID();
                for (int i = 1; i < nodes.size(); i++) {
                    long thisGID = nodes.get(i).getGID();
                    //the order is added to reachability engine for quick testing
                    reachEngine.addEdge(lastGID, thisGID);

                    lastGID = thisGID;
                }
            }
		}
		

		HashMap<String,Vector<ISyncNode>> syncNodesMap = trace.getSyncNodesMap();		
		HashMap<Long,AbstractNode> firstNodes = trace.getThreadFirstNodeMap();
		HashMap<Long,AbstractNode> lastNodes = trace.getThreadLastNodeMap();		
		HashMap<Long,Stack<ISyncNode>> threadSyncStack = new HashMap<Long,Stack<ISyncNode>>();

		partialOrderMap.clear();
		lockPairsMap.clear();
		threadLockPairs.clear();
		
		boolean unPairedUnlock = false;
		HashSet<Long> tidSet = new HashSet<>();
		AbstractNode unPairedLockNode = null;
		
		//thread first node - last node
        for (String addr : syncNodesMap.keySet()) {
            Vector<ISyncNode> nodes = syncNodesMap.get(addr);

            Vector<LockPair> lockPairs = new Vector<LockPair>();
            //save it in a map
            lockPairsMap.put(addr, lockPairs);


            //maintain a list of unmatched wait nodes
            HashMap<Long, WaitNode> waitnodes_unmatched = new HashMap<Long, WaitNode>();
            //during recording
            //should after wait, before notify
            //after lock, before unlock

            for (ISyncNode node : nodes) {
                long tid = node.getTid();

                Vector<LockPair> lps_tid = threadLockPairs.get(tid);
                if (lps_tid == null) {
                    lps_tid = new Vector<LockPair>();
                    threadLockPairs.put(tid, lps_tid);
                }

                long thisGID = node.getGID();
                if (node instanceof StartNode) {
                    long f_tid = Long.valueOf(node.getAddr());//is address the same as tid??
                    AbstractNode fnode = firstNodes.get(f_tid);
                    if (fnode != null) {
                        long fGID = fnode.getGID();
                        //start-begin ordering
                        reachEngine.addEdge(thisGID, fGID);
                    }

                    //add join to partial order map
                    partialOrderMap.put(fnode, (StartNode) node);

                } else if (node instanceof JoinNode) {
                    long l_tid = Long.valueOf(node.getAddr());
                    AbstractNode lnode = lastNodes.get(l_tid);
                    if (lnode != null) {
                        long lGID = lnode.getGID();
                        //end-join ordering
                        reachEngine.addEdge(lGID, thisGID);

                    }

                    //add join to partial order map
                    partialOrderMap.put((JoinNode) node, lnode);

                } else if (node instanceof LockNode) {

                    Stack<ISyncNode> stack = threadSyncStack.get(tid);
                    if (stack == null) {
                        stack = new Stack<ISyncNode>();
                        threadSyncStack.put(tid, stack);
                    }

                    stack.push(node);

                    //
                    if (unPairedUnlock && !tidSet.contains(node.getTid())) {
                        partialOrderMap.put((AbstractNode) node, unPairedLockNode);
                        tidSet.add(node.getTid());
                    }
                } else if (node instanceof UnlockNode) {
                    Stack<ISyncNode> stack = threadSyncStack.get(tid);

                    //assert(stack.size()>0);//this is possible when segmented
                    if (stack == null) {
                        stack = new Stack<ISyncNode>();
                        threadSyncStack.put(tid, stack);
                    }


                    //pair lock/unlock nodes
                    /*
                     * if stack if empty, it means it only has a unlock node
					 * the lock node is in the prefix so that it doesn't appear in the trace
					 * then this unlock event should happen all the first lock node in each thread
					 */
                    if (stack.isEmpty()) {
                        LockPair lp = new LockPair(null, node);
                        lockPairs.add(lp);

                        lps_tid.add(lp);

                        unPairedUnlock = true;
                        tidSet.add(node.getTid());
                        unPairedLockNode = (AbstractNode) node;
                    } else if (stack.size() == 1) {
                        LockPair lp = new LockPair(stack.pop(), node);
                        lockPairs.add(lp);

                        lps_tid.add(lp);

                    } else
                        stack.pop();//handle reentrant lock here

                }
				/*
				 * wait is modeled as a wait-lock pair
				 * there should be a notify node happen first, then it can get the lock
				 * the correct order should be wait(t1)-notify(t2)-lock(t1)
				 */
                else if (node instanceof WaitNode) {//interpret waitnode as unlock-wait, the trace contains another lock node

                    waitnodes_unmatched.put(node.getTid(), (WaitNode) node);

                    //wait is interpreted as unlock-wait
                    //so we pair wait with previos lock

                    Stack<ISyncNode> stack = threadSyncStack.get(tid);
                    //assert(stack.size()>0);
                    if (stack == null) {
                        stack = new Stack<ISyncNode>();
                        threadSyncStack.put(tid, stack);
                    }
                    if (stack.isEmpty()) {
                        LockPair lp = new LockPair(null, node);
                        lockPairs.add(lp);
                        lps_tid.add(lp);
                    } else if (stack.size() == 1) {
                        LockPair lp = new LockPair(stack.pop(), node);
                        lockPairs.add(lp);
                        lps_tid.add(lp);
                    } else
                        stack.pop();//handle reentrant lock here
                } else if (node instanceof NotifyNode) {
                    NotifyNode matchNotifyNode = (NotifyNode) node;

                    long notifyGID = matchNotifyNode.getGID();
                    if (waitnodes_unmatched.size() > 0) {
                        //if this notify node model a notify all event, the waitTid is 0
                        long waitTid = matchNotifyNode.getWaitTid();
                        //if it is notifyAll event
                        if (waitTid == 0) {
								
								/*
								 * Modified the RVRuntime::logNotifyAll()
								 * convert it to a normal Notify Node
								 */

//								Collection<WaitNode> waitnodes = waitnodes_unmatched.values();
//								
//								if (waitnodes != null) {
//									for (Iterator<WaitNode> iterator = waitnodes.iterator(); iterator.hasNext();) {
//										WaitNode wnode = (WaitNode) iterator.next();
//										if (wnode.getTid() == matchNotifyNode.getTid()) {
//											continue;
//										}
//										Vector<AbstractNode> threadNodes = trace.getThreadNodesMap().get(wnode.getTid());
//										int lockNodeIndex = threadNodes.indexOf(wnode)+1;
//										//there must be a lock node following the waitnode
//										AbstractNode lockNode = threadNodes.get(lockNodeIndex);
//										
//										if(!(lockNode instanceof LockNode))
//											System.err.println("trace collection is wrong");
//										
//										//notify-wait ordering
//										reachEngine.addEdge(wnode.getGID(),notifyGID);
//										reachEngine.addEdge(notifyGID,lockNode.getGID());
//			
//										//add wait-notify to partial order map
//			
//										partialOrderMap.put(lockNode, matchNotifyNode);
//									}
//								}
                        } else {
                            WaitNode wnode = waitnodes_unmatched.get(waitTid);
                            //get lock node, and enforce wait-notify-lock
                            if (wnode != null) {
                                Vector<AbstractNode> threadNodes = trace.getThreadNodesMap().get(wnode.getTid());
                                int lockNodeIndex = threadNodes.indexOf(wnode) + 1;
                                //there must be a lock node following the waitnode
                                AbstractNode lockNode = threadNodes.get(lockNodeIndex);

                                if (!(lockNode instanceof LockNode))
                                    System.err.println("trace collection is wrong");

                                //notify-wait ordering
                                reachEngine.addEdge(wnode.getGID(), notifyGID);
                                reachEngine.addEdge(notifyGID, lockNode.getGID());

                                //add wait-notify to partial order map
                                partialOrderMap.put(lockNode, matchNotifyNode);
                            }
                        }


                        //do we need to enforce wait->notify??
//							partialOrderMap.put(matchNotifyNode, wnode);

                        //TODO: handle lost notify


                    }

                }
            }


            //check threadSyncStack
            for (Stack<ISyncNode> stack : threadSyncStack.values()) {
                if (stack.size() > 0)//handle reentrant lock here, only pop the first locking node
                {
                    ISyncNode node = stack.firstElement();
                    LockPair lp = new LockPair(node, null);
                    lockPairs.add(lp);

                    long tid = node.getTid();

                    Vector<LockPair> lps_tid = threadLockPairs.get(tid);
                    lps_tid.add(lp);

                }
            }
        }
		
				
	}
}
