(ns median-degree.graph-test
  (:require [clj-time.core :as time]
            [clojure.test :refer :all]
            [median-degree.graph :refer :all]))

(def now
  (time/now))

(def later
  (time/plus (time/now) (time/seconds 60)))

(deftest form-edge-from-tx-test
  "takes a VenmoTX struct and returns an edge map"
  (testing "valid VenmoTX struct"
    (let [venmo-tx {:actor :foo :target :bar :created_time now}]
      (is (= {#{:foo :bar} now} (form-edge-from-tx venmo-tx)))))
  (testing "invalid VenmoTX struct"
    (let [venmo-tx {}]
      (is (thrown? clojure.lang.ExceptionInfo (form-edge-from-tx venmo-tx))))))

(deftest marge-into-graph-test
  "takes a valid edge struct and merges into graph"
  (let [graph {}]
    (testing "add empty struct to empty graph"
      (let [edge {}]
        (is (= 0 (count (merge-into-graph graph edge))))))
    (let [edge1 {#{:foo :bar} now}
          edge2 {#{:bar :baz} later}
          graph1 (merge-into-graph graph edge1)]
      (testing "graph with added edge"
        (is (= 1 (count graph1))))
      (testing "graph with two edges"
        (is (= 2 (count (merge-into-graph graph1 edge2)))))
      (testing "add overlapping edges, newer one wins"
        (let [edge3 {#{:foo :bar} later}
              graph2 (merge-into-graph graph1 edge3)]
          (is (and (= 1 (count graph2)) (time/equal? later (get graph2 #{:foo :bar})))))))))

(deftest evict-edges-test
  "takes a graph and evicts edges given a latest-created timestamp and a limit"
  (let [graph {#{:foo :bar} now
               #{:bar :baz} later}]
    (testing "single arity"
      (is (= 1 (count (evict-edges graph)))))
    (testing "dual arity"
      (is (= 1 (count (evict-edges graph later)))))
    (testing "triple arity"
      (is (= 1 (count (evict-edges graph later (time/seconds 60))))))
    (testing "alter created timestamp"
      (is (= 2 (count (evict-edges graph now)))))
    (testing "alter created timestamp"
      (is (= 2 (count (evict-edges graph later (time/seconds 120))))))))

(deftest eviction-limit-test
  "tests predicate form function that gives a test predicate givin a timestamp
  and a limit"
  (testing "is after"
    (is ((eviction-limit now (time/seconds 60)) later)))
  (testing "is not after"
    (is (not ((eviction-limit later (time/seconds 0)) now)))))

(deftest find-latest-transaction-test
  "tests function that reduces graph of expected form to latest
  edge-creation timestamp"
  (let [graph {#{:foo :bar} now
               #{:foo :baz} now
               #{:bar :baz} later
               #{:baz :qux} later}]
    (testing "valid graph"
      (is (time/equal? later (find-latest-transaction graph)))))
  (let [graph {}]
    (testing "invalid graph"
      (is (thrown? clojure.lang.ArityException (find-latest-transaction graph))))))

(deftest compare-transaction-times-test
  "tests transaction times comparison func"
  (testing "now then later"
    (is (time/equal? later (compare-transaction-times now later))))
  (testing "later then now"
    (is (time/equal? later (compare-transaction-times now later))))
  (testing "now then now"
    (is (time/equal? now (compare-transaction-times now now))))
  (testing "now then inl"
    (is (nil? (compare-transaction-times now nil)))))

(deftest add-edge-test
  "tests adding edge to graph - function adds and evicts"
  (let [edge {#{:bar :baz} later}
        edge1 {#{:foo :baz} later}
        graph {#{:foo :bar} now}]
    (testing "edge added and old edge evicted"
      (is (= edge (add-edge graph edge))))
    (testing "old edge added, no change"
      (is (= graph (add-edge graph graph))))
    (testing "edges added with evictions"
      (is (= (merge edge edge1) (add-edge edge edge1))))))

(let [graph {#{:foo :bar} now
             #{:bar :baz} later
             #{:foo :baz} later}]
  (deftest fetch-edges-test
    "tests fetching edges from graph struct"
    (testing "fetch-edges"
        (is (= (keys graph) (fetch-edges graph)))))
  
  (deftest fetch-nodes-test
    "tests fetchin nodes from graph struct"
    (testing "fetch nodes"
      (is (= #{:foo :bar :baz} (fetch-nodes graph)))))

  (deftest fetch-node-degrees-test
    "tests fetchin node degrees from graph struct"
    (testing "fetch-node-degrees"
      (is (= {:foo 2 :bar 2 :baz 2} (fetch-node-degrees graph)))))

  (deftest fetch-adjacency-list-test
    "tests fetching adjacency list view of graph"
    (testing "fetch-adjacency-list"
      (is (= {:foo #{:bar :baz}
              :bar #{:foo :baz}
              :baz #{:foo :bar}} (fetch-adjacency-list graph))))))

(deftest add-edge!-test
  "tests atom swap"
  (testing "empty atom"
    (is (= {} @venmo-graph)))
  (testing "non-empty graph after swap"
    (let [edge {#{:foo :bar} now}]
      (is (and (= edge (add-edge! edge)) (= edge @venmo-graph))))))
