package edu.tamu.aser.runtime;

import java.util.HashMap;
import java.util.Map.Entry;

public class PORVectorClock {
    
    public HashMap<Long,Integer> vc;
    int index;
    int ID;
    //boolean write;
    public void setIndex(int i)
    {
        this.index = i;
    }
    public int getIndex()
    {
        return this.index;
    }
    //for field access only
    public void setID(int i)
    {
        this.ID = i;
    }
    public int getID()
    {
        return this.ID;
    }
    //for field access only
//    public void setWrite()
//    {
//        this.write = true;
//    }
//    public boolean isWrite()
//    {
//        return write;
//    }
    public PORVectorClock(long tid)
    {
        vc = new HashMap<Long,Integer>();
        vc.put(tid, 0);
    }
    
    public PORVectorClock(PORVectorClock vectorclock)
    {
        vc = new HashMap<Long,Integer>(vectorclock.vc);
    }
    
    public void increment(long tid)
    {
        int clock = vc.get(tid);
        vc.put(tid, ++clock);
    }
    
    public void join(PORVectorClock vc2){
        
        for(Entry<Long, Integer> entry : vc2.vc.entrySet() )
        {
            Long tid = entry.getKey();
            Integer value = entry.getValue();
            
            Integer clock = vc.get(tid);
            if(clock==null||clock<value)
                vc.put(tid, value);
        }
    }
    public boolean isLessThan(PORVectorClock vc2)
    {
        for(Entry<Long, Integer> entry : vc.entrySet() )
        {
            Long tid = entry.getKey();
            Integer value = entry.getValue();
            
            Integer clock = vc2.vc.get(tid);
            
            if(clock<value)
                return false;
        }
        return true;
    }

    public void setZero(long tid) {
       
        vc.put(tid, 0);
    }
}
