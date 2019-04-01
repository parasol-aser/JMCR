[![Build Status](https://travis-ci.org/travis-ci/travis-web.svg?branch=master)](https://travis-ci.org/travis-ci/travis-web)

# Maximal-Causality-Reduction (MCR) Java version

MCR is a stateless model checker powered by an efficient reduction algorithm. It systematically explores the state-space of the program by collecting runtime traces of the program executions and constructing ordering constraints over the traces to generate other possible schedules. It captures the values of the writes and reads to prune redundant explorations. By enforcing at least one read to return a different value, it generates a new schedule which drives the program to reach a new state. 

# Getting Started

## Environment
* Install z3 
	* Follow https://github.com/Z3Prover/z3
	* Add `z3` to your PATH
* Install Apache Maven http://maven.apache.org/install.html


## How to Run
First git clone the project. To compile mcr, run `mvn package ` under the root directory of the project, it will generates jar files into `build` directory. 

To run the tests under `mcr-test/`, run the bash script `mcr_cmd`. 
The usage of `mcr_cmd`:

```
usage: ./mcr_cmd [options] test_class [parameters]
  e.g., ./mcr_cmd [options] edu.tamu.aser.rvtest_simple_tests.Example
  options:
  	--help -h usage
	--debug -D print debug information
	--static -S using static analysis       (see the ECOOP'17 paper)
	--class_path absolute_dir -c absolute_dir
					if this is not specified, default the current bin/ as the class path
	--memmory-model MM -m MM selecting the memory model SC/TSO/PSO  (see the OOPLSA'16 paper)
	
```

If `-S` is specified, the tool will first run static analysis to generate the system dependencies graph into `SDGs` and then explore the program. The number of the reads and constraitns as well as the constraints solving times are save to the file under `stats/`.

`-m` selects the executed memory model. We currently support SC/TSO/PSO, and use SC. 

### to run the users' own tests

MCR works with JUnit 4. Given a JUnit 4 test class, it will explore
each of the tests in the test class. To use MCR, you need to add the
following annotation "@RunWith(JUnit4MCRRunner.class)" to the
test class. Users can refer to the benchmarks we put under 'mcr-test/' 
for more ideas about how to write the tests.

## An Example

MCR is able to detect concurrency errors caused by deadlocks, order violation, data race and so on. The following code contains a potential deadlock.  (See `TestDeadLock.java` under `edu.tamu.aser.results`).


```
	public void increment1() {
        synchronized (mutex1) {
            this.field = 1;
            synchronized (mutex2) { }
        }
    }

    public void increment2() {
        synchronized (mutex2) {
            int r = this.field;
            synchronized (mutex1) { }
        }
    }

    public int getField() {
        return this.field;
    }

    @Test
    public void test() throws InterruptedException {

        final TestDeadLock counter = new TestDeadLock();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() { counter.increment1(); }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() { counter.increment2(); }
        });

        t1.start(); t2.start();

        t1.join();  t2.join();
    }
```

After running MCR on the program above (`./mcr_cmd edu.tamu.aser.tests.TestDeadLock`), MCR can generate the specific interleaving which triggers the deadlock and reproduce the failure meanwhile: 

```
!!! FAILURE DETECTED DURING EXPLORATION OF SCHEDULE #2: Deadlock detected in schedule
The following trace triggered this error:
       NewExploration_TestDeadLock.java:58:start
       NewExploration_TestDeadLock.java:59:start
       Thread-6_TestDeadLock.java:54:read
       Thread-6_TestDeadLock.java:23:read
       Thread-6_TestDeadLock.java:23:Lock
       Thread-6_TestDeadLock.java:29:read
       Thread-5_TestDeadLock.java:47:read
       Thread-5_TestDeadLock.java:14:read
       Thread-5_TestDeadLock.java:14:Lock
       Thread-5_TestDeadLock.java:15:write
       Thread-5_TestDeadLock.java:16:read
       Thread-6_TestDeadLock.java:30:read


```





# Useful Documents
* [ECOOP'17] [Speeding Up Maximal Causality Reduction with Static Dependency Analysis](https://huangshiyou.github.io/files/Huang-ECOOP-2017-16.pdf)

* [OOPSLA'16] [Maximal Causality Reduction for TSO and PSO](https://huangshiyou.github.io/files/mcr_relax-huang.pdf)

* [PLDI'15] [Stateless Model Checking Concurrent Programs with Maximal Causality Reduction](https://parasol.tamu.edu/~jeff/academic/mcr.pdf)

* [PLDI'14] [Maximal Sound Predictive Race Detection
with Control Flow Abstraction](http://fsl.cs.illinois.edu/FSL/papers/2014/huang-meredith-rosu-2014-pldi/huang-meredith-rosu-2014-pldi-public.pdf)

