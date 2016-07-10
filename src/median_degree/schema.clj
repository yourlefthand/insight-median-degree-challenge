(ns median-degree.schema
  (:require [schema.core :as s]
            [schema.coerce :as c]
            [schema.utils :as s-utils]
            [clj-time.core :as time]
            [clj-time.coerce :as ctime])
  (:import org.joda.time.DateTime))

(s/defschema VenmoTX
  "schema for coercing 
   and validating venmo transactions"
  {:target s/Str
   :actor s/Str
   :created_time DateTime})

(defn date-matcher
  "provides coercion from string to date"
  [schema]
  (when (= DateTime schema)
    (c/safe
      (fn [x]
        (if (string? x) (ctime/from-string x) x)))))

(def tx-matcher
  "provides datestring + json coercion"
  (c/first-matcher [date-matcher c/json-coercion-matcher]))

(defn coerce-and-validate
  "generic coercion/validation with throw"
  [schema matcher data]
  (let [coercer (c/coercer schema matcher)
        result (coercer data)]
    (if (s-utils/error? result)
      (let [error (s-utils/error-val result)]
        (throw (ex-info (format "Value does not match schema :%s"
                                error)
                        {:type :coercion-exception
                         :cause error})))
      result)))

(defn tx-validate
  "coercion/validation for VenmoTX"
  [data]
  (coerce-and-validate VenmoTX tx-matcher data))
