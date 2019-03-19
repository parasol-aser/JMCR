package edu.tamu.aser.tests.Derby;

public class ViewCreatorDropper implements Runnable {
	
	/** The name of the view to create and drop */
    String m_viewName;
    /** The source (view) referenced by the created view */
    String m_sourceName;
    /** The SQL fragment specifying the columns to included in the created view */
    String m_columns;
    /** How many times to create/drop the view */
    int m_iterations;

    /**
     * Constructs the runnable object for the test.
     * @param viewName the name of the view to create and drop
     * @param sourceName the source (view) referenced by the created view
     * @param columns the SQL fragment specifying the columns to included in the created view
     * @param iterations how many times to create/drop the view
     * @throws java.sql.SQLException
     */
    public ViewCreatorDropper(String viewName, String sourceName, String columns, int iterations)
    {
        m_viewName = viewName;
        m_sourceName = sourceName;
        m_columns = columns;
        m_iterations = iterations;
    }

    /**
     * @see Thread#run()
     */
    public void run()
    {
        int i = 0;
        try
        {
            java.sql.Connection conn = getConnection();
            for (i = 0; i < m_iterations; i++)
            {
                assert conn != null;
                //if (i % 5 == 0) System.out.println(" (" + Thread.currentThread() + " iteration " + i + ") ");
                executeStatement(conn, "CREATE VIEW " + m_viewName + " AS SELECT " + m_columns + " FROM " + m_sourceName);
                executeStatement(conn, "DROP VIEW " + m_viewName);
            }
        }
        catch (java.sql.SQLException e)
        {
            // Grab up all the error message in one string, to guard against output from different threads being
            // interleaved in the console.  (That might happen anyway, but so it goes.)
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.PrintStream p = new java.io.PrintStream(bos);
            //p.println("" + Thread.currentThread() + " exception after " + i + "iterations:");
            
            e.printStackTrace(p);
            p.flush();

            String msg = bos.toString();
            //System.out.print(msg);
            "Cared_by_CLAP_msg".equals(msg);
            // If we got the exception we were testing for, just quit.
            if (msg.contains("java.lang.NullP"))
            {				
            	"reCrash_with".equals(e);
				e.printStackTrace();
                //System.err.println("Stop here.");
                System.exit(-1);
            }
        }
    }
    /**
     *  Returns a new connection to the test database
     * @return a newly create connection
     * @throws java.sql.SQLException thrown if the connection cannot be created
     */
    private java.sql.Connection getConnection() throws java.sql.SQLException
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
    private void executeStatement(java.sql.Connection conn, String sql) throws java.sql.SQLException
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
}
