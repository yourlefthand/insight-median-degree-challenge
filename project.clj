(defproject median-degree "0.1.0"
  :dependencies [[cheshire "5.6.3"]
                 [clj-time "0.12.0"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [prismatic/schema "1.1.2"]]
  :main ^:skip-aot median-degree.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
