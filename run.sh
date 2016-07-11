#!/usr/bin/env bash

##dips into ./src to build uberjar, then runs with arguments to play nice with insight testsuite
(cd ./src && lein uberjar) && java -jar ./src/target/uberjar/median-degree-0.1.0-standalone.jar -i ./venmo_input/venmo-trans.txt -o ./venmo_output/output.txt
