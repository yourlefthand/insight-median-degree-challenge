(ns median-degree.core
  (:require [cheshire.core :refer :all]
            [clojure.tools.cli :refer [parse-opts]]
            [median-degree.graph :as graph]
            [median-degree.schema :refer [tx-validate]])
  (:import (com.fasterxml.jackson.core JsonParseException))
  (:gen-class))

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
                graph (graph/add-edge! (graph/form-edge-from-tx valid-tx))]
            (println (graph/fetch-node-degrees graph)))
          (catch JsonParseException e
            (println (str "unable to form json from " tx ":" (.getStackTrace e))))
          (catch clojure.lang.ExceptionInfo e
            (if (= :coercion-exception (-> e ex-data :type))
              (println (str "unable to validate data for tx: " tx ":" e)))))))))
