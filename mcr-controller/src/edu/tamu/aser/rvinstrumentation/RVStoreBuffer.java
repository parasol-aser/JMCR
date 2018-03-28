/**
 * 
 */
package edu.tamu.aser.rvinstrumentation;

import java.util.HashMap;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import edu.tamu.aser.mcr.graph.Queue;
import edu.tamu.aser.mcr.trace.AbstractNode;
import edu.tamu.aser.mcr.trace.ReadNode;
import edu.tamu.aser.mcr.trace.Trace;
import edu.tamu.aser.mcr.trace.WriteNode;
import edu.tamu.aser.scheduling.strategy.MCRStrategy;

/**
 * @author Alan
 *
 */
public class RVStoreBuffer{
    
    static HashMap<Long, Queue<String>> storeBuffer = new HashMap<Long, Queue<String>>();
    
    //when a syn happens, empty the buffer
    public static void memBar(){
        
    }
    
    //update the buffer
    public static void updateStore(){
        //comparing the field with the SID of the variable in the head of the Queue
    }
    
    //buffer the store
    public static void bufferStore(String InsnInfo) {


        //System.out.println(InsnInfo);
        long tid = Thread.currentThread().getId();
        if(storeBuffer.get(tid)==null){
            Queue<String> q = new Queue<String>();
            q.enqueue(InsnInfo);
            storeBuffer.put(tid, q);
        }
        else{
            storeBuffer.get(tid).enqueue(InsnInfo);
        }
    }

}
