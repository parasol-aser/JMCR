package edu.tamu.aser.reex;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.tamu.aser.runtime.RVRunTime;
import edu.tamu.aser.scheduling.MCRProperties;
import edu.tamu.aser.scheduling.strategy.DefaultStrategy;
import sun.misc.Unsafe;
import edu.illinois.imunit.internal.parsing.BlockEvent;
import edu.illinois.imunit.internal.parsing.Event;
import edu.illinois.imunit.internal.parsing.Name;
import edu.illinois.imunit.internal.parsing.Ordering;
import edu.illinois.imunit.internal.parsing.Orderings;
import edu.illinois.imunit.internal.parsing.SimpleEvent;
import edu.tamu.aser.internaljuc.MyUnsafe;
import edu.tamu.aser.internaljuc.Reex_Condition;
import edu.tamu.aser.internaljuc.Reex_ReentrantLock;
import edu.tamu.aser.internaljuc.Reex_Semaphore;
import edu.tamu.aser.internaljuc.Reex_TimeUnit;
import edu.tamu.aser.listeners.Listeners;
import edu.tamu.aser.scheduling.events.ArrayAccessEventDesc;
import edu.tamu.aser.scheduling.events.BlockedForIMUnitEventDesc;
import edu.tamu.aser.scheduling.events.BlockedForThreadBlockDesc;
import edu.tamu.aser.scheduling.events.EventDesc;
import edu.tamu.aser.scheduling.events.EventType;
import edu.tamu.aser.scheduling.events.FieldAccessEventDesc;
import edu.tamu.aser.scheduling.events.JoinEventDesc;
import edu.tamu.aser.scheduling.events.LocationDesc;
import edu.tamu.aser.scheduling.events.LockEventDesc;
import edu.tamu.aser.scheduling.events.ParkUnparkEventDesc;
import edu.tamu.aser.scheduling.events.ThreadLifeEventDesc;
import edu.tamu.aser.scheduling.events.WaitNotifyEventDesc;
import edu.tamu.aser.scheduling.filtering.DefaultFilter;
import edu.tamu.aser.scheduling.filtering.SchedulingFilter;
import edu.tamu.aser.scheduling.strategy.ChoiceType;
import edu.tamu.aser.scheduling.strategy.SchedulingStrategy;
import edu.tamu.aser.scheduling.strategy.ThreadInfo;

/**
 * Class that orchestrates threads so that they can be scheduled using custom
 * {@link SchedulingStrategy}s. The instrumentation hooks all call into this
 * class.
 * 
 */
public class Scheduler {

    private static final String BANG = "!";
    private static final String UNABLE_TO_OBTAIN_INSTANCE_OF = "Unable to obtain instance of: ";

    /******************************************************************
     ****************************************************************** 
     *********************** SCHEDULER STATE **************************
     ****************************************************************** 
     ******************************************************************/

    private static Map<Thread, ThreadInfo> liveThreadInfos;
    private static SortedSet<ThreadInfo> pausedThreadInfos;
    private static Set<ThreadInfo> blockedThreadInfos;
    private static final Reex_ReentrantLock schedulerStateLock = new Reex_ReentrantLock();
    private static final Reex_Condition schedulerWakeupCondition = schedulerStateLock.newCondition();
    private static final String AT = "@";
    private static final Map<String, Set<Event>> currentOrderings = new HashMap<>();
    private static final Map<String, Thread> currentHappenedEvents = new HashMap<>();

    private final static Reex_Semaphore deadlockOrFinishNotifier = new Reex_Semaphore(0);

    /**
     * {@link SchedulingStrategy} to be used for scheduling decisions.
     */
    private static SchedulingStrategy schedulingStrategy;

    /**
     * {@link SchedulingFilter} to be used for scheduling decisions.
     */
    private static SchedulingFilter schedulingFilter;
    private static boolean exploring = false;

    private static final Unsafe unsafe = MyUnsafe.getUnsafe();

    /*
     * Initialize state before everything.
     */
    static {
        initState();

        // Catch any uncaught exception thrown by any thread
        // NOTE: this can be overridden by the code under test
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            /*
             * This method will execute in the context of the thread that raised
             * the exception
             */
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (e != null) {
                    e.printStackTrace();
                }
                if(e instanceof NullPointerException)//TODO: JEFF
                {
//                    StringBuilder message = new StringBuilder();
//                    for( StackTraceElement traceElement : e.getStackTrace())
//                        message.append(traceElement.toString()).append("\n");

//                    edu.tamu.aser.reex.JUnit4MCRRunner.npes.add(message.toString());
                    failureDetected(null);
                    Listeners.fireCompletedExploration();
                    Scheduler.endThread();
                }
                else if (e instanceof ConcurrentModificationException) {
//                    StringBuilder message = new StringBuilder();
//                    for( StackTraceElement traceElement : e.getStackTrace()) {
//                        message.append(traceElement.toString()).append("\n");
//                    }

//                    edu.tamu.aser.reex.JUnit4MCRRunner.npes.add(message.toString());
                    failureDetected(null);
                    Listeners.fireCompletedExploration();
                    Scheduler.endThread();
                } else {
                    failureDetected(null);
                    Listeners.fireCompletedExploration();
                    System.exit(2);
                }
            }
        });


        /* Set the scheduling strategy to be used */
        MCRProperties prop = MCRProperties.getInstance();
        String schedulingStrategyClassName = prop.getProperty(MCRProperties.SCHEDULING_STRATEGY_KEY);

        if (schedulingStrategyClassName != null) {
            try {
                schedulingStrategy = (SchedulingStrategy) Class.forName(schedulingStrategyClassName).newInstance();
                System.out.println("\nUsing the following scheduling strategy for exploration: " + schedulingStrategy.getClass().getName());
            } catch (Exception e) {
                System.err.println(UNABLE_TO_OBTAIN_INSTANCE_OF + schedulingStrategyClassName + BANG);
                e.printStackTrace();
                System.exit(2);
            }
        } 
        else {
            System.out.println("\nWARNING: no value specified for property \"" + MCRProperties.SCHEDULING_STRATEGY_KEY + "\"");
            System.out.println("WARNING: using \"" + DefaultStrategy.class.getName() + "\", which will choose only one possible schedule");
            System.out.println("NOTE: See the \"edu.tamu.aser.scheduling.strategy\" package for a list of provided strategies\n");
            schedulingStrategy = new DefaultStrategy();
        }

        /* Set the scheduling filter to be used */
        String schedulingFilterClassName = prop.getProperty(MCRProperties.SCHEDULING_FILTER_KEY);
        if (schedulingFilterClassName != null) {
            try {
                schedulingFilter = (SchedulingFilter) Class.forName(schedulingFilterClassName).newInstance();
            } catch (Exception e) {
                System.err.println(UNABLE_TO_OBTAIN_INSTANCE_OF + schedulingFilterClassName + BANG);
                e.printStackTrace();
                System.exit(2);
            }
        } else {
            schedulingFilter = new DefaultFilter();
        }
    }
    
    /**
     * Daemon thread for scheduling
     */
    static {
        Thread schedulerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean timeout = false;
                while (true) {  
                    schedulerStateLock.lock();
                    try {
                        if (!liveThreadInfos.isEmpty()) {
                            if (liveThreadInfos.size() == blockedThreadInfos.size()) {
                                // notify the runner of deadlock
                                deadlockOrFinishNotifier.release();
                            }
                            if (!pausedThreadInfos.isEmpty() && 
                                    pausedThreadInfos.size() == liveThreadInfos.size() - blockedThreadInfos.size()) {
                                
                                ThreadInfo chosenPausedThreadInfo = (ThreadInfo) choose(pausedThreadInfos, ChoiceType.THREAD_TO_SCHEDULE);
                                                           
                                //System.out.println("Choose thread info:" + chosenPausedThreadInfo.toString() );
                                
                                if(chosenPausedThreadInfo!=null)
                                {
                                    pausedThreadInfos.remove(chosenPausedThreadInfo);
                                    chosenPausedThreadInfo.getPausingSemaphore().release();
                                }
                                //something wrong
                                //just release the lock, wait for the thread to be added to the paused thread
                            }
                            else if(timeout && !pausedThreadInfos.isEmpty())//JEFF
                            {
                                ThreadInfo chosenPausedThreadInfo = (ThreadInfo) choose(pausedThreadInfos, ChoiceType.THREAD_TO_FAIR);
                                if(chosenPausedThreadInfo!=null)
                                {
                                    pausedThreadInfos.remove(chosenPausedThreadInfo);
                                    chosenPausedThreadInfo.getPausingSemaphore().release();

                                }
                            }
                        }
                        //JEFF: in case some blocking operation not tracked
                       timeout = !schedulerWakeupCondition.await(500, Reex_TimeUnit.MILLISECONDS);
                        
                    } catch (Throwable exp) {
                        System.out.flush();
                        System.err.println("Uncaught exception in scheduler thread:");
                        exp.printStackTrace(System.err);
                        System.exit(2);
                    } finally {
                        //schedulerWakeupCondition.awaitUninterruptibly();

                        schedulerStateLock.unlock();
                    }
                } //end while
            }
        }, "Scheduler");
        schedulerThread.setDaemon(true);
        schedulerThread.start();
    }

    /**
     * Should be called before a new exploration is going to be performed.
     * 
     * 1) Informs the scheduling strategy that is being used that a new
     * exploration will be performed. <br/>
     * 2) Prepares for a schedule execution.
     */
    public static void startingExploration(String name) {
        Listeners.fireStartingExploration(name);
        schedulingStrategy.startingExploration();
    }

    /**
     * Should be called after an exploration has been performed.
     * 
     * 1) Informs the listeners that the exploration has completed
     */
    public static void completedExploration() {
        Listeners.fireCompletedExploration();
    }

    /**
     * Should be called before a new schedule is going to be executed (during
     * exploration). Does the following:
     * 
     * 1) (Re)Initializes the scheduler state. <br/>
     * 2) Informs the scheduling strategy that is being used that a new schedule
     * will be executed.
     */
    public static void startingScheduleExecution() {
        
//        long tid = Thread.currentThread().getId();
//        JUnit4MCRRunner.rd = new RaceDetect(tid);        
        Listeners.fireStartingSchedule();
        schedulerStateLock.lock();
        try {
            resetState();
            schedulingStrategy.startingScheduleExecution();
            exploring = true;
        } finally {
            schedulerStateLock.unlock();
        }
    }

    /**
     * Helper method to reinitialize non-final members for each schedule.
     */
    private static void initState() {
        if (Listeners.debugExploration) {
            // Use ordered map when debugging to eliminate print output
            // non-determinism
            liveThreadInfos = new TreeMap<Thread, ThreadInfo>(new Comparator<Thread>() {
                @Override
                public int compare(Thread o1, Thread o2) {
                    int idComparision = ((Long) o1.getId()).compareTo(o2.getId());
                    return idComparision == 0 ? o1.getName().compareTo(o2.getName()) : idComparision;
                }
            });
        } else {
            // Use efficient map when not debugging
            liveThreadInfos = new HashMap<Thread, ThreadInfo>();
        }
        pausedThreadInfos = new TreeSet<ThreadInfo>();
        blockedThreadInfos = new HashSet<ThreadInfo>();
        informSchedulerOfCurrentThread();
        currentHappenedEvents.clear();
    }

    private static void resetState() {
        liveThreadInfos.clear();
        pausedThreadInfos.clear();
        blockedThreadInfos.clear();
        informSchedulerOfCurrentThread();
    }

    private static void informSchedulerOfCurrentThread() {
        Thread currentThread = Thread.currentThread();
        liveThreadInfos.put(currentThread, new ThreadInfo(currentThread));
    }

    /**
     * Should be called at the end of a schedule execution (during exploration).
     * Does the following:
     * 
     * 1) Informs the scheduling strategy that is being used that a new schedule
     * will be executed.
     */
    public static void completedScheduleExecution() {
        // TODO: schedulerStateLock.lock();
        /* Trying to make sure all threads are joined */
        while (!pausedThreadInfos.isEmpty()) {
            System.out.flush();
            //System.err.println(pausedThreadInfos.size() + " thread(s) not joined! Trying to force join(s).");
            ThreadInfo joinThreadInfo = pausedThreadInfos.first();
            if (joinThreadInfo != null) {
                try {
                    performJoin(joinThreadInfo.getThread());
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        // TODO: schedulerStateLock.unlock();
        /* Current schedule is done */
        exploring = false;
        
        //what does this mean?
        Listeners.fireCompletedSchedule(schedulingStrategy.getChoicesMadeDuringThisSchedule());
        schedulingStrategy.completedScheduleExecution();
    }

    /**
     * Returns the {@link SchedulingStrategy} being used to make scheduling
     * decisions.
     * 
     * @return the {@link SchedulingStrategy} being used to make scheduling
     *         decisions.
     */
    public static SchedulingStrategy getSchedulingStrategy() {
        return schedulingStrategy;
    }

    /**
     * Returns the {@link SchedulingFilter} being used to filter scheduling
     * choices.
     * 
     * @return the {@link SchedulingFilter} being used to filter scheduling
     *         choices.
     */
    public static SchedulingFilter getSchedulingFilter() {
        return schedulingFilter;
    }

    /**
     * Returns the {@link Map} of currently live {@link Thread}s to their
     * {@link ThreadInfo}s.
     * 
     * @return the {@link Map} of currently live {@link Thread}s to their
     *         {@link ThreadInfo}s.
     */
    public static Map<Thread, ThreadInfo> getLiveThreadInfos() {
        if (Listeners.debugExploration) {
            return Collections.unmodifiableSortedMap((SortedMap<Thread, ThreadInfo>) liveThreadInfos);
        }
        return Collections.unmodifiableMap(liveThreadInfos);
    }

    /**
     * Returns the {@link Map} of currently paused {@link Thread}s to their
     * {@link ThreadInfo}s.
     * 
     * @return the {@link Map} of currently paused {@link Thread}s to their
     *         {@link ThreadInfo}s.
     */
    public static Set<ThreadInfo> getPausedThreadInfos() {
        if (Listeners.debugExploration) {
            return Collections.unmodifiableSortedSet((SortedSet<ThreadInfo>) pausedThreadInfos);
        }
        return Collections.unmodifiableSet((SortedSet<ThreadInfo>) pausedThreadInfos);
    }

    public static boolean canExecuteMoreSchedules() {
        return schedulingStrategy.canExecuteMoreSchedules();
    }

    public static void failureDetected(String errorMsg) {
        Listeners.fireFailureDetected(errorMsg, schedulingStrategy.getChoicesMadeDuringThisSchedule());
    }

    public static Reex_Semaphore getTerminationNotifer() {
        return deadlockOrFinishNotifier;
    }

    /******************************************************************
     ****************************************************************** 
     **************** SCHEDULER THREAD & CHOICE POINT *****************
     ****************************************************************** 
     ******************************************************************/

    

    /**
     * Ask the {@link SchedulingStrategy} to choose one among the given
     * {@link Object}s. The {@link ChoiceType} specifies what is being chosen.
     * 
     * @param objectChoices
     *            {@link SortedSet} of {@link Object}s to choose from
     * 
     * @param choiceType
     *            {@link ChoiceType} indicating what is being chosen
     * 
     * @return the choice.
     */
    public static Object choose(SortedSet<? extends Object> objectChoices, ChoiceType choiceType) {
        if (objectChoices.isEmpty()) {
            throw new IllegalArgumentException("There has to be at least one choice i.e. objectChoices cannot be empty");
        }
        schedulerStateLock.lock();
        try {
            objectChoices = schedulingFilter.filterChoices(objectChoices, choiceType);
            Object chosenObject;
            if (objectChoices.size() == 1) {  //when there is only one thread, it has to execute this thread
                chosenObject = objectChoices.first();
                // System.out.println("Choose only:" + ((ThreadInfo)chosenObject).getThread().getId());
                
            } else {
                Listeners.fireMakingChoice(Collections.unmodifiableSortedSet(objectChoices), choiceType);
                chosenObject = schedulingStrategy.choose(Collections.unmodifiableSortedSet(objectChoices), choiceType);            
                Listeners.fireChoiceMade(chosenObject);
            }
            return chosenObject;
        } finally {
            schedulerStateLock.unlock();
        }
    }
    
    /******************************************************************
     ****************************************************************** 
     ***************** INSTRUMENTATION HOOKS **************************
     ****************************************************************** 
     ******************************************************************/

    /**
     * Executed before a (currently executing) parent thread forks a child
     * thread (using {@link Thread}.start).
     * 
     * @param childThread
     *            the {@link Thread} that will be forked
     */
    public static void beforeForking(Thread childThread) {
        beforeEvent(new ThreadLifeEventDesc(EventType.BEGIN), true);
        if (exploring) {
            schedulerStateLock.lock();
            try {
                ThreadInfo currentThreadInfo = liveThreadInfos.get(Thread.currentThread());
                currentThreadInfo.setEventDesc(new ThreadLifeEventDesc(EventType.FORK, childThread));
                ThreadInfo childThreadInfo = new ThreadInfo(childThread);
                liveThreadInfos.put(childThread, childThreadInfo);
                Listeners.fireBeforeForking(childThreadInfo);
            } finally {
                schedulerStateLock.unlock();
            }
        }
    }

    /**
     * Executed after a (currently executing) parent thread forks a child thread
     * (using {@link Thread}.start).
     * 
     * @param childThread
     *            the {@link Thread} that has been forked
     */
    public static void afterForking(Thread childThread) {
    	afterEvent(new ThreadLifeEventDesc(EventType.BEGIN));
    }

    /**
     * Executed at the beginning of a {@link Runnable}'s run method.
     */
    public static void beginThread() {
        if (exploring) {
            schedulerStateLock.lock();
            try {
                ThreadInfo currentThreadInfo = liveThreadInfos.get(Thread.currentThread());
                if(currentThreadInfo!=null)
                {                
                    currentThreadInfo.incrementRunCount();
                    currentThreadInfo.setEventDesc(new ThreadLifeEventDesc(EventType.BEGIN));
                }
            } finally {
                schedulerStateLock.unlock();
            }
        }
    }

    /**
     * Executed at the end of a {@link Runnable}'s run method.
     */
    public static void endThread() {
        if (exploring) {
            schedulerStateLock.lock();
            try {
                Thread currentThread = Thread.currentThread();
                ThreadInfo currentThreadInfo = liveThreadInfos.get(currentThread);
                if(currentThreadInfo!=null)
                {
                    currentThreadInfo.setEventDesc(new ThreadLifeEventDesc(EventType.END));
                    int newRunCount = currentThreadInfo.decrementRunCount();
                    if (newRunCount == 0) {
                        Set<ThreadInfo> joiningThisThreadInfos = new HashSet<ThreadInfo>();
                        for (ThreadInfo blockedThreadInfo : blockedThreadInfos) {
                            if (blockedThreadInfo.getEventDesc().getEventType().equals(EventType.JOIN)) {
                                JoinEventDesc joinEventDesc = (JoinEventDesc) blockedThreadInfo.getEventDesc();
                                if (joinEventDesc.getJoinThread().equals(currentThread)) {
                                    joiningThisThreadInfos.add(blockedThreadInfo);
                                    blockedThreadInfo.getPausingSemaphore().release();
                                }
                            }
                        }
                        blockedThreadInfos.removeAll(joiningThisThreadInfos);
                        liveThreadInfos.remove(currentThread);
                    }
                }
            } finally {
                schedulerWakeupCondition.signal();
                schedulerStateLock.unlock();
            }
        }
    }
    
    /**
     * Called before a field is accessed, it first needs to get the lock
     * it is instrumented to the class
     * @param isRead
     * @param owner
     * @param name
     * @param desc
     */
    public static void beforeFieldAccess(boolean isRead, String owner, String name, String desc) {
        if (exploring) {
            beforeEvent(new FieldAccessEventDesc(isRead ? EventType.READ : EventType.WRITE, owner, name, desc), true);
        }
    }
    
    public static void beforeArrayAccess(boolean isRead) {
        if (exploring) {
            beforeEvent(new ArrayAccessEventDesc(isRead ? EventType.READ : EventType.WRITE), true);
        }
    }
    /**
     * Print an error message for unexpected usage of Unsafe class during
     * exploration
     * 
     * @param owner
     *            the class to which the method belongs
     * @param name
     *            the name of the method
     * @param desc
     *            the description of the method
     */
    public static void beforeUnsafeOther(String owner, String name, String desc) {
        if (exploring) {
            System.out.println("Reex internal error: unexpected Unsafe usage while exploring");
            System.out.println("owner=" + owner + ", name=" + name + ", desc=" + desc);
            Thread.dumpStack();
        }
    }

    /**
     * Executed after a field is accessed.
     * 
     * @param isRead
     *            whether the access is going to be a read
     * @param owner
     *            the class to which the field belongs
     * @param name
     *            the name of the field
     * @param desc
     *            the description of the field
     */
    public static void afterFieldAccess(boolean isRead, String owner, String name, String desc) {
        if (exploring) {
            afterEvent(new FieldAccessEventDesc(isRead ? EventType.READ : EventType.WRITE, owner, name, desc));
        }
    }
    
    public static void afterArrayAccess(boolean isRead) {
        if (exploring) {
            afterEvent(new ArrayAccessEventDesc(isRead ? EventType.READ : EventType.WRITE));
        }
    }

    /**
     * Executed instead of a monitor enter (synchronized keyword/block).
     * 
     * @param lockObject
     *            the lock/monitor to be acquired
     */
    @SuppressWarnings("deprecation")
    public static void performLock(Object lockObject) {
        
        Thread.currentThread().getId(); 
        System.identityHashCode(lockObject);
        //race detect
//        JUnit4MCRRunner.rd.lockAccess(tid, addr);
        //
        if (exploring) {
            performLock(lockObject, 1);
        } else {
            unsafe.monitorEnter(lockObject);
        }
    }

    /**
     * Executed instead of a monitor enter (synchronized keyword/block) and used
     * by {@link #performWait(Object)} to re-acquire released lock.
     * 
     * @param lockObject
     *            the lock/monitor to be acquired
     * @param lockCount
     *            number of instances of the lock to be acquired
     */
    public static void performLock(Object lockObject, int lockCount) {
        LockEventDesc lockEventDesc = new LockEventDesc(EventType.LOCK, lockObject);

        beforeEvent(lockEventDesc, true);

//        System.out.println("increase index");
        while (true) {
            ThreadInfo lockingThreadInfo = null;
            boolean lockAvailable = true;
            schedulerStateLock.lock();
            try {
                lockingThreadInfo = liveThreadInfos.get(Thread.currentThread());
                //System.err.println("locking thread info " + lockingThreadInfo.toString());
                
                for (ThreadInfo liveThreadInfo : liveThreadInfos.values()) {
                    if (!liveThreadInfo.equals(lockingThreadInfo)) {
                        if (liveThreadInfo.getLockCount(lockObject) > 0) {
                            //other threads holding the lock now
                            //System.err.println("lock held by " + liveThreadInfo.toString());
                            lockAvailable = false;
                            blockedThreadInfos.add(lockingThreadInfo);
                            unblockThreadsBlockingForThreadBlock();
                            break;
                        }     
                    }
                }
                
                if (lockAvailable) {
                    //System.err.println("acquir lock");
                    lockingThreadInfo.acquiredLock(lockObject, lockCount);
                    //System.err.println("acquired lock");
                    break;
                }
            } finally {
                if (!lockAvailable) {
                    schedulerWakeupCondition.signal();
                }
                schedulerStateLock.unlock();
            }
            try {
//                System.err.println("acquiring");
//                System.out.println("Available permits:" + lockingThreadInfo.getPausingSemaphore().);
                lockingThreadInfo.getPausingSemaphore().acquire();
//                System.err.println("acquired");
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(2);
            }
        }
        afterEventNoIndexIncrease(lockEventDesc);
    }

    /**
     * Executed instead of a monitor exit (synchronized keyword/block).
     * 
     * @param lockObject
     *            the lock/monitor to be released
     */
    @SuppressWarnings("deprecation")
    public static void performUnlock(Object lockObject) {
        
//        long tid = Thread.currentThread().getId();      
//        String addr = System.identityHashCode(lockObject)+""; 
        //race detect
//        JUnit4MCRRunner.rd.lockAccess(tid, addr);
        //       
        if (exploring) {
            LockEventDesc unlockEventDesc = new LockEventDesc(EventType.UNLOCK, lockObject);
            beforeEvent(unlockEventDesc, true);
            schedulerStateLock.lock();
            try {
                ThreadInfo unlockingThreadInfo = liveThreadInfos.get(Thread.currentThread());
                if (unlockingThreadInfo.releasedLock(lockObject, 1) <= 0) {
                    Set<ThreadInfo> waitingForThisLockThreadInfos = new HashSet<ThreadInfo>();
                    for (ThreadInfo blockedThreadInfo : blockedThreadInfos) {
                        if (blockedThreadInfo.getEventDesc().getEventType().equals(EventType.LOCK)) {
                            LockEventDesc lockEventDesc = (LockEventDesc) blockedThreadInfo.getEventDesc();
                            if (lockEventDesc.getLockObject() == lockObject) {
                                waitingForThisLockThreadInfos.add(blockedThreadInfo);
                            }
                        }
                    }
                    blockedThreadInfos.removeAll(waitingForThisLockThreadInfos);
                    pausedThreadInfos.addAll(waitingForThisLockThreadInfos);
                }
            } finally {
                schedulerStateLock.unlock();
            }
            afterEvent(unlockEventDesc);
        } else {
            unsafe.monitorExit(lockObject);
        }
    }

    /**
     * Executed instead of {@link Object#wait()} invocations.
     * 
     * @param waitObject
     *            the {@link Object} on which {@link #wait()} is being
     *            performed.
     * @throws InterruptedException
     */
    public static void performWait(Object waitObject) throws InterruptedException {
        if (exploring) {
            WaitNotifyEventDesc waitEventDesc = new WaitNotifyEventDesc(EventType.WAIT, waitObject);
            beforeEvent(waitEventDesc, true);
            ThreadInfo waitingThreadInfo = null;
            int preWaitLockCount = -1;
            schedulerStateLock.lock();
            try {
                waitingThreadInfo = liveThreadInfos.get(Thread.currentThread());
                preWaitLockCount = waitingThreadInfo.getLockCount(waitObject);
                if (preWaitLockCount < 1) {
                    throw new IllegalMonitorStateException("Calling wait without holding object lock!");
                }
                waitingThreadInfo.releasedLock(waitObject, preWaitLockCount);
                blockedThreadInfos.add(waitingThreadInfo);
                unblockThreadsBlockingForThreadBlock();

                Set<ThreadInfo> waitObjectLockThreadInfos = new HashSet<ThreadInfo>();
                for (ThreadInfo blockedThreadInfo : blockedThreadInfos) {
                    if (blockedThreadInfo.getEventDesc().getEventType().equals(EventType.LOCK)) {
                        LockEventDesc lockEventDesc = (LockEventDesc) blockedThreadInfo.getEventDesc();
                        if (lockEventDesc.getLockObject() == waitObject) {
                            waitObjectLockThreadInfos.add(blockedThreadInfo);
                        }
                    }
                }
                blockedThreadInfos.removeAll(waitObjectLockThreadInfos);
                pausedThreadInfos.addAll(waitObjectLockThreadInfos);
            } finally {
                schedulerWakeupCondition.signal();
                schedulerStateLock.unlock();
            }
            waitingThreadInfo.getPausingSemaphore().acquire();
            performLock(waitObject, preWaitLockCount);
            afterEvent(waitEventDesc);
        } else {
            waitObject.wait();
        }
    }
    
    /**
     * 
     * Split wait into wait-lock
     * 
     * @param waitObject
     * @throws InterruptedException
     */
    public static int performOnlyWait(Object waitObject) throws InterruptedException {
        if (exploring) {
            WaitNotifyEventDesc waitEventDesc = new WaitNotifyEventDesc(EventType.WAIT, waitObject);
            
            beforeEvent(waitEventDesc, true);

            /*
             * when the wait even is chosen and logged, increase the index
             * so that the scheduler can move to the next choice
             */
            RVRunTime.currentIndex++;
            ThreadInfo waitingThreadInfo = null;
            int preWaitLockCount = -1;
            schedulerStateLock.lock();
            try {
                waitingThreadInfo = liveThreadInfos.get(Thread.currentThread());
                preWaitLockCount = waitingThreadInfo.getLockCount(waitObject);
                if (preWaitLockCount < 1) {
                    throw new IllegalMonitorStateException("Calling wait without holding object lock!");
                }
                
                waitingThreadInfo.releasedLock(waitObject, preWaitLockCount);
                
                blockedThreadInfos.add(waitingThreadInfo);
                unblockThreadsBlockingForThreadBlock();

                Set<ThreadInfo> waitObjectLockThreadInfos = new HashSet<ThreadInfo>();
                for (ThreadInfo blockedThreadInfo : blockedThreadInfos) {
                    if (blockedThreadInfo.getEventDesc().getEventType().equals(EventType.LOCK)) {
                        LockEventDesc lockEventDesc = (LockEventDesc) blockedThreadInfo.getEventDesc();
                        if (lockEventDesc.getLockObject() == waitObject) {
                            waitObjectLockThreadInfos.add(blockedThreadInfo);
                        }
                    }
                }
                blockedThreadInfos.removeAll(waitObjectLockThreadInfos);
                pausedThreadInfos.addAll(waitObjectLockThreadInfos);
            } finally {
                schedulerWakeupCondition.signal();
                schedulerStateLock.unlock();
            }
            waitingThreadInfo.getPausingSemaphore().acquire();
            
            
            
            afterEventNoIndexIncrease(waitEventDesc);
            return preWaitLockCount;
        } else {
            waitObject.wait();
            return 0;
        }
    }

    /**
     * Executed instead of timed {@link Object#wait()} invocations.
     * 
     * @param waitObject
     *            the {@link Object} on which {@link #wait()} is being
     *            performed.
     * @throws InterruptedException
     */
    public static void performTimedWait(Object waitObject, long millis, int nanos) throws InterruptedException {
        if (exploring) {
            WaitNotifyEventDesc waitEventDesc = new WaitNotifyEventDesc(EventType.TIMED_WAIT, waitObject);
            beforeEvent(waitEventDesc, false);
            afterEvent(waitEventDesc);
        } else {
            waitObject.wait(millis, nanos);
        }
    }

    /**
     * Executed instead of timed {@link Object#wait()} invocations.
     * 
     * @param waitObject
     *            the {@link Object} on which {@link #wait()} is being
     *            performed.
     * @throws InterruptedException
     */
    public static void performTimedWait(Object waitObject, long millis) throws InterruptedException {
        if (exploring) {
            WaitNotifyEventDesc waitEventDesc = new WaitNotifyEventDesc(EventType.TIMED_WAIT, waitObject);
            beforeEvent(waitEventDesc, false);
            afterEvent(waitEventDesc);
        } else {
            waitObject.wait(millis);
        }
    }

    /**
     * Executed instead of {@link Object#notify()} invocations.
     * 
     * @param notifyObject
     *            the {@link Object} on which {@link #notify()} is being
     *            performed.
     */
    public static long performNotify(Object notifyObject) {
        if (exploring) {
            return performNotify(notifyObject, false);
        } else {
            notifyObject.notify();
            return 0;
        }
    }
    
    public static void performNotifyOld(Object notifyObject) {
        if (exploring) {
             performNotify(notifyObject, false);
        } else {
            notifyObject.notify();
        }
    }

    /**
     * Executed instead of {@link Object#notifyAll()} invocations.
     *
     */
    public static long performNotifyAll(Object notifyAllObject) {
        if (exploring) {
            return performNotify(notifyAllObject, true);
        } else {
            notifyAllObject.notifyAll();
            return 0;
        }
    }

    /**
     * Executed instead of {@link Object#notify()} and
     * {@link Object#notifyAll()} invocations.
     * 
     * @param notifyObject
     *            the {@link Object} on which {@link #notify()} or
     *            {@link #notifyAll()} is being performed.
     * @param notifyAll
     *            <code>true</code> if {@link #notifyAll)}, <code>false</code>
     *            otherwise.
     */
    private static long performNotify(Object notifyObject, boolean notifyAll) {
        WaitNotifyEventDesc notifyEventDesc = new WaitNotifyEventDesc(notifyAll ? EventType.NOTIFY_ALL : EventType.NOTIFY, notifyObject);
        long notifiedThreadId = -1;
        beforeEvent(notifyEventDesc, true);
        schedulerStateLock.lock();
        try {
            ThreadInfo notifyingThreadInfo = liveThreadInfos.get(Thread.currentThread());
            if (notifyingThreadInfo.getLockCount(notifyObject) <= 0) {
                throw new IllegalMonitorStateException("Calling notify without holding object lock!");
            }
            SortedSet<ThreadInfo> notifyableThreadInfos = new TreeSet<ThreadInfo>();
            for (ThreadInfo blockedThreadInfo : blockedThreadInfos) {
                if (blockedThreadInfo.getEventDesc().getEventType().equals(EventType.WAIT)) {
                    WaitNotifyEventDesc waitEventDesc = (WaitNotifyEventDesc) blockedThreadInfo.getEventDesc();
                    if (waitEventDesc.getWaitObject() == notifyObject) {
                        notifyableThreadInfos.add(blockedThreadInfo);
                    }
                }
            }
            if (!notifyableThreadInfos.isEmpty()) {
                
                /*
                 * when it is a notifyAll event, any wait event can be waken up
                 * the randomness can't not be well modeled
                 * in order to simplify the problem, here I always take the first wait event that is to be waken up
                 */
                
                if (notifyAll) {
//                    blockedThreadInfos.removeAll(notifyableThreadInfos);
//                    for (ThreadInfo threadToNotify : notifyableThreadInfos) {
//                        threadToNotify.getPausingSemaphore().release();
//                    }
                    
                   //notifiedThreadId = 0;
                   //change it to: 
                    ThreadInfo threadToNotify = (ThreadInfo) choose(notifyableThreadInfos, ChoiceType.THREAD_TO_NOTIFY);
                    blockedThreadInfos.remove(threadToNotify);
                    threadToNotify.getPausingSemaphore().release();
                    notifiedThreadId = notifyableThreadInfos.first().getThread().getId();
                } else {
                    ThreadInfo threadToNotify = (ThreadInfo) choose(notifyableThreadInfos, ChoiceType.THREAD_TO_NOTIFY);
                    blockedThreadInfos.remove(threadToNotify);
                    threadToNotify.getPausingSemaphore().release();
                    notifiedThreadId = threadToNotify.getThread().getId();
                }
            }
        } finally {
            schedulerStateLock.unlock();
        }
        afterEvent(notifyEventDesc);
        return notifiedThreadId;
    }

    /**
     * Executed instead of {@link Thread#join()}.
     * 
     * @param joinThread
     *            the {@link Thread} on which  is being
     *            performed.
     * @throws InterruptedException
     */
    public static void performJoin(Thread joinThread) throws InterruptedException {
        
        if (exploring) {
            JoinEventDesc joinEventDesc = new JoinEventDesc(EventType.JOIN, joinThread);
            // For MCR, original false
            beforeEvent(joinEventDesc, true);
            boolean joined = false;
            schedulerStateLock.lock();
            ThreadInfo joiningThreadInfo = liveThreadInfos.get(Thread.currentThread());
            try {
                ThreadInfo joinThreadInfo = liveThreadInfos.get(joinThread);
                if (joinThreadInfo == null) {
                    joined = true;
                } else {
                    blockedThreadInfos.add(joiningThreadInfo);
                    unblockThreadsBlockingForThreadBlock();
                }
            } finally {
                if (!joined) {
                    schedulerWakeupCondition.signal();
                }
                schedulerStateLock.unlock();
            }
            afterEvent(joinEventDesc);
            if (!joined) {
                try {
                    joiningThreadInfo.getPausingSemaphore().acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.exit(2);
                }
            }
        } else {
            joinThread.join();
        }
    }

    /**
     * Executed instead of a timed {@link Thread#join()}.
     * 
     * @param joinThread
     *            the {@link Thread} on which  is being
     *            performed.
     * @throws InterruptedException
     */
    public static void performTimedJoin(Thread joinThread, long millis, int nanos) throws InterruptedException {
        if (exploring) {
            JoinEventDesc timedJoinEventDesc = new JoinEventDesc(EventType.TIMED_JOIN, joinThread);
            beforeEvent(timedJoinEventDesc, false);
            afterEvent(timedJoinEventDesc);
        } else {
            joinThread.join(millis, nanos);
        }
    }

    /**
     * Executed instead of a timed {@link Thread#join()}.
     * 
     * @param joinThread
     *            the {@link Thread} on which  is being
     *            performed.
     * @throws InterruptedException
     */
    public static void performTimedJoin(Thread joinThread, long millis) throws InterruptedException {
        if (exploring) {
            JoinEventDesc timedJoinEventDesc = new JoinEventDesc(EventType.TIMED_JOIN, joinThread);
            beforeEvent(timedJoinEventDesc, false);
            afterEvent(timedJoinEventDesc);
        } else {
            joinThread.join(millis);
        }
    }

    /**
     * Executed instead of
     * {@link Unsafe#park(boolean isAbsolute, long time)}. Needed to
     * implement support for j.u.c classes.
     * 
     * @param isAbsolute
     *            determines milli/nano-seconds, see Unsafe
     * @param time
     *            timeout value where 0 means indefinite, see Unsafe
     */
    public static void performPark(boolean isAbsolute, long time) {
        if (exploring) {
            Thread curThread = Thread.currentThread();
            ParkUnparkEventDesc parkUnparkEventDesc = new ParkUnparkEventDesc(EventType.PARK, curThread);
            if (!isAbsolute && time == 0L) {
                beforeEvent(parkUnparkEventDesc, true);
                ThreadInfo parkingThreadInfo = null;
                boolean blocked = false;
                schedulerStateLock.lock();
                try {
                    parkingThreadInfo = liveThreadInfos.get(curThread);
                    if (parkingThreadInfo.isParkPermitAvailable()) {
                        parkingThreadInfo.setParkPermitAvailable(false);
                    } else {
                        blockedThreadInfos.add(parkingThreadInfo);
                        unblockThreadsBlockingForThreadBlock();
                        blocked = true;
                    }
                } finally {
                    if (blocked) {
                        schedulerWakeupCondition.signal();
                    }
                    schedulerStateLock.unlock();
                }
                if (blocked) {
                    try {
                        parkingThreadInfo.getPausingSemaphore().acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        System.exit(2);
                    }
                }
            } else {
                beforeEvent(parkUnparkEventDesc, false);
            }
            afterEvent(parkUnparkEventDesc);
        } else {
            unsafe.park(isAbsolute, time);
        }
    }

    /**
     * Executed instead of {@link Unsafe#unpark(Object)}. Needed to
     * implement support for j.u.c classes.
     * 
     * @param threadObject
     *            which thread to unblock, see Unsafe
     */
    public static void performUnpark(Object threadObject) {
        if (exploring) {
            ParkUnparkEventDesc parkUnparkEventDesc = new ParkUnparkEventDesc(EventType.UNPARK, (Thread) threadObject);
            beforeEvent(parkUnparkEventDesc, true);
            schedulerStateLock.lock();
            try {
                ThreadInfo unparkableThreadInfo = null;
                for (ThreadInfo blockedThreadInfo : blockedThreadInfos) {
                    if (blockedThreadInfo.getEventDesc().getEventType().equals(EventType.PARK)) {
                        ParkUnparkEventDesc parkEventDesc = (ParkUnparkEventDesc) blockedThreadInfo.getEventDesc();
                        if (parkEventDesc.getThread() == threadObject) {
                            unparkableThreadInfo = blockedThreadInfo;
                            break;
                        }
                    }
                }
                if (null != unparkableThreadInfo) {
                    blockedThreadInfos.remove(unparkableThreadInfo);
                    unparkableThreadInfo.getPausingSemaphore().release();
                } else { // need to remember for future parks
                    unparkableThreadInfo = liveThreadInfos.get((Thread) threadObject);
                    // only remember unpark if given thread has been started
                    // semantics do not guarantee anything if thread isn't
                    // started
                    if (null != unparkableThreadInfo) {
                        unparkableThreadInfo.setParkPermitAvailable(true);
                    }
                }
            } finally {
                schedulerStateLock.unlock();
            }
            afterEvent(parkUnparkEventDesc);
        } else {
            unsafe.unpark(threadObject);
        }
    }

    /**
     * Updates the {@link LocationDesc} of the current {@link Thread}'s
     * {@link ThreadInfo}. Executed before any of the events listed in
     * {@link EventType}.
     * 
     * @param className
     * @param methodName
     * @param lineNumber
     */
    public static void updateThreadLocation(String className, String methodName, int lineNumber) {
        if (exploring) {
            schedulerStateLock.lock();
            try {
                ThreadInfo currentThreadInfo = liveThreadInfos.get(Thread.currentThread());
                currentThreadInfo.setLocationDesc(new LocationDesc(className, methodName, lineNumber));
            } catch (NullPointerException e) {
                System.out.println("null");
            }
            finally {
                schedulerStateLock.unlock();
            }
        }
    }

    /******************************************************************
     ****************************************************************** 
     ************************ HELPER METHODS **************************
     ****************************************************************** 
     ******************************************************************/

    /**
     * Helper method called before a schedule relevant event.
     * 
     * @param eventDesc
     *            {@link EventDesc} describing the schedule relevant event.
     * @param pause
     *            whether to pause the current thread before the event.
     */
    private static void beforeEvent(EventDesc eventDesc, boolean pause) {
        ThreadInfo currentThreadInfo;
        schedulerStateLock.lock();       
        try {
            Listeners.fireBeforeEvent(eventDesc);
            currentThreadInfo = liveThreadInfos.get(Thread.currentThread());
            if(currentThreadInfo!=null)
            {
                currentThreadInfo.setEventDesc(eventDesc);
                if (pause) {
                    pausedThreadInfos.add(currentThreadInfo);
                }
            }
        } finally {
            if (pause) {
                schedulerWakeupCondition.signal();
            }
            schedulerStateLock.unlock();
        }
        
        try {
            if (pause) {
                if(currentThreadInfo!=null){
//                    System.err.println("---sema accquired by: "+currentThreadInfo.toString());
                    currentThreadInfo.getPausingSemaphore().acquire();
//                    System.err.println("***sema accquired by: "+currentThreadInfo.toString());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    
    public static void performSleep() {
        ThreadInfo currentThreadInfo;
        schedulerStateLock.lock();
        try {
            currentThreadInfo = liveThreadInfos.get(Thread.currentThread());
            if(currentThreadInfo!=null)
            {
                    pausedThreadInfos.add(currentThreadInfo);
            }
        } finally {
                schedulerWakeupCondition.signal();
            }
            schedulerStateLock.unlock();
        
        try {
                if(currentThreadInfo!=null)
                currentThreadInfo.getPausingSemaphore().acquire();
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**
     * Helper method called after a schedule relevant event has been performed
     * 
     * @param eventDesc
     *            {@link EventDesc} describing the even that was performed.
     */
    private static void afterEvent(EventDesc eventDesc) {
    	schedulerStateLock.lock();
        Listeners.fireAfterEvent(eventDesc);

        schedulerStateLock.unlock();
    }
    
    private static void afterEventNoIndexIncrease(EventDesc eventDesc) {
    	schedulerStateLock.lock();
        Listeners.fireAfterEvent(eventDesc);
        schedulerStateLock.unlock();
    }

    /**
     * Method used to replace fireEvent in IMUnit, to put constraints for exploration
     * 
     * @param eventName
     *            IMUnit event name
     */
    public static void fireIMUnitEvent(String eventName) {
        // If event does not need to wait for other events:
        // 2) Add to happened events
        // 1) Find any events waiting for this event and enable them
        // If event needs to wait for other events:
        // 1) Check if other events have happened, if so just add this to
        // happened events
        // 2) If other events have not happened, add this thread to blocked
        // threads
        if (!currentOrderings.isEmpty()) {
            /* Collect the event(s) this event needs to wait for */
            Set<Event> beforeEvents = new HashSet<Event>();
            if (currentOrderings.containsKey(eventName)) {
                beforeEvents.addAll(currentOrderings.get(eventName));
            }
            Thread currentThread = Thread.currentThread();
            String qualifiedName = eventName + AT + currentThread.getName();
            if (currentOrderings.containsKey(qualifiedName)) {
                beforeEvents.addAll(currentOrderings.get(qualifiedName));
            }

            /* Wait for collected events to be completed */
            if (!beforeEvents.isEmpty()) {
                for (Event beforeEvent : beforeEvents) {
                    if (beforeEvent instanceof SimpleEvent) {
                        SimpleEvent simpleEvent = (SimpleEvent) beforeEvent;
                        String simpleEventDesc = getEventDesc(simpleEvent);
                        blockForIMUnitEvent(simpleEventDesc);
                    } else if (beforeEvent instanceof BlockEvent) {
                        BlockEvent blockEvent = (BlockEvent) beforeEvent;
                        String blockEventDesc = getEventDesc(blockEvent);
                        blockForIMUnitEvent(blockEventDesc);
                        blockForThreadBlock(currentThread, blockEventDesc);
                    }
                }
            }
            schedulerStateLock.lock();
            try {
                /* This event has now happened */
                currentHappenedEvents.put(eventName, currentThread);
                currentHappenedEvents.put(qualifiedName, currentThread);
                Set<ThreadInfo> unblock = new HashSet<ThreadInfo>();
                for (ThreadInfo blockedThreadInfo : blockedThreadInfos) {
                    EventDesc eventDesc = blockedThreadInfo.getEventDesc();
                    if (eventDesc.getEventType().equals(EventType.BLOCKED_FOR_IMUNIT_EVENT)) {
                        String blockingForEvent = ((BlockedForIMUnitEventDesc) eventDesc).getEvent();
                        if (blockingForEvent.equals(eventName) || blockingForEvent.equals(qualifiedName)) {
                            unblock.add(blockedThreadInfo);
                        }
                    }
                }
                blockedThreadInfos.removeAll(unblock);
                for (ThreadInfo unblockThreadInfo : unblock) {
                    unblockThreadInfo.getPausingSemaphore().release();
                }
            } finally {
                schedulerStateLock.unlock();
            }
        }
    }


    private static void unblockThreadsBlockingForThreadBlock() {
        Set<ThreadInfo> unblock = new HashSet<ThreadInfo>();
        for (ThreadInfo blockedThreadInfo : blockedThreadInfos) {
            EventDesc eventDesc = blockedThreadInfo.getEventDesc();
            if (eventDesc.getEventType().equals(EventType.BLOCKED_FOR_THREAD_BLOCK)) {
                Thread blockingForThread = ((BlockedForThreadBlockDesc) eventDesc).getThread();
                if (blockingForThread.equals(Thread.currentThread())) {
                    unblock.add(blockedThreadInfo);
                }
            }
        }
        blockedThreadInfos.removeAll(unblock);
        for (ThreadInfo unblockThreadInfo : unblock) {
            unblockThreadInfo.getPausingSemaphore().release();
        }
    }

    private static void blockForThreadBlock(Thread currentThread, String blockEventDesc) {
        boolean willBlock = false;
        ThreadInfo currentThreadInfo = null;
        schedulerStateLock.lock();
        try {
            currentThreadInfo = liveThreadInfos.get(currentThread);
            Thread blockEventThread = currentHappenedEvents.get(blockEventDesc);
            ThreadInfo blockEventThreadInfo = liveThreadInfos.get(blockEventThread);
            if (!blockedThreadInfos.contains(blockEventThreadInfo)) {
                currentThreadInfo.setEventDesc(new BlockedForThreadBlockDesc(blockEventThread));
                blockedThreadInfos.add(currentThreadInfo);
                unblockThreadsBlockingForThreadBlock();
                willBlock = true;
            }
        } finally {
            if (willBlock) {
                schedulerWakeupCondition.signal();
            }
            schedulerStateLock.unlock();
        }
        try {
            if (willBlock) {
                currentThreadInfo.getPausingSemaphore().acquire();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void blockForIMUnitEvent(String blockEventDesc) {
        boolean willBlock = false;
        ThreadInfo currentThreadInfo = null;
        schedulerStateLock.lock();
        try {
            currentThreadInfo = liveThreadInfos.get(Thread.currentThread());
            if (!currentHappenedEvents.containsKey(blockEventDesc)) {
                currentThreadInfo.setEventDesc(new BlockedForIMUnitEventDesc(blockEventDesc));
                blockedThreadInfos.add(currentThreadInfo);
                unblockThreadsBlockingForThreadBlock();
                willBlock = true;
            }
        } finally {
            if (willBlock) {
                schedulerWakeupCondition.signal();
            }
            schedulerStateLock.unlock();
        }
        try {
            if (willBlock) {
                currentThreadInfo.getPausingSemaphore().acquire();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    public static void setIMUnitSchedule(String name, Orderings orderings) {
        currentOrderings.clear();
        for (Ordering partialOrder : orderings.getOrderings()) {
            SimpleEvent afterEvent = partialOrder.getAfterEvent();
            String afterEventDesc = getEventDesc(afterEvent);
            Set<Event> beforeEvents = currentOrderings.get(afterEventDesc);
            if (beforeEvents == null) {
                beforeEvents = new HashSet<Event>();
                currentOrderings.put(afterEventDesc, beforeEvents);
            }
            beforeEvents.add(partialOrder.getBeforeEvent());
        }
        currentHappenedEvents.clear();
    }

    public static void clearIMUnitSchedule() {
        currentOrderings.clear();
        currentHappenedEvents.clear();
    }

    /**
     * Helper method for constructing the description of a {@link SimpleEvent}.
     * 
     * @param simpleEvent
     * @return description of simpleEvent
     */
    private static String getEventDesc(SimpleEvent simpleEvent) {
        String eventDesc = simpleEvent.getEventName().getName();
        Name eventThreadName = simpleEvent.getThreadName();
        if (eventThreadName.getName() != null) {
            eventDesc += AT + eventThreadName.getName();
        }
        return eventDesc;
    }

    /**
     * Helper method for constructing the description of a {@link BlockEvent}.
     * 
     * @param blockEvent
     * @return description of blockEvent
     */
    private static String getEventDesc(BlockEvent blockEvent) {
        String eventDesc = blockEvent.getBlockAfterEventName().getName();
        Name eventThreadName = blockEvent.getThreadName();
        if (eventThreadName.getName() != null) {
            eventDesc += AT + eventThreadName.getName();
        }
        return eventDesc;
    }

}
