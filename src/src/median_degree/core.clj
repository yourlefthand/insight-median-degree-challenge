(ns median-degree.core
  (:require [cheshire.core :refer :all]
            [clojure.tools.cli :refer [parse-opts]]
            [median-degree.graph :as graph]
            [median-degree.median :refer [median]]
            [median-degree.schema :refer [tx-validate]])
  (:import (com.fasterxml.jackson.core JsonParseException))
  (:gen-class))

(def cli-options
  [["-i" "--in FILEPATH" 
    "input filepath relative to runtime path" 
    :default "../venmo_input/venmo-trans.txt"]
   ["-o" "--out FILEPATH" 
    "output filepath relative to runtime path"
    :default "../venmo_output/output.txt"]
   ["-h" "--help"]])

(defn -main
  "takes input filepath and output filepath as runtime arguments
   input filebuffer is used to form graph of user interactions
   graph is used to calculate median node degree"
  [& args]
  (let [file-opts (:options (parse-opts args cli-options))]
    (with-open [in-file (clojure.java.io/reader (:in file-opts))
                out-file (clojure.java.io/writer (:out file-opts))]
      (doseq [tx (line-seq in-file)]
        (try 
          (let [valid-tx (tx-validate (parse-string tx true))
                created-time (:created_time valid-tx)
                graph (graph/add-edge! (graph/form-edge-from-tx valid-tx)
                                       (graph/latest-transaction! created-time))]
            (.write out-file 
                  (format "%.2f\n" 
                          (double 
                            (median (vals (graph/fetch-node-degrees graph)))))))
          (catch JsonParseException e
            (spit "median-degree.log" (str "WARN: unable to form json from " tx ":" (.getStackTrace e) "\n") :append true))
          (catch clojure.lang.ExceptionInfo e
            (if (= :coercion-exception (-> e ex-data :type))
              (spit "median-degree.log" (str "WARN: unable to validate data for tx: " tx ":" e "\n") :append true))))))))
