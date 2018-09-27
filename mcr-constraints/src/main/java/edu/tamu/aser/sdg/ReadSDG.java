package edu.tamu.aser.sdg;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.tamu.aser.config.Configuration;
import edu.tamu.aser.graph.ReachabilityEngine;

public class ReadSDG {
	
//	public static ReadSDG instance = new ReadSDG();
	
	public static Map<Integer, Set<Integer>> readSDG(){
		
//		System.out.println("Reading SDG...");
		Map<Integer, Set<Integer>> sdg = new HashMap<Integer, Set<Integer>>();
		FileInputStream fis;
		try {
			String fileName = System.getProperty("user.dir") + "/SDGs/" + Configuration.class_name + ".sdg";
			System.out.println("Reading SDG from "+fileName);
			fis = new FileInputStream(fileName);
			ObjectInputStream ois = new ObjectInputStream(fis);
	        sdg = (HashMap) ois.readObject();
	        ois.close();
	        fis.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Reading SDG... Done!");
		
		
		
//		Properties p = new Properties();
//		try {
//			p.load(new FileInputStream("/Users/Alan/workspace_java/aser-engine/SDG"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		for (String key : p.stringPropertyNames()) {
//			   sdg.put(key, (Set<String>) p.get(key));
//			}
		
		return sdg;
	}
	
	public static ReachabilityEngine ConstructReachability(Map<Integer, Set<Integer>> sdg){
		ReachabilityEngine reachSDG = new ReachabilityEngine();
		
		for (Map.Entry<Integer, Set<Integer>> entry:sdg.entrySet()){
			Integer key = entry.getKey();
			Set<Integer> valueSet = entry.getValue();
			
			Iterator<Integer> itr = valueSet.iterator();
			while(itr.hasNext()){
				Integer value = itr.next();
				reachSDG.addEdge(key, value);
			}
		}
		
		//test
//		reachSDG.addEdge(1, 2);
//		reachSDG.addEdge(2, 3);
//		reachSDG.addEdge(2, 4);
		
		return reachSDG;
	}
	
public static Map<String, Integer> NodeToId(){
		
//		System.out.println("Reading label_id...");
		Map<String, Integer> nodeToId = new HashMap<>();
		FileInputStream fis;
		try {
			String fileName = System.getProperty("user.dir") + "/SDGs/" + Configuration.class_name + ".label_id";
			System.out.println("Reading label_id from "+fileName);
			fis = new FileInputStream(fileName);
			ObjectInputStream ois = new ObjectInputStream(fis);
			nodeToId = (HashMap) ois.readObject();
	        ois.close();
	        fis.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Reading label_id... Done!");
		
		return nodeToId;
	}
}
