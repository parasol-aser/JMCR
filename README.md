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

To run the tests under `mcr-test/`, run `the bash script `mcr_cmd`. 
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

The following shows a simple example (See `RVExample.java` under `edu.tamu.aser.results`).


```
package edu.tamu.aser.results;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.tamu.aser.reex.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class RVExample {

	private static int x;
	private static int y;
	private static Object lock = new Object();
	
	public static void main(String[] args) {	
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < 2; i++) {
					synchronized (lock) {
						x = 0;
					}
					if (x > 0) {
						y++;
						x = 2;
					}
				}
			}

		});

		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < 2; i++) {
					if (x > 1) {
						if (y == 3) {
							System.err.println("Find the error!");
						} else
							y = 2;
					}
				}
			}

		});
		t1.start();
		t2.start();

		for (int i = 0; i < 2; i++) {
			synchronized (lock) {
				x = 1;
				y = 1;
			}
		}
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() throws InterruptedException {
		try {
			x = 0;
			y = 0;
			RVExample.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}
}
```

After running MCR on the program above, we can generate the following output:

```
=============== EXPLORATION STATS ===============
NUMBER OF SCHEDULES: 107
EXPLORATION TIME: 0:00:04  + 04 milli sec
=================================================
Writing the #reads, #constraints, time to file ConstraitsProfile/edu.tamu.aser.results.RVExample

```





# Useful Documents
* [ECOOP'17] [Speeding Up Maximal Causality Reduction with Static Dependency Analysis](https://huangshiyou.github.io/files/Huang-ECOOP-2017-16.pdf)

* [OOPSLA'16] [Maximal Causality Reduction for TSO and PSO](https://huangshiyou.github.io/files/mcr_relax-huang.pdf)

* [PLDI'15] [Stateless Model Checking Concurrent Programs with Maximal Causality Reduction](https://parasol.tamu.edu/~jeff/academic/mcr.pdf)

* [PLDI'14] [Maximal Sound Predictive Race Detection
with Control Flow Abstraction](http://fsl.cs.illinois.edu/FSL/papers/2014/huang-meredith-rosu-2014-pldi/huang-meredith-rosu-2014-pldi-public.pdf)

