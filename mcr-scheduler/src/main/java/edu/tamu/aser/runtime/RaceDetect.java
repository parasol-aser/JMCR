package edu.tamu.aser.runtime;

import edu.tamu.aser.ExploreSeedInterleavings;
import edu.tamu.aser.race.Race;

import java.util.HashMap;
import java.util.Map;

public class RaceDetect {

    HashMap<Long,PORVectorClock> threadVC = new HashMap<Long,PORVectorClock>();
    HashMap<String, PORVectorClock>  readVCMap = new HashMap<String,PORVectorClock>();
    HashMap<String, PORVectorClock>  writeVCMap = new HashMap<String,PORVectorClock>();
    HashMap<String, PORVectorClock>  lockVCMap = new HashMap<String,PORVectorClock>();

    public RaceDetect(long tid)
    {
        //initialize the main thread's vc
        PORVectorClock vc= new PORVectorClock(tid);
        vc.increment(tid);
        threadVC.put(tid,vc);
    }
    private void addRace(int ID1, int ID2)
    {
        Race r = new Race(ID1,ID2);
       if( ExploreSeedInterleavings.races.add(r))
           //System.err.println("Races ("+RVCausalTest.races.size()+"): "+r)
           ;

    }
    public void dataAccess(long tid, String addr, int ID/*location*/, boolean write)
    {
        PORVectorClock currentVC = threadVC.get(tid);

        PORVectorClock rvc = readVCMap.get(addr);
        PORVectorClock wvc = writeVCMap.get(addr);

        if(write){
            if(rvc!=null&&!rvc.isLessThan(currentVC))
            {
                //Race: add ID and ID
                {
                   addRace(ID,rvc.getID());
                }
            }
            
            if(wvc!=null)
            {
                if(!wvc.isLessThan(currentVC))
                {
                    addRace(ID,wvc.getID());
                }
              
                wvc.join(currentVC);
                
            }
            else
            {
                wvc= new PORVectorClock(currentVC);
                writeVCMap.put(addr, wvc);
            }
            
            wvc.setID(ID);
        }
        else
        {
            if(wvc!=null)
            {
                if(!wvc.isLessThan(currentVC))
                {
                    addRace(ID,wvc.getID());
                    currentVC.join(wvc);

                }
                

            }
            
            
            //read
            if(rvc==null)
            {
                rvc = new PORVectorClock(currentVC);
                readVCMap.put(addr, rvc);
            }

            rvc.setID(ID);
        }

    }
    
    public void lockAccess(long tid, String addr)
    {
        PORVectorClock currentVC = threadVC.get(tid);
        PORVectorClock vc = lockVCMap.get(addr);
        if(vc==null){
            
            vc= new PORVectorClock(currentVC);
            lockVCMap.put(addr, vc);
        }
        else
        {
                //update thread vc
                currentVC.join(vc);
                vc.join(currentVC);
                
        }
        
        currentVC.increment(tid);

    }
    public void startThread(long tid, long tid_t)
    {
        PORVectorClock currentVC = threadVC.get(tid);
        currentVC.setZero(tid_t);
        
        PORVectorClock vc = new PORVectorClock(currentVC);
        vc.increment(tid_t);
        threadVC.put(tid_t, vc);
        
        currentVC.increment(tid);
           
        {//set all the other thread's vector clocks to zero 
            for(Map.Entry<Long, PORVectorClock> entry: threadVC.entrySet())
            {
                if(entry.getKey()!=tid_t&&entry.getKey()!=tid)
                {
                    entry.getValue().setZero(tid_t);
                }
            }
                
        }
    }
    public void joinThread(long tid, long tid_t)
    {
        PORVectorClock currentVC = threadVC.get(tid);
        
        PORVectorClock vc = threadVC.get(tid_t);
        
        currentVC.join(vc);
        currentVC.increment(tid);
    }
}
