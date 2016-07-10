(ns median-degree.core
  (:require [cheshire.core :refer :all]
            [clj-time.core :as time]
            [clj-time.coerce :as ctime]
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

(defn median
  "given a set of numbers, return the median val"
  [nc]
  (let [nc (sort nc)
        cnt (count nc)
        mid (bit-shift-right cnt 1)]
    (if (odd? cnt)
      (nth nc mid)
      (/ (+ (nth nc mid) (nth nc (dec mid))) 2))))

(defn median-degree
  "takes a map of node id to degree and returns
  median degree value"
  [node-degrees]
  (median (vals node-degrees)))

;also should be more generic
(defn degree-nodes
  "reduces a graph to an array of node references
  and further reduces to an instance count"
  [graph]
  (frequencies (flatten (mapv #((juxt :target :actor ) %) graph))))

;this should be made more generic btw
(defn evict-edges
  "finds the newest timestamp in graph, filters values accordingly"
  [graph new-tx]
  (let [graph (conj graph new-tx)
        latest-tx (reduce (fn [a b] 
                            (if (time/after? (:created_time a)
                                             (:created_time b)) 
                              a
                              b)) graph)
        latest-time (:created_time latest-tx)
        eviction-limit (time/minus latest-time (time/seconds 60))]
    (filter #(time/after? (:created_time %) eviction-limit) graph)))

(defn update-graph!
  "swaps the graph value for an updated val"
  [new-tx]
  (swap! venmo-graph evict-edges new-tx))

(defn -main
  "takes input filepath and output filepath as arguments
   input filebuffer is used to form graph of user interactions
   graph is used to calculate median node degree"
  [& args]
  (let [file-opts (:options (parse-opts args cli-options))]
    (with-open [in-file (clojure.java.io/reader (:in file-opts))]
      (doseq [tx (line-seq in-file)]
        (try 
          (let [valid-tx (tx-validate (parse-string tx true))
                graph (update-graph! valid-tx)]
            (println (median-degree (degree-nodes graph))))
          (catch JsonParseException e
            (println (str "unable to form json from " tx ":" (.getStackTrace e))))
          (catch clojure.lang.ExceptionInfo e
            (if (= :coercion-exception (-> e ex-data :type))
              (println (str "unable to validate data for tx: " tx ":" e))))))))))
