package edu.tamu.aser.util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import edu.tamu.aser.config.Configuration;

/**
 * Created by TraianSF on 05.08.2014.
 */
public class Logger {

    private PrintWriter out;
    Configuration config;
    /**
	 * Initialize the file printer. All race detection statistics are stored
	 * into the file race-report.txt
	 *
	 * @param config
	 */
	public Logger(Configuration config)
	{
        this.config = config;
		try{
		String fname = "race-report.txt";

        File file = new File(config.outdir);
        file.mkdirs();
		out = new PrintWriter(new FileWriter(config.outdir + "/" + fname,true));

		String type = "";
		if(config.rmm_pso)
			type+="pso: ";

		if(config.nobranch)
			type += "maximal: ";
		else if(config.allconsistent)
			type += "Said et al.: ";
		else if(config.smtlib1)
			type += "maximal-branch (yices): ";
		else
			type += "maximal-branch (z3): ";
//		out.println("\n------------------ "+type+config.tableName+" -------------------\n");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void closePrinter()
	{
		if(out!=null)
			out.close();
	}

    public PrintWriter getPrinter() {
        return out;
    }

    public void report(String msg, MSGTYPE type)
	{
		switch(type)
		{
		case REAL:
        case ERROR:
			System.err.println(msg);
			out.println(msg);
			break;
        case INFO:
			System.out.println(msg);
            if (config.verbose) {
                out.println(msg);
            }
			break;
 		case STATISTICS:
            if (config.verbose) {
                System.out.println(msg);
            }
			out.println(msg);
			break;
        case VERBOSE:
            if (config.verbose) {
                System.out.println(msg);
                out.println(msg);
            }
            break;
		case POTENTIAL:
			break;
		default: break;
		}
	}

    public enum MSGTYPE
    {
        REAL,POTENTIAL,STATISTICS, INFO, VERBOSE, ERROR
    }
}
