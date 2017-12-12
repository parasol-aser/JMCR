
package edu.tamu.aser.mcr.constraints;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import org.w3c.tools.sexpr.SimpleSExprStream;

import edu.tamu.aser.mcr.config.Configuration;
import edu.tamu.aser.mcr.config.Util;
import edu.tamu.aser.mcr.graph.ReachabilityEngine;

/**
 * Constraint solving with Z3 solver
 * 
 * @author jeffhuang
 *
 */
public class ConstraintsSolving
{
	protected static String SMT = ".smt";
	protected static String OUT = ".smtout";
	protected static String Z3_PATH = "/usr/local/bin/z3";       //the path of z3
	
	private String OS = System.getProperty("os.name").toLowerCase();

	File outFile,smtFile;
	protected List<String> CMD;
	
	public Model model;
	public Vector<String> schedule;
		
	boolean sat;

    long timeout;
	
	public ConstraintsSolving(Configuration config, int id)
	{				
		try{
			init(config,id);
		
		}catch(IOException e)
		{
			System.err.println(e.getMessage());
		}
	}
	/**
	 * initialize solver configuration
	 * @param config
	 * @param id
	 * @throws IOException
	 */
	public void init(Configuration config, int id) throws IOException
	{		
		if(Configuration.Optimize){
			smtFile = Util.newOutFile(config.constraint_outdir,config.tableName +"_opt" +"_"+id+SMT);
	        
			outFile = Util.newOutFile(config.constraint_outdir,config.tableName +"_opt" +"_"+id+OUT);
		}
		else{
			smtFile = Util.newOutFile(config.constraint_outdir,config.tableName +"_"+id+SMT);
	        
			outFile = Util.newOutFile(config.constraint_outdir,config.tableName +"_"+id+OUT);
		}
		

        String[] quotes = config.smt_solver.split(" ");
        boolean inQuote = false;
        CMD = new ArrayList<>();
        
//        if (OS.indexOf("mac") >= 0) {
//        	Z3_PATH = "../z3-osx/bin/z3";
//		} else {
//			Z3_PATH = "../z3-ubuntu/bin/z3";
//		}
        
        CMD.add(Z3_PATH);
        for(String arg: quotes){
        	CMD.add(arg);
        }
        timeout = config.solver_timeout;
	}
	
	/**
	 * solve constraint "msg"
	 * @param msg
	 */
	public void sendMessage(String msg)
	{
		PrintWriter smtWriter = null;
		try{
			smtWriter = Util.newWriter(smtFile, true);
			smtWriter.println(msg);
		    smtWriter.close();
		    
		    //invoke the solver
	        exec(outFile, smtFile.getAbsolutePath());

	        model = GetModel.read(outFile);
	        
	        if(model!=null)
	        {
	        	sat = true;
	        	//schedule = computeSchedule(model);
	        }
	        //String z3OutFileName = z3OutFile.getAbsolutePath();
	        //retrieveResult(z3OutFileName);
		    
	        //delete files
//	        Files.delete(outFile.toPath());
//	        Files.delete(smtFile.toPath());
	        
		}catch(IOException e)
		{
			System.err.println(e.getMessage());

		}
	}
	public boolean isSatisfiable(String msg)
	{
		PrintWriter smtWriter = null;
		try{
			smtWriter = Util.newWriter(smtFile, true);
			smtWriter.println(msg);
		    smtWriter.close();
		    
		    //invoke the solver
	        exec(outFile, smtFile.getAbsolutePath());

				FileInputStream fis = new FileInputStream(outFile);
				SimpleSExprStream p = new SimpleSExprStream(fis);
				p.setListsAsVectors(true);
				
				boolean isSatisfied = false;
				String result = GetModel.readResult(p);

				if("sat".equals(result)) {
					
					isSatisfied =  true;
				}
				
		        //delete files
		        Files.delete(outFile.toPath());
		        Files.delete(smtFile.toPath());
		        
		        return isSatisfied;
		    
		}catch(Exception e)
		{
			System.err.println(e.getMessage());

		}
		
		return false;
	}
	/**
	 * solve constraint "msg"
	 * @param msg
	 * @param endVar 
	 */
	
	/**
	 * add reachEngine and causalConstraint to the parameter
	 * @author Alan
	 */
	public void sendMessage(String msg, String endVar, String wVar, String endVar_prefix, 
			ReachabilityEngine reachEngine, String causalConstraint, Configuration config)
	{
		PrintWriter smtWriter = null;
		try{
			smtWriter = Util.newWriter(smtFile, true);
		  	smtWriter.println(msg);
		    smtWriter.close();
		    
		    //invoke the solver
	        exec(outFile, smtFile.getAbsolutePath());

	        model = GetModel.read(outFile);
	        
	        if(model!=null)
	        {
	        	sat = true;
	        	schedule = computeSchedule(model,endVar, wVar, endVar_prefix, reachEngine, causalConstraint, config);
	        }
	        //String z3OutFileName = z3OutFile.getAbsolutePath();
	        //retrieveResult(z3OutFileName);
	        
	        //delete files
	        Files.delete(outFile.toPath());
	        Files.delete(smtFile.toPath());
	        
		}catch(IOException e)
		{
			System.err.println(e.getMessage());

		}
	}
	/**
	 * Given the model of solution, return the corresponding schedule
	 * 
	 * @param model: 
	 * @param endVar_prefix: the last schedule point that must be included
	 * @return
	 */
	public Vector<String> computeSchedule(Model model,String endVar, String wVar, String endVar_prefix, 
			ReachabilityEngine reachEngine, String causalConstraint,
			Configuration config) {
		
		//Alan
		String constraint[] = causalConstraint.split("\n");
		long gidEndVar = Integer.parseInt(endVar.substring(1));
		
		Vector<String> schedule = new Vector<String>();
		//add endVar
		schedule.add(endVar);
		
		//no constraint -- just endVar in the schedule
		if(model.getMap().isEmpty())return schedule;
		
		//what if end var has no relationship with other nodes
		//in the depNodes? then the solution will not include this var
		//then get(endVar) will return null
		//e.g endVar = x2, x1<x3
		//for this case, I simply make endVar the first one
		int endValue = 0;
		if(model.getMap().get(endVar) != null){
			endValue = (Integer) model.getMap().get(endVar);
		}
		else{
			endValue = -100;
		}
		
		
		int VALUE = endValue;
		
		if(!endVar_prefix.equals("x0"))
		{
		schedule.add(endVar_prefix);

		int endValue_prefix = (Integer) model.getMap().get(endVar_prefix);
		
		if(VALUE<endValue_prefix)
			VALUE = endValue_prefix;
		}
		
		//it is super hard to build prefix under TSO or PSO
		//since it is difficult to make it shortest
		
		Map<String, Object> map = model.getMap();
		Map<String, Integer> newMap = new HashMap<String, Integer>();
		for(String key: map.keySet()){
			int value = (Integer)map.get(key);
			newMap.put(key, value);
			
		}
		//decreasing
		Map<String, Integer> sortedMap = sortByValue(newMap);
		
//		Iterator<Entry<String,Object>> setIt = model.getMap().entrySet().iterator();
		
		Set<Entry<String, Integer>> entrySet = sortedMap.entrySet();
		
		Iterator<Entry<String,Integer>> setIt = entrySet.iterator();
		
		while(setIt.hasNext())
		{
			Entry<String,Integer> entryModel = setIt.next();
			String op = entryModel.getKey();
			int order = entryModel.getValue();
			
			{
				if(order<VALUE)//only add var that value smaller than endValue
				{
					
					/**
					 * besides value should be smaller, 
					 * 1. there should be a reachability  p to this endVar
					 * 2. the nodes reach p
					 */
					
					if (Configuration.mode=="TSO" || Configuration.mode=="PSO") {
						boolean flag = false;
						for (int i = 0; i < schedule.size(); i++) {
							String var = schedule.get(i);
							if(var == endVar){
								long gidFirst = Integer.parseInt(op.substring(1));
								long gidSecond = Integer.parseInt(var.substring(1));
								if (reachEngine.canReach(gidFirst, gidSecond)) {
									flag = true;
									break;
								}
							}
							else if (var == wVar) {
								long gidFirst = Integer.parseInt(op.substring(1));
								long gidSecond = Integer.parseInt(var.substring(1));
								if (reachEngine.canReach(gidFirst, gidSecond)) {
									flag = true;
									break;
								}
							}
						}
						
						if(!flag)continue;
					}
					
					
					for(int i=0;i<schedule.size();i++)
					{
						if(order<(Integer)model.getMap().get(schedule.get(i)))
						{
							if(!schedule.contains(op))
							schedule.insertElementAt(op, i);
							break;
						}
						
					}
				}
			}		
		}	
		return schedule;
	}
	
	public void exec(final File outFile, String file) throws IOException
	{

		//CMD = "ls";
		final List<String> cmds = new ArrayList<String>();
        cmds.addAll(CMD);
        cmds.add(file);

		//for tests
		//System.out.println("here out the cmd:");
		//System.out.println(cmds);
        ProcessBuilder processBuilder = new ProcessBuilder(cmds);
        processBuilder.redirectOutput(outFile);
        processBuilder.redirectErrorStream(true);

        Process process = null;
        try {
        	 process = processBuilder.start();
		} catch (Exception e) {
			// TODO: handle exception
			//System.out.println("process start wrong");
			e.printStackTrace();
			System.exit(-1);
		}
        
        try {
            process.waitFor();
        } catch (InterruptedException e) {
        	e.printStackTrace();
            process.destroy();
            
        }
    }
	
	/**
	 *  //sort the map by the value
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> 
    sortByValue( Map<K, V> map )
	{
	    List<Map.Entry<K, V>> list =
	        new LinkedList<>( map.entrySet() );
	    Collections.sort( list, new Comparator<Map.Entry<K, V>>()
	    {
	        @Override
	        public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
	        {
	            return (o2.getValue()).compareTo( o1.getValue() );
	        }
	    } );
	
	    Map<K, V> result = new LinkedHashMap<>();
	    for (Map.Entry<K, V> entry : list)
	    {
	        result.put( entry.getKey(), entry.getValue() );
	    }
	    return result;
	}
	
}


