package edu.tamu.aser.tests.pooltest;

import pool107.org.apache.commons.pool.ObjectPool;

public class TestThread implements Runnable {
    ObjectPool _pool = null;
    boolean _failed = false;

    public TestThread(ObjectPool pool) {
        _pool = pool;
    }


    public boolean failed() {
        return _failed;
    }

    public void run() {
            Object obj = null;
            try {
                obj = _pool.borrowObject();
            } catch(Exception e) {
                _failed = true;
            }

    }
}
