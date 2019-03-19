package edu.tamu.aser.instrumentation;

public class RVConfig {
    public static final RVConfig instance = new RVConfig();
    public static final String propFile = "rv.conf";

//    public String mode = "SC";   //default: sequential consistency memory model
    public final String BUFFER_STORE="bufferStore";
    public final String BUFFER_STORE_PSO="bufferStorePSO";
    
//    public final String UPDATE_JUDGE="updateJudge";
    public final String UPDATE_STORE="updateStore";
    public final String UPDATE_STORE_PSO="updateStorePSO";
    public final String UPDATE_BEFORE_LOAD="updateBeforeLoad";
    public final String MEM_BARRIER="bufferEmpty";
    //public final String 
    
    public final String DESC_BUFFER_STORE="(Ljava/lang/Object;Ljava/lang/String;)V";//return a boolean
    public final String DESC_BUFFER_STORE_PSO="(Ljava/lang/Object;Ljava/lang/String;)V";
    //    public final String DESC_UPDATE_JUDGE="()Z";
    //public final String DESC_UPDATE_STORE="()Ljava/lang/String;";
    public final String DESC_UPDATE_STORE="()V";
    public final String DESC_UPDATE_STORE_PSO="()V";
    public final String DESC_UPDATE_BEFORE_LOAD="()V";
    public final String DESC_MEM_BARRIER="()V";
    //
    public final String LOG_FIELD_ACCESS = "logFieldAcc";
    public final String LOG_INIT_WRITE_ACCESS = "logInitialWrite";
    public final String LOG_ARRAY_ACCESS = "logArrayAcc";
    public final String LOG_LOCK_INSTANCE = "logLock";
    public final String LOG_LOCK_STATIC = "logStaticSyncLock";
    public final String LOG_UNLOCK_INSTANCE = "logUnlock";
    public final String LOG_UNLOCK_STATIC = "logStaticSyncUnlock";
    public final String LOG_BRANCH = "logBranch";
//    public final String LOG_THREAD_START = "logStart";
    public final String LOG_THREAD_JOIN = "logJoin";
    public final String LOG_THREAD_SLEEP = "logSleep";
    public final String LOG_WAIT = "logWait";
    public final String LOG_NOTIFY = "logNotify";
    public final String LOG_NOTIFY_ALL = "logNotifyAll";
    
    public final String LOG_THREAD_BEFORE_START = "logBeforeStart";
    public final String LOG_THREAD_AFTER_START = "logAfterStart";
    public final String LOG_THREAD_BEGIN = "logThreadBegin";
    public final String LOG_THREAD_END = "logThreadEnd";
    
    public final String DESC_LOG_FIELD_ACCESS = "(ILjava/lang/Object;ILjava/lang/Object;Z)V";
    public final String DESC_LOG_ARRAY_ACCESS_DETECT_SHARING ="(ILjava/lang/Object;IZ)V";
    public final String DESC_LOG_FIELD_ACCESS_DETECT_SHARING = "(IIZ)V";

    public final String DESC_LOG_INIT_WRITE_ACCESS = "(ILjava/lang/Object;ILjava/lang/Object;)V";
    public final String DESC_LOG_ARRAY_ACCESS = "(ILjava/lang/Object;ILjava/lang/Object;Z)V";
    public final String DESC_LOG_LOCK_INSTANCE = "(ILjava/lang/Object;)V";
    public final String DESC_LOG_LOCK_STATIC = "(II)V";
    public final String DESC_LOG_UNLOCK_INSTANCE = "(ILjava/lang/Object;)V";
    public final String DESC_LOG_UNLOCK_STATIC = "(II)V";
    public final String DESC_LOG_BRANCH = "(I)V";
    public final String DESC_LOG_THREAD_START = "(ILjava/lang/Object;)V";
    public final String DESC_LOG_THREAD_JOIN = "(ILjava/lang/Object;)V";
    public final String DESC_LOG_THREAD_SLEEP = "()V";
    public final String DESC_LOG_WAIT = "(ILjava/lang/Object;)V";
    public final String DESC_LOG_NOTIFY = "(ILjava/lang/Object;)V";

    public final String DESC_LOG_THREAD_BEGIN = "()V";
    public final String DESC_LOG_THREAD_END = "()V";
    
    public boolean verbose = true;

    public String[] excludeList;
    public String[] includeList;

    public RVConfig() {
        try {
//            Properties properties = new Properties();
//            properties.load(new FileInputStream(propFile));
//            verbose = properties.getProperty("rv.verbose", "false").equals("true");
//            excludeList = properties.getProperty("rv.excludeList", "").split(",");
//            // includeList =
//            // properties.getProperty("rv.includeList","").split(",");
//            logClass = properties.getProperty("rv.logClass", "rvpredict.logging.RecordRT").replace('.', '/');

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
