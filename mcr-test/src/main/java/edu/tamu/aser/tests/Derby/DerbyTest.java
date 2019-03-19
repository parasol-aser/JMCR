package edu.tamu.aser.tests.Derby;

import static org.junit.Assert.fail;

import derby2861.ViewCreatorDropper;
import edu.tamu.aser.tests.reex.JUnit4MCRRunner;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.junit.Test;
import org.junit.runner.RunWith;


//10.3.2.1
/**
 * This class tests the thread-safeness of the Derby database system, using the embedded driver.  With the
 * proper choice of main arguments, which are probably different for different machines, it will sometimes show an
 * SQLException with the following output from printStackTrace().  It may need to be run more than once, as having
 * leftover views existing from prior runs sometimes seems to have an effect.
 *  
 * <pre>
 * java.lang.NullPointerException
 *     at org.apache.derby.iapi.sql.dictionary.TableDescriptor.getObjectName(TableDescriptor.java:758)
 *    at org.apache.derby.impl.sql.depend.BasicDependencyManager.getPersistentProviderInfos(BasicDependencyManager.java:677)
 *     at org.apache.derby.impl.sql.compile.CreateViewNode.bindViewDefinition(CreateViewNode.java:287)
 *     at org.apache.derby.impl.sql.compile.CreateViewNode.bind(CreateViewNode.java:183)
 *     at org.apache.derby.impl.sql.GenericStatement.prepMinion(GenericStatement.java:345)
 *     at org.apache.derby.impl.sql.GenericStatement.prepare(GenericStatement.java:119)
 *     at org.apache.derby.impl.sql.conn.GenericLanguageConnectionContext.prepareInternalStatement(GenericLanguageConnectionContext.java:745)
 *     at org.apache.derby.impl.jdbc.EmbedStatement.execute(EmbedStatement.java:568)
 *     at org.apache.derby.impl.jdbc.EmbedStatement.execute(EmbedStatement.java:517)
 *     at TestEmbeddedMultiThreading.executeStatement(TestEmbeddedMultiThreading.java:109)
 *     at TestEmbeddedMultiThreading.access$100(TestEmbeddedMultiThreading.java:10)
 *     at TestEmbeddedMultiThreading$ViewCreatorDropper.run(TestEmbeddedMultiThreading.java:173)
 *     at java.lang.Thread.run(Thread.java:534)
 * Stop here.
 * </pre>
 */
@RunWith(JUnit4MCRRunner.class)
public class DerbyTest
{
	
    /**
     * Invoke the test, providing a number of threads and a number of iterations.
     * @param s arguments to the function, must be two integers: number of thread and number of iterations
     */
    static public void main(String[] args)
    {
		long st,et;
		st=System.currentTimeMillis();
		
		String[] s = new String[2];
    	s[0]="2";
    	s[1]="2";
    	if(args.length==2)
    	{
    		s[0]=args[0];
    		s[1]=args[1];
    	}
    	try
        {
            if (s.length < 2)
            {
                System.out.println("Usage: main NUMBER_OF_THREADS NUMBER_OF_ITERATIONS");
                System.exit(-1);
            }
            setup();

            doit(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
		
		et=System.currentTimeMillis();
		System.out.println(et-st);	
    }

    /**
     * Creates the test object, registering the Derby embedded driver.  If the test database does not already exists,
     * creates that Derby database.
     * @throws ClassNotFoundException thrown if the driver class could not be found, probably due to classpath problems
     */
    private static void setup()
    {
        //Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

        EmbeddedDriver driver = new EmbeddedDriver();
        
        try
        {
            java.sql.DriverManager.getConnection("jdbc:derby:DERBY2861;create=true");
        }
        catch (java.sql.SQLException e)
        {
            System.out.println("Exception creating database...assuming already exists");
        }
    }

    /**
     * Runs the thread-safeness test.
     * @param numThreads the number of threads to spawn for the test
     * @param numIterations the number of iterations to run each thread
     * @throws java.sql.SQLException thrown if there is an SQL error setting up the test
     */
    private static void doit(int numThreads, int numIterations) throws java.sql.SQLException
    {
        try
        {
            executeStatement(getConnection(), "CREATE TABLE schemamain.SOURCETABLE (col1 int, col2 char(10), col3 varchar(20), col4 decimal(10,5))");
        }
        catch (java.sql.SQLException e)
        {
            // Just report this...probably the table or view already exists
            //System.out.println(e.getMessage());
        }
        try
        {
        	executeStatement(getConnection(), "CREATE VIEW viewSource AS SELECT col1, col2 FROM schemamain.SOURCETABLE");
        }catch(Exception e){}
//        
        
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++)
        {
            threads[i] = new Thread(new ViewCreatorDropper("schema1.VIEW" + i, "viewSource", "*", numIterations));
            threads[i].setName("thread-"+i);
        }
        for (int i = 0; i < numThreads; i++)
        {
            threads[i].start();
        }
		try {
	        for (int i = 0; i < numThreads; i++)
	        {
	            threads[i].join();
	        }
		} catch (InterruptedException e) {
			"reCrash_with".equals(e);
			e.printStackTrace();
		}

    }

    /**
     *  Returns a new connection to the test database
     * @return a newly create connection
     * @throws java.sql.SQLException thrown if the connection cannot be created
     */
    private static java.sql.Connection getConnection() throws java.sql.SQLException
    {
        return java.sql.DriverManager.getConnection("jdbc:derby:DERBY2861");
    }

    /**
     * Creates and executes a new SQL statement on the connection, ensuring that the statement is closed, regardless
     * of whether the statement execution throws an exception
     * @param conn the connection against which to run the statement
     * @param sql the SQL to execute
     * @throws java.sql.SQLException thrown if there is any SQL error executing the statement (or creating it)
     */
    private static void executeStatement(java.sql.Connection conn, String sql) throws java.sql.SQLException
    {
        //System.out.println("" + Thread.currentThread() + " executing " + sql);
        java.sql.Statement stmt = null;
        try
        {
            stmt = conn.createStatement();
            stmt.execute(sql);
        }
        finally
        {
            if (stmt != null)
            {
                try
                {
                    stmt.close();
                }
                catch (java.sql.SQLException e)
                {
                    System.out.println("Eating close() exception: " + e.getMessage());
                }
            }
        }
    }
    
	@Test
	public void test() throws InterruptedException {
		DerbyTest.main(new String[]{});
	}

}
