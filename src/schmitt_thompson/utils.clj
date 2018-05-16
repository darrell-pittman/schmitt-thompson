(ns schmitt-thompson.utils
  (:require [clojure.core.async
             :as a
             :refer [<!!]]))

(defn filter-map [m entry-fn pred]
  (into {} (filter (comp pred entry-fn) m)))

(defn filter-map-by-val [m pred]
  (filter-map m val pred))

(defn filter-map-by-key [m pred]
  (filter-map m key pred))

(defn sync-reader
  ([writer batch-size in] (sync-reader writer batch-size in 0))
  ([writer batch-size in num-items]
    (let [batch (a/take batch-size in)
          items (<!! (a/into [] batch))]
      (if (seq items)
        (let [new-num (+ num-items (count items))]
          (writer items)
          (recur writer batch-size in new-num))
        num-items))))
