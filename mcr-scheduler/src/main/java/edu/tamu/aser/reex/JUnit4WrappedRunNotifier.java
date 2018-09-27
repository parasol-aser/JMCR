package edu.tamu.aser.reex;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

public class JUnit4WrappedRunNotifier extends RunNotifier {

    private final RunNotifier notifier;

    private boolean testExpStarted;

    private Description runningTestDescription;

    private Failure testFailure;

    public JUnit4WrappedRunNotifier(RunNotifier notifier) {
        this.notifier = notifier;
    }

    /**
     * Test exploration is starting reset failed status
     */
    public void testExplorationStarted() {
        this.testExpStarted = true;
        this.testFailure = null;
    }

    /**
     * Only fire started event if the exploration is starting
     */
    @Override
    public void fireTestStarted(Description description) throws StoppedByUserException {
        if (this.testExpStarted) {
            this.notifier.fireTestStarted(description);
            this.runningTestDescription = description;
            // No longer starting
            this.testExpStarted = false;
        }
    }

    /**
     * Intercept test failure
     */
    @Override
    public void fireTestAssumptionFailed(Failure failure) {
        this.notifier.fireTestAssumptionFailed(failure);
        this.testFailure = failure;
    }

    /**
     * Intercept test failure
     */
    @Override
    public void fireTestFailure(Failure failure) {
        this.notifier.fireTestFailure(failure);
        this.testFailure = failure;
    }
    
    public void setFailure(Failure failure) {
        this.testFailure = failure;
    }

    /**
     * Return current test's failure status
     * 
     * @return current test's failure status
     */
    public boolean isTestFailed() {
        return this.testFailure != null;
    }
    
    /**
     * Return current test's failure object
     * 
     * @return current test's failure object
     */
    public Failure getFailure() {
        return this.testFailure;
    }

    /**
     * Do not fire test finished event until exploration is finished.
     */
    @Override
    public void fireTestFinished(Description description) {
        // Will be fired when exploration is completed.
    }

    /**
     * Fires the test finished event.
     */
    public void testExplorationFinished() {
        this.notifier.fireTestFinished(this.runningTestDescription);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.junit.runner.notification.RunNotifier#fireTestIgnored(org.junit.runner
     * .Description)
     */
    @Override
    public void fireTestIgnored(Description description) {
        this.notifier.fireTestIgnored(description);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.junit.runner.notification.RunNotifier#addFirstListener(org.junit.
     * runner.notification.RunListener)
     */
    @Override
    public void addFirstListener(RunListener listener) {
        this.notifier.addFirstListener(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.junit.runner.notification.RunNotifier#addListener(org.junit.runner
     * .notification.RunListener)
     */
    @Override
    public void addListener(RunListener listener) {
        this.notifier.addListener(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.junit.runner.notification.RunNotifier#fireTestRunFinished(org.junit
     * .runner.Result)
     */
    @Override
    public void fireTestRunFinished(Result result) {
        this.notifier.fireTestRunFinished(result);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.junit.runner.notification.RunNotifier#fireTestRunStarted(org.junit
     * .runner.Description)
     */
    @Override
    public void fireTestRunStarted(Description description) {
        this.notifier.fireTestRunStarted(description);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.junit.runner.notification.RunNotifier#pleaseStop()
     */
    @Override
    public void pleaseStop() {
        this.notifier.pleaseStop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.junit.runner.notification.RunNotifier#removeListener(org.junit.runner
     * .notification.RunListener)
     */
    @Override
    public void removeListener(RunListener listener) {
        this.notifier.removeListener(listener);
    }

}
