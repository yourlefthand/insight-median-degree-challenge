#!/usr/bin/env bash

##changes working dir to src, then runs lein to initialize main method

if [ ! -f ./src/target/uberjar/median-degree-0.1.0-standalone.jar ]; then
	(cd ./src && lein uberjar)
fi

java -jar ./src/target/uberjar/median-degree-0.1.0-standalone.jar -i ./venmo_input/venmo-trans.txt -o ./venmo_output/output.txt
