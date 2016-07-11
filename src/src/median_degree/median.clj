(ns median-degree.median)

(defn median
  "given a coll of numbers, return the median val"
  [nc]
  (let [nc (sort nc)
        cnt (count nc)
        mid (bit-shift-right cnt 1)]
    (if (odd? cnt)
      (nth nc mid)
      (/ (+ (nth nc mid) (nth nc (dec mid))) 2))))
