package edu.tamu.aser.scheduling.strategy;

import java.util.IdentityHashMap;

import edu.tamu.aser.internaljuc.Reex_Semaphore;
import edu.tamu.aser.scheduling.events.EventDesc;
import edu.tamu.aser.scheduling.events.LocationDesc;


public class ThreadInfo implements Comparable<ThreadInfo> {

    private final Thread thread;
    private final Reex_Semaphore pausingSemaphore;
    private IdentityHashMap<Object, Integer> lockSet;
    private boolean parkPermitAvailable;
    private EventDesc eventDesc;
    private LocationDesc locationDesc;
    private int runCount;

    public ThreadInfo(Thread thread, EventDesc eventDesc) {
        this.thread = thread;
        this.pausingSemaphore = new Reex_Semaphore(0);
        this.lockSet = new IdentityHashMap<Object, Integer>();
        this.eventDesc = eventDesc;
        this.runCount = 0;
        this.parkPermitAvailable = false;
    }

    public ThreadInfo(Thread thread) {
        this(thread, null);
    }

    public Thread getThread() {
        return thread;
    }

    public Reex_Semaphore getPausingSemaphore() {
        return pausingSemaphore;
    }

    public boolean isParkPermitAvailable() {
        return parkPermitAvailable;
    }

    public void setParkPermitAvailable(boolean availability) {
        parkPermitAvailable = availability;
    }

    public int getLockCount(Object lockObject) {
        Integer lockCount = lockSet.get(lockObject);
        if (lockCount == null) {
            lockCount = 0;
        }
        return lockCount;
    }

    public void acquiredLock(Object lockObject, int numLocks) {
        Integer lockCount = lockSet.get(lockObject);
        if (lockCount == null) {
            lockCount = 0;
        }
        lockCount += numLocks;
        lockSet.put(lockObject, lockCount);
    }

    public int releasedLock(Object lockObject, int numLocks) {
        Integer lockCount = lockSet.get(lockObject);
        if (lockCount == null || lockCount - numLocks < 0) {
            //throw new IllegalStateException("Attempting to release lock that has was not acquired");
            System.out.println("Attempting to release lock that has was not acquired");
            return lockCount;
        }
        lockCount -= numLocks;
        lockSet.put(lockObject, lockCount);
        return lockCount;
    }

    public EventDesc getEventDesc() {
        return eventDesc;
    }

    public void setEventDesc(EventDesc eventDesc) {
        this.eventDesc = eventDesc;
    }

    public LocationDesc getLocationDesc() {
        return locationDesc;
    }

    public void setLocationDesc(LocationDesc latestPC) {
        this.locationDesc = latestPC;
    }

    public int incrementRunCount() {
        return ++this.runCount;
    }

    public int decrementRunCount() {
        return --this.runCount;
    }

    public int getRunCount() {
        return this.runCount;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((thread == null) ? 0 : thread.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ThreadInfo other = (ThreadInfo) obj;
        if (thread == null) {
            if (other.thread != null)
                return false;
        } else if (!thread.equals(other.thread))
            return false;
        return true;
    }

    @Override
    public int compareTo(ThreadInfo o) {
        int idComparision = ((Long) getThread().getId()).compareTo(o.getThread().getId());
        return idComparision == 0 ? getThread().getName().compareTo(o.getThread().getName()) : idComparision;
    }

    @Override
    public String toString() {
        return "ThreadInfo [thread=" + thread + ", eventDesc=" + eventDesc + ", locationDesc=" + locationDesc + "]\n";
    }

}
