package edu.tamu.aser.runtime;

import edu.tamu.aser.reex.Scheduler;
import edu.tamu.aser.scheduling.strategy.RVPORStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RVPORRunTime {


	private static HashMap<Long,PORVectorClock> threadVC;

	//maintain a list of VCs for each memory

	private static HashMap<String, ArrayList<PORVectorClock>>  vectorClockMap;
	   public static HashMap<Long, String> threadTidNameMap;
	    public static HashMap<Long, Integer> threadTidIndexMap;
	final static String MAIN_NAME = "0";
    //race detect
    public static RaceDetect rd;
	public static String getThreadCanonicalName(long tid)
	{
	    return threadTidNameMap.get(tid);
	}
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
           rd = new RaceDetect(tid);

	       threadVC = new HashMap<Long,PORVectorClock>();
	       vectorClockMap = new HashMap<String, ArrayList<PORVectorClock>>();

	    PORVectorClock vc= new PORVectorClock(tid);
	    vc.increment(tid);
	    threadVC.put(tid,vc);

        threadTidNameMap = new HashMap<Long, String>();
        threadTidNameMap.put(tid, MAIN_NAME);
        threadTidIndexMap = new HashMap<Long, Integer>();
        threadTidIndexMap.put(tid, 1);	}

	public static void logFieldAcc(int ID, final Object o, int SID,
			final Object v, final boolean write) {

	       //Scheduler.beforeFieldAccess(!write, "owner", "name", "desc");

        long tid = Thread.currentThread().getId();


	    String addr = System.identityHashCode(o) + "_" + SID;


	    //race detect
	    rd.dataAccess(tid, addr, ID, write);
	    //

        PORVectorClock currentVC = threadVC.get(tid);

        ArrayList<PORVectorClock> vcList = vectorClockMap.get(addr);
	    if(vcList==null)
	    {  vcList = new ArrayList<PORVectorClock>();
	    	       vectorClockMap.put(addr,vcList);

	    }

	    if(vcList.size()>0)
	    {

	       for(int i=vcList.size()-1;i>=0;i--)
	       {
	           PORVectorClock vc = vcList.get(i);

	            if(!vc.isLessThan(currentVC))
	            {
	                //CONFLICT

	                RVPORStrategy.addBackTrack(vc.getIndex());

	                //don't repeat the same backtrack point
	                vcList.remove(i);

	                //just the most recent
	                //break;
	            }

	            //vc.join(currentVC);

	            //currentVC.join(vc);

	        }

	    }

	        PORVectorClock vc= new PORVectorClock(currentVC);
	        vcList.add(vc);

	        vc.setIndex(RVPORStrategy.currentIndex);

	}

	public static void logInitialWrite(int ID, final Object o, int index,
			final Object v) {

	       String addr = System.identityHashCode(o) + "_" + index;
	        PORVectorClock currentVC = threadVC.get(Thread.currentThread().getId());

	        ArrayList<PORVectorClock> vcList = vectorClockMap.get(addr);
	        if(vcList==null)
	        {  vcList = new ArrayList<PORVectorClock>();
	                   vectorClockMap.put(addr,vcList);

	        }

	        vcList.add(new PORVectorClock(currentVC));

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


	    long tid = Thread.currentThread().getId();

	    Thread t = (Thread) o;
	       long tid_t = t.getId();


	        //race detect
	        rd.startThread(tid, tid_t);
	        //



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

	public static void logAfterStart(int ID, final Object o) {

	}

	public static void logJoin(int ID, final Object o) {
        try {
            Scheduler.performJoin((Thread) o);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long tid = Thread.currentThread().getId();

        PORVectorClock currentVC = threadVC.get(tid);

        Thread t = (Thread) o;
        PORVectorClock vc = threadVC.get(t.getId());

        currentVC.join(vc);


        //race detect
        rd.joinThread(tid, t.getId());
        //
	}
    public static void logSleep()
    {
           try {
                Scheduler.performSleep();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
	public static void logWait(int ID, final Object o) {

        //logLock(ID,o,false);

        int wait = 0;
        try {
            wait = Scheduler.performOnlyWait(o);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}

	public static void logNotify(int ID, final Object o) {
        //logLock(ID,o,false);
        long notifiedThreadId = Scheduler.performNotify(o);

	}

	public static void logNotifyAll(int ID, final Object o) {
        //logLock(ID,o,false);
        Scheduler.performNotifyAll(o);

	}

	public static void logStaticSyncLock(int ID, int SID) {

        Scheduler.performLock(getObject(SID));

	}

	public static void logStaticSyncUnlock(int ID, int SID) {
        Scheduler.performUnlock(getObject(SID));

	}

	public static void logLock(int ID, final Object lock) {

	       Scheduler.performLock(lock);

long tid = Thread.currentThread().getId();


        String addr = System.identityHashCode(lock)+"";



        //race detect
        rd.lockAccess(tid, addr);
        //



        PORVectorClock currentVC = threadVC.get(tid);

        ArrayList<PORVectorClock> vcList = vectorClockMap.get(addr);
        if(vcList==null)
        {  vcList = new ArrayList<PORVectorClock>();
                   vectorClockMap.put(addr,vcList);

        }

        if(vcList.size()>0)
        {

           for(int i=vcList.size()-1;i>=0;i--)
           {
               PORVectorClock vc = vcList.get(i);

                if(!vc.isLessThan(currentVC))
                {
                    //CONFLICT

                    RVPORStrategy.addBackTrack(vc.getIndex());
                    //don't repeat the same backtrack point
                    vcList.remove(i);
                  //just the most recent
                   //break;
                }


            }

        }

            PORVectorClock vc= new PORVectorClock(currentVC);
            vcList.add(vc);

            vc.setIndex(RVPORStrategy.currentIndex);
	}

	public static void logUnlock(int ID, final Object lock) {


        Scheduler.performUnlock(lock);


	    long tid = Thread.currentThread().getId();

        String addr = System.identityHashCode(lock)+"";

	    //race detect
        rd.lockAccess(tid, addr);
        //
	}

	public static void logArrayAcc(int ID, final Object o, int index,
			final Object v, final boolean write) {
        //Scheduler.beforeArrayAccess(!write);

	    logFieldAcc(ID,o,index,v,write);

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
