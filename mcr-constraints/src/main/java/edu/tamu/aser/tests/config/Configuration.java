package edu.tamu.aser.config;

import com.beust.jcommander.*;
import edu.tamu.aser.graph.ReachabilityEngine;
import edu.tamu.aser.sdg.ReadSDG;

import java.util.*;

public class Configuration {

//    public static final String PROGRAM_NAME = "rv-predict";
    public static final String YES = "yes";
    public static final String NO = "no";
    @Parameter(description="<java_command_line>")


	public static Map<Integer, Set<Integer>> SDG = null;
	public static ReachabilityEngine reachSDG = null;
	public static Map<String, Integer> mapNodeLabelToId = null;

    public static boolean DEBUG = false;

    //oopsla -- extend mcr to TSO/PSO
    public static String mode = "SC";  //default: SC

    //ecoop -- using static dependency analysis to optimize mcr
	public static boolean Optimize = false;     //use or not use optimization by SDG
	public static boolean plus = false;  //mcr+ or mcr+s  true: mcs+s

	//toplas  -- optimal mcr
    public static boolean OMCR = false;

    //for naming output file
    public static String class_name = null;
    public static String class_path = null;

    static {

        ConstraintsProperties instance = ConstraintsProperties.getInstance();
        //configure memory model
        mode = instance.getProperty("mm");

        //configure the static dependency analysis
        Optimize = Boolean.parseBoolean(instance.getProperty("dep_mcr"));
        plus = Boolean.parseBoolean(instance.getProperty("plus"));

        //configure optimal mcr
        OMCR = Boolean.parseBoolean(System.getProperty("opt_mcr"));

        //vm option by -D
        class_name = System.getProperty("class_name");

        //debug mode?
        DEBUG = Boolean.parseBoolean(instance.getProperty("debug"));

        //this is for the constraints reduction using static dependency analysis
        if (Optimize){
            SDG = ReadSDG.readSDG();
            reachSDG = ReadSDG.ConstructReachability(SDG);
            mapNodeLabelToId = ReadSDG.NodeToId();
        }
    }
	
    public static long numReads;
    public static long rwConstraints;
    public static long solveTime;
    
	//end

    public final static String opt_only_log = "--log";
    @Parameter(names = opt_only_log, description = "Record execution in given directory (no prediction)", descriptionKey = "1000")
    public String log_dir = null;
    public boolean log = true;

    final static String opt_log_output = "--output";
    @Parameter(names = opt_log_output, description = "Output of the logged execution [yes|no|<file>]", hidden = true, descriptionKey = "1010")
    public String log_output = YES;

 	final static String opt_optlog = "--with-profile";
    @Parameter(names = opt_optlog, description = "Use profiling to optimize logging size", hidden = true, descriptionKey = "1020")
    public boolean optlog;

//    public final static String opt_include = "--include";
//    @Parameter(names = opt_include, validateWith = PackageValidator.class,
//            description = "Comma separated list of packages to include", hidden = true, descriptionKey = "1025")
//    public static String additionalIncludes;
//
//    public final static String opt_exclude = "--exclude";
//    @Parameter(names = opt_exclude, validateWith = PackageValidator.class,
//            description = "Comma separated list of packages to exclude", hidden = true, descriptionKey = "1030")
//    public static String additionalExcludes;

    public final static String opt_sharing_only = "--detectSharingOnly";
    @Parameter(names = opt_sharing_only, description = "Run agent only to detect shared variables", hidden = true, descriptionKey = "1040")
    public boolean agentOnlySharing;

    public final static String opt_only_predict = "--predict";
    @Parameter(names = opt_only_predict, description = "Run prediction on logs from given directory", descriptionKey = "2000")
    public String predict_dir = null;
    public boolean predict = true;

//	final static String opt_rmm_pso = "--pso";//for testing only
//    @Parameter(names = opt_rmm_pso, description = "PSO memory model", hidden = true)
    public boolean rmm_pso;

	final static String opt_max_len = "--maxlen";
    final static String default_max_len= "1000";
    @Parameter(names=opt_max_len, description = "Window size", hidden = true, descriptionKey = "2010")
    public long window_size = 1000;

//	final static String opt_no_schedule = "--noschedule";
//    @Parameter(names=opt_no_schedule, description = "not report schedule", hidden = true)
    //ok, let's make noschedule by default
    public boolean noschedule = true;

	final static String opt_no_branch = "--nobranch";
    @Parameter(names=opt_no_branch, description = "Use no branch model", hidden = true, descriptionKey = "2020")
    public boolean nobranch;

	final static String opt_no_volatile = "--novolatile";
    @Parameter(names=opt_no_volatile, description = "Exclude volatile variables", hidden = true, descriptionKey = "2030")
    public boolean novolatile;

	final static String opt_allrace = "--allrace";
    @Parameter(names=opt_allrace, description = "Check all races", hidden = true, descriptionKey = "2040")
    public boolean allrace;

//	final static String opt_all_consistent = "--allconsistent";
//    @Parameter(names = opt_all_consistent, description = "require all read-write consistent", hidden = true)
    public boolean allconsistent;

//	final static String opt_constraint_outdir = "--outdir";
//    @Parameter(names = opt_constraint_outdir, description = "constraint file directory", hidden = true)
    public String constraint_outdir;

    public final static String opt_table_name = "--table";
//    @Parameter(names = opt_table_name, description = "Name of the table storing the log", hidden = true)
    public String tableName = null;

    final static String opt_smt_solver = "--solver";
    @Parameter(names = opt_smt_solver, description = "Solver command to use (SMT-LIB v1.2)", hidden = true, descriptionKey = "2050")
    
    //using the absolute path
    public String smt_solver = "-smt2 -T:600 -st";//"\"" + OS.current().getNativeExecutable("z3") + "\"" + " -smt";

	final static String opt_solver_timeout = "--solver_timeout";
    @Parameter(names = opt_solver_timeout, description = "Solver timeout in seconds", hidden = true, descriptionKey = "2060")
    public long solver_timeout = 600;

	final static String opt_solver_memory = "--solver_memory";
//    @Parameter(names = opt_solver_memory, description = "solver memory size in MB", hidden = true)
    public long solver_memory = 8000;

	final static String opt_timeout = "--timeout";
    @Parameter(names = opt_timeout, description = "Rv-predict timeout in seconds", hidden = true, descriptionKey = "2070")
    public long timeout = 3600;

//    final static String opt_smtlib1 = "--smtlib1";
//    @Parameter(names = opt_smtlib1, description = "use constraint format SMT-LIB v1.2", hidden = true)
    public boolean smtlib1 = true;

	final static String opt_optrace = "--optrace";
//    @Parameter(names = opt_optrace, description = "optimize race detection", hidden = true)
    //by default optrace is true
    public boolean optrace = true;


	public final static String opt_outdir = "--dir";
    @Parameter(names = opt_outdir, description = "Output directory", hidden = true, descriptionKey = "8000")
    public String outdir = "tmp";

	final static String short_opt_verbose = "-v";
    final static String opt_verbose = "--verbose";
    @Parameter(names = {short_opt_verbose, opt_verbose}, description = "Generate more verbose output", descriptionKey = "9000")
    public boolean verbose;
}
