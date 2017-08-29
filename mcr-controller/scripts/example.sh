#!/bin/bash

project_dir=..
dist_dir=${project_dir}/dist
lib_dir=${project_dir}/lib

examples_dir=${project_dir}/../schedule-controller-test
examples_bin_dir=${examples_dir}/bin

example_package=jBench.programs.account
example_class=${example_package}.AccountTest

java \
    -javaagent:${dist_dir}/instrumentor.jar \
    -Dimunit.instrumentation.packages=${example_package} \
    -Dimunit.exploration.schedulingstrategy=edu.uiuc.imunit.scheduling.strategy.BoundedSearchStrategy \
    -cp ${dist_dir}/explorer.jar:${lib_dir}/junit.jar:${lib_dir}/org.hamcrest.core.jar:${examples_bin_dir} \
    org.junit.runner.JUnitCore ${example_class}
