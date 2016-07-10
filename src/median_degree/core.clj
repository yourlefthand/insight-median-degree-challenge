(ns median-degree.core
  (:require [cheshire.core :refer :all]
            [clj-time.core :as time]
            [clojure.tools.cli :refer [parse-opts]]
            [median-degree.schema :refer [tx-validate]])
  (:import (com.fasterxml.jackson.core JsonParseException))
  (:gen-class))

(def venmo-graph
  (atom (vector)))

(def cli-options
  [["-i" "--in FILEPATH" 
    "input filepath relative to runtime path" 
    :default "./venmo_input/venmo-trans.txt"]
   ["-o" "--out FILEPATH" 
    "output filepath relative to runtime path"
    :default "./venmo_output/output.txt"]
   ["-h" "--help"]])

;also more generic
(defn reduce-to-degrees
  "reduces a graph to an array of node references
  and further reduces to an instance count"
  [graph]
  )
;this should be made more generic btw
(defn evict-edges
  "finds the newest timestamp in graph, filters values accordingly"
  [graph]
  (let [latest (max (map #(:time_created %) graph))
        eviction-limit (time/minus max-timestamp (time/seconds 60))]
    (filter #(> % eviction-limit) graph)))

(defn update-graph
  "swaps the graph value for an updated val"
  [f]
  (swap! venmo-graph f))

(defn -main
  "takes input filepath and output filepath as arguments
   input filebuffer is used to form graph of user interactions
   graph is used to calculate median node degree"
  [& args]
  (let [file-opts (:options (parse-opts args cli-options))]
    (with-open [in-file (clojure.java.io/reader (:in file-opts))]
      (doseq [tx (line-seq in-file)]
        (try 
          (let [valid-tx (tx-validate (parse-string tx true))]
            
            )
          (catch JsonParseException e
            (println (str "unable to form json from " tx ":" (.getStackTrace e))))
          (catch Exception e
            (println (str "unable to validate data for tx: " tx ":" (.getStackTrace e)))))))))
