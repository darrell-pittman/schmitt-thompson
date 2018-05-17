(ns schmitt-thompson.dynamo
  (:require [schmitt-thompson.utils :as utils])
  (:require [schmitt-thompson.config :as cfg])
  (:require [taoensso.faraday :as far]))

(def ^:const batch-size 25)

(let [app-config (cfg/config (cfg/profile))]
  (def opts (cfg/dynamo-db app-config))
  (def table (:aws-table app-config)))


(defn table-items [table-name & puts-deletes]
  (let [items-map (zipmap [:put :delete] puts-deletes)
        filtered-map (utils/filter-map-by-val items-map seq)]
  {table-name filtered-map}))


(def writer (partial
             utils/sync-reader
             (fn [items]
               (time (far/batch-write-item
                      opts
                      (table-items table items))))
             batch-size))

