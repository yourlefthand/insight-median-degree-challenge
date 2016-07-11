(ns median-degree.graph
  "namespace manages graph of venmo transactions.
  stores graph in atomic hashmap with set of node identities as keys
  and created times as values."
  (:require [clj-time.core :as time]
            [clj-time.coerce :as ctime]
            [clojure.core.reducers :as r]
            [clojure.set :refer [union]]
            [median-degree.schema :refer [tx-validate]]))

(def venmo-graph
  "initializes graph as empty map on require"
  (atom {}))

(def latest-transaction
  "initializes as losing transaction date on require"
  (atom (ctime/from-long 0)))

(defn compare-transaction-times
  "compares transaction times"
  [a b]
  (if (time/after? a  b)
    a b))

(def latest-transaction!
  "updates compares and updates the latest transaction information
  given a new edge"
  (partial swap! latest-transaction compare-transaction-times))

(defn find-latest-transaction
  "expects a vector of median-degree.schema/VenmoTX structs,
  reduces to the latest tx"
  [graph]
  (reduce compare-transaction-times (vals graph)))

(defn eviction-limit
  "provides boolean predicate on whether a datetime falls sooner than
  60 seconds of a given datetime"
  [target limit]
  #(time/after? % (time/minus target limit))) 

(defn evict-edges
  "takes existing graph & new transaction,
  evicts outdated edges and returns valid graph"
  ([graph]
   (let [latest-created (find-latest-transaction graph)]
     (evict-edges graph latest-created)))
  ([graph latest-created]
     (evict-edges graph latest-created (time/seconds 60)))
  ([graph latest-created limit]
     (into {} (filter 
                (comp (eviction-limit latest-created limit)
                      val) graph))))

(defn merge-into-graph
  "merges edge into graph - replaces value if created time is newer
  guarantees only one, undirected edge in graph for any node pair"
  [graph edge]
  (merge-with compare-transaction-times graph edge))

(defn form-edge-from-tx
  "forms an edge from a tx object, where an edge consists of a hashmap:
  {#{actor target} time_created}
  using a set of node ids as this graph is undirected"
  [tx]
  (let [tx (tx-validate tx)]
    {(apply hash-set ((juxt :actor :target) tx)) (:created_time tx)}))

(defn fetch-edges
  "fetches edges of graph from atomic graph unless
  graph is provided as arg"
  ([]
   (fetch-edges @venmo-graph))
  ([graph]
   (keys graph)))

(defn fetch-nodes
  "fetches a set of node ids
  grabs from atomic graph unless graph is provided as arg"
  ([]
   (fetch-nodes @venmo-graph))
  ([graph]
   (reduce union (fetch-edges graph))))

(defn fetch-node-degrees
  "returns node degrees for atomic graph unless graph is specified"
  ([]
   (fetch-node-degrees @venmo-graph))
  ([graph]
   (frequencies (flatten (map vec (fetch-edges graph))))))

(defn fetch-adjacency-list
  "fetches edge-list as undirected adjacency list
  uses atomic graph unless graph is provided as arg"
  ([]
   (fetch-adjacency-list @venmo-graph))
  ([graph]
   (fetch-adjacency-list graph {}))
  ([graph adj-list]
   (if (empty? graph)
     adj-list
     (let [head (key (first graph))]
       (fetch-adjacency-list (into {} (rest graph)) 
                             (merge-with union
                                         adj-list 
                                         {(first head) (hash-set (last head))}
                                         {(last head) (hash-set (first head))}))))))
(defn add-edge
  "takes a median-degree.schema/VenmoTX struct and upserts into graph
  evicts edges and returns valid edge list of tx graph"
  ([graph new-edge]
   (evict-edges (merge-into-graph graph new-edge)))
  ([graph new-edge latest-transaction]
   (evict-edges (merge-into-graph graph new-edge) latest-transaction)))

(def add-edge!
  "swaps atomic value of venmo-graph
  with update per new edge"
  (partial swap! venmo-graph add-edge)) 

