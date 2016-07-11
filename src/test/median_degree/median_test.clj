(ns median-degree.median-test
  (:require [clojure.test :refer :all]
            [median-degree.median :refer :all]))

(def simple-coll '(1 1 1 2))

(def long-coll (range 15000))

(def empty-coll '())

(deftest median-test
  "given a coll of numeric values, return median value"
  (testing "simple coll of values"
    (is (= 1 (median simple-coll))))
  (testing "long coll of values"
    (is (= (/ 14999 2) (median long-coll))))
  (testing "empty coll"
    (is (thrown? IndexOutOfBoundsException (median empty-coll)))))
