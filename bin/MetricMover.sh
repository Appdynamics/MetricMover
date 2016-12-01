#!/bin/bash

echo $*
java -Dmetric.populator.home=../ -cp "../lib/*:../conf" com.appdynamics.tools.metricmover.Runner $*
