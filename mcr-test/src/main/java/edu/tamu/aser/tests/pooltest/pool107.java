/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.tamu.aser.tests.pooltest;

import static org.junit.Assert.fail;

import edu.tamu.aser.reex.JUnit4MCRRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import pool107.org.apache.commons.pool.impl.GenericObjectPool;

/**
 * @author Rodney Waldhoff
 * @author Dirk Verbeeck
 * @author Sandy McArthur
 * @version $Revision: 609415 $ $Date: 2008-01-06 16:45:32 -0500 (Sun, 06 Jan 2008) $
 */

@RunWith(JUnit4MCRRunner.class)
public class pool107 {

    /*    public static Test suite() {
        return new TestSuite(TestGenericObjectPool.class);
	}*/
	
    public static void main(String[] args){
    	pool = new GenericObjectPool(new SimpleFactory());
    	
    	
        SimpleFactory factory = new SimpleFactory();
        factory.setMakeLatency(300);
        factory.setMaxActive(2);
        final GenericObjectPool pool = new GenericObjectPool(factory);
        pool.setMaxActive(2);
        pool.setMinIdle(3);
        
        pool._numActive = 3;
        factory.activeCount = 3;
        // Above two lines should equal to pool.borrowObject()
        //Object obj = pool.borrowObject(); // numActive = 1, numIdle = 0
        
        // Create a test thread that will run once and try a borrow after
        // 150ms fixed delay
        TestThread borrower = new TestThread(pool);
        Thread borrowerThread = new Thread(borrower);
        
        // Create another thread that will call ensureMinIdle
        Thread minIdleThread = new Thread(new Runnable() {            
            @Override
                public void run() {
                try {
                    pool.ensureMinIdle();
                } catch (Exception e) {
                	//fail();
                	System.out.println(e);
                    // Ignore
                }
            }
            });
        
        // Off to the races
        minIdleThread.start();
        borrowerThread.start();  
        try {
			minIdleThread.join();
			borrowerThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
//        assertTrue(!borrower.failed());
//        pool.close();
    }
 
    protected static GenericObjectPool pool = null;
  
    @Test
	public void test() throws InterruptedException {
		try {
			pool107.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}
}


