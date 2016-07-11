(ns median-degree.schema-test
  (:require [clj-time.core :as time]
            [clj-time.coerce :as ctime]
            [clojure.test :refer :all]
            [median-degree.schema :refer :all]
            [schema.core :as s]
            [schema.coerce :as c])
  (:import org.joda.time.DateTime))

(def now
  (time/now))

(def date-string
  "2016-03-28T23:23:12Z")

(def datetime-from-string
  (ctime/from-string date-string))

(def simple-schema
  {:foo s/Str
   :bar s/Keyword})

(deftest validate-against-VenmoTX-test
  "attempts to validate generic maps against schema"
  (testing "validation succeeds"
    (let [schema {:actor :foo :target :bar :created_time now}]
      (is (= schema (s/validate VenmoTX schema)))))
  (testing "validation succeeds with additional keys"
    (let [schema {:actor :foo :target :bar :created_time now :foo "bar" :baz 2}]
      (is (= schema (s/validate VenmoTX schema)))))
  (testing "validation fails"
    (let [schema {:actor :foo :target :bar}]
      (is (thrown? clojure.lang.ExceptionInfo (s/validate VenmoTX schema)))))
  (testing "validation fails"
    (let [schema {}]
      (is (thrown? clojure.lang.ExceptionInfo (s/validate VenmoTX schema))))))

(deftest coerce-and-validate-test
  "tests function that coerces and validates"
  (testing "simple schema coercion succeeds"
    (let [m {:foo "baz" :bar "qux"}]
      (is (= {:foo "baz" :bar :qux} 
             (coerce-and-validate simple-schema c/json-coercion-matcher m)))))
  (testing "simple schema coercion fails"
    (let [m {:foo "baz" :bar 15}]
     (is (thrown? clojure.lang.ExceptionInfo
                 (coerce-and-validate simple-schema c/json-coercion-matcher m))))))

(deftest tx-validate-test
  "tests coercion/validation against VenmoTX"
  (testing "coercion/validation succeeds"
    (let [data {:target "foo" :actor "bar" :created_time date-string}]
      (is (= {:target :foo :actor :bar :created_time datetime-from-string}
             (tx-validate data)))))
  (testing "coercion/validation fails on coercion"
    (let [data {:target "foo" :actor "bar" :created_time "invalid date string"}]
      (is (thrown? clojure.lang.ExceptionInfo
                   (tx-validate data)))))
  (testing "coercion/validation fails on validation"
    (let [data {:target "foo" :created_time date-string}]
      (is (thrown? clojure.lang.ExceptionInfo
                   (tx-validate data))))))
      
