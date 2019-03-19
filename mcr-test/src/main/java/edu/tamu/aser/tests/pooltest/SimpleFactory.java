package edu.tamu.aser.tests.pooltest;

import pool107.org.apache.commons.pool.PoolableObjectFactory;

public class SimpleFactory implements PoolableObjectFactory {
    public SimpleFactory() {
        this(true);
    }
    public SimpleFactory(boolean valid) {
        this(valid,valid);
    }
    public SimpleFactory(boolean evalid, boolean ovalid) {
        evenValid = evalid;
        oddValid = ovalid;
    }
    void setValid(boolean valid) {
        setEvenValid(valid);
        setOddValid(valid);            
    }
    void setEvenValid(boolean valid) {
        evenValid = valid;
    }
    void setOddValid(boolean valid) {
        oddValid = valid;
    }
    public void setThrowExceptionOnPassivate(boolean bool) {
        exceptionOnPassivate = bool;
    }
    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }
    public void setDestroyLatency(long destroyLatency) {
        this.destroyLatency = destroyLatency;
    }
    public void setMakeLatency(long makeLatency) {
        this.makeLatency = makeLatency;
    }
    public Object makeObject() { 
        synchronized(this) {
            activeCount++;
            if (activeCount > maxActive) {
                throw new IllegalStateException(
                    "Too many active instances: " + activeCount);
            }
        }
        return "1";
    }
    public void destroyObject(Object obj) {
        if (destroyLatency > 0) {
            doWait(destroyLatency);
        }
        synchronized(this) {
            activeCount--;
        }
    }
    public boolean validateObject(Object obj) {
        if (enableValidation) { 
            return validateCounter++%2 == 0 ? evenValid : oddValid; 
        }
        else {
            return true;
        }
    }
    public void activateObject(Object obj) throws Exception {
        if (exceptionOnActivate) {
            if (!(validateCounter++%2 == 0 ? evenValid : oddValid)) {
                throw new Exception();
            }
        }
    }
    public void passivateObject(Object obj) throws Exception {
        if(exceptionOnPassivate) {
            throw new Exception();
        }
    }
    int makeCounter = 0;
    int validateCounter = 0;
    public int activeCount = 0;
    boolean evenValid = true;
    boolean oddValid = true;
    boolean exceptionOnPassivate = false;
    boolean exceptionOnActivate = false;
    boolean enableValidation = true;
    long destroyLatency = 0;
    long makeLatency = 0;
    int maxActive = Integer.MAX_VALUE;

    public boolean isThrowExceptionOnActivate() {
        return exceptionOnActivate;
    }

    public void setThrowExceptionOnActivate(boolean b) {
        exceptionOnActivate = b;
    }

    public boolean isValidationEnabled() {
        return enableValidation;
    }

    public void setValidationEnabled(boolean b) {
        enableValidation = b;
    }
    
    private void doWait(long latency) {
        try {
            Thread.sleep(latency);
        } catch (InterruptedException ex) {
            // ignore
        }
    }
}
