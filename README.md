# Venmo Transaction Graph & Median Node Degree

a clojure approach to the [insight data engineering code challenge](https://github.com/InsightDataScience/coding-challenge)

## Dependencies

### java
JDK 8 - preferably [openjdk](http://openjdk.java.net/install/) - must be installed. `$ java` & `$ javac` should be on PATH and configured to resolve jdk8:

```bash
$ java -version
openjdk version "1.8.0_91"
OpenJDK Runtime Environment (build 1.8.0_91-8u91-b14-0ubuntu4~14.04-b14)
OpenJDK 64-Bit Server VM (build 25.91-b14, mixed mode)
```

### CA certificates
Lein will attempt to fetch dependencies from clojar and maven repositories that may use https. On some systems (particularly virtual machines) the system ca certificates may need to be refreshed before dependencies can be fetched and the project compiled:

on debian:

    $ sudo update-ca-certificates -f

on rhel-based (see [redhat's accepted solution](https://access.redhat.com/solutions/1549003) for more detail):

    $ sudo update-ca-trust extract 

### lein

[leiningen](http://leiningen.org/#install) must be installed and available on PATH to user running test suites.

*NOTE* please do not install leiningen from repository, but follow the instructions provided at http://leiningen.org/#install as the version in repository is far out of date and does not provide methods for updating.

```bash
$ lein -version
Leiningen 2.6.1 on Java 1.8.0_91 OpenJDK 64-Bit Server VM
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

    $ bash run.sh

(*NOTE*: this script includes arguments: `-i ./venmo_input/venmo-trans.txt -o ./venmo_output/output.txt` to provide compatibility with `$ cd ./insight_testsuite; ./run_tests.sh`)

### with lein run
from top directory:

    $ cd ./src; lein run [options]

### with java -jar
(example compiles to uberjar then runs)

from top directory:

    $ (cd ./src; lein uberjar) && java -jar ./src/target/uberjar/median-degree-0.1.0-standalone.jar [options]

## Options

   * `-i/--in ./path/to/infile`
   * `-o/--out ./path/to/outfile`

   if no args are provided, runtime defaults to:

   * `-i ../venmo_input/venmo-trans.txt`
   * `-o ../venmo_output/output.txt`

   this is because `$ lein run` must run from `./src` and the jvm path will initialize in that dir. 

