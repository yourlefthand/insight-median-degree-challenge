# median-degree

a clojure approach to the [insight data engineering code challenge](https://github.com/InsightDataScience/coding-challenge)

## Installation

### Dependencies

   * java
      * JDK 8 - preferably [openjdk](http://openjdk.java.net/install/) - must be installed. `$ java` & `$ javac` should be on path and configured to point at jdk8:
        ```bash
           $ java -version
           openjdk version "1.8.0_91"
           OpenJDK Runtime Environment (build 1.8.0_91-8u91-b14-0ubuntu4~14.04-b14)
           OpenJDK 64-Bit Server VM (build 25.91-b14, mixed mode)
        ```
   * lein
      * [leiningen](http://leiningen.org/#install) must be installed and available on PATH to user running test suites:
        ```bash
           $ cd ./src; lein test
           lein test median-degree.graph-test

           lein test median-degree.median-test

           lein test median-degree.schema-test

           Ran 17 tests containing 43 assertions.
           0 failures, 0 errors.
        ``` 

## Usage

### with run.sh
from top directory:

    `$ bash run.sh`

(*NOTE*: this script includes arguments: `-i ./venmo_input/venmo-trans.txt -o ./venmo_output/output.txt` to provide compatibility with `$ cd ./insight_testsuite; ./run_tests.sh`)

### with lein run
from top directory:

    ```$ cd ./src; lein run [args]```

### with java -jar
(example compiles to uberjar then runs)

from top directory:

    ```$ (cd ./src; lein uberjar) && java -jar ./src/target/uberjar/median-degree-0.1.0-standalone.jar [args]```

## Options

   * `-i/--in ./path/to/infile`
   * `-o/--out ./path/to/outfile`

   if no args are provided, runtime defaults to:

   * `-i ../venmo_input/venmo-trans.txt`
   * `-o ../venmo_output/output.txt`

   this is because `$ lein run` must run from `./src` and the jvm path will initialize in that dir. 

