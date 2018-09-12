# Design of MCR

The current version of MCR is implemented in Java using [ASM](http://asm.ow2.org/) and [Z3](https://github.com/Z3Prover/z3). The workflow of MCR is as follows.

<div style="text-align:center"><img src="mcr.png" alt="Drawing" style="width: 220px;" /></div>

It contains three major steps:

* collecting trace (`mcr-controller`)
* constructing constraints over the trace (`mcr-engine`)
* building new interleavings (a sequence of thread schedules) from the solution to the constraints (`mcr-engine`)

## Trace collection

To collect the trace of a target program, it first uses ASM to instrument the bytecode of the program. Then it executes the instrumented class under a dynamic scheduler and log the corresponding events. The logged events include accesses to shared memory locations, synchronizations (lock/unlock, wait/notify, etc.) and thread create/join. The rewrite of the bytecode is under `mcr-controller/src/edu.tamu.aser.rvinstrumentor/`. The scheduler implementation is under `mcr-controller/src/edu.tamu.aser.scheduling/`.

## Constraints Construction

After collecting the trace, MCR leverages the [Maximal Causality Model](http://fsl.cs.illinois.edu/FSL/papers/2014/huang-meredith-rosu-2014-pldi/huang-meredith-rosu-2014-pldi-public.pdf) to construct ordering constraints over the trace to compute all the possible interleavings that can be derived from the considered trace. For each event in the trace, MCR introduces an integer variable *O* to represent its order in the new interleaving. *E.g.,* if *e1* should happen before *e2* in the new interleaing, then their ordering is encoded as *O1<O2*. The construction of the constraint model is under `mcr-engine/src/edu/tamu/aser/mcr/constraints/`.


## Interleaving Generation
MCR uses the Z3 SMT solver to solve the constraints. If there exists a solution to the constraints, MCR can compute a sequence of events of which the ordering corresponds to the value of its corresponding variable *O*. Then MCR maps the sequence of events to the sequence of the corresponding thread ID as the seed interleaving. This is implemented under `mcr-engine/src/main/`.

