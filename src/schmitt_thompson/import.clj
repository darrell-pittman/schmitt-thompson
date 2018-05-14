(ns schmitt-thompson.import
  (:require [schmitt-thompson.schema :as sch])
  (:require [schmitt-thompson.config :as cfg])
  (:require [schmitt-thompson.utils :as utils])
  (:require [taoensso.faraday :as far])
  (:require [clojure.java.jdbc :as j])
  (:require [clojure.core.async
             :as a
             :refer [chan <!! >!! <! go thread go-loop close! put!]]))

(defn protocol-key [year type]
  (str year ":" type))

(defn import-cfg [year type full-path-to-schmitt-db]
  (let [app-config (cfg/config (cfg/profile))]
    {:protocol-key (protocol-key year type)
     :dynamo-opts (cfg/dynamo-db app-config)
     :dynamo-table (:aws-table app-config)   
     :schmitt-db (cfg/schmitt-db full-path-to-schmitt-db)}))

(defn put-item [schema protocol-key row]
  (let [pk ((:pk schema) protocol-key row)
        sk ((:sk schema) protocol-key row)
        data ((:data schema) protocol-key row)
        attrs (reduce #(assoc %1 %2 (%2 row)) {} (:attrs schema)) ]
    (merge
     {:pk pk
      :sk sk
      :data data} attrs)))

(defn put-protocol [protocol-key, year, type]
  (let [out (chan)
        p (put-item sch/protocol protocol-key {:year year :type type})]
    (put! out p)
    (close! out)
    out))

(defn put-entity [schema query info]
  (let [out (chan)
        {:keys [protocol-key schmitt-db]} info]
    (future (j/query
             schmitt-db
             query
             {:row-fn (fn [row] (put-item schema protocol-key row))
              :result-set-fn (fn [rs]
                               (doseq [item rs]
                                 (>!! out item))
                               (close! out))}))
    out))

(defn import-protocol [year type full-path-to-schmitt-db schemas]
  (let [info (import-cfg year type full-path-to-schmitt-db)
        {:keys [protocol-key]} info
        protocol-item (put-protocol protocol-key year type)]
    (conj
     (map (fn [schema]
            (let [sql ((:import-sql schema))]
              (put-entity schema sql info)))
          schemas)
     (put-protocol protocol-key year type))))

;;Beyond here be testing


(defn table-items [table-name & puts-deletes]
  (let [items-map (zipmap [:put :delete] puts-deletes)
        filtered-map (utils/filter-map-by-val items-map seq)]
  {table-name filtered-map}))

(def dynamo-db (cfg/dynamo-db (cfg/config (cfg/profile))))

(let [in (a/merge (import-protocol
          2017
          "ADULT"
          "/home/monkey/p30m/protocols/import/databases/2017/Algorithms_adult_AH_data.mdb"
          [sch/algorithm-advice]))]

  (loop [num-items 0]
    (let [batch (a/take 25 in)
          items (<!! (a/into [] batch))
          stmt (table-items :dev.protocols items)]
      (when (seq items)
        (let [new-num (+ num-items (count items))
              num-dots (mod (/ new-num 25) 80)]
          (clojure.pprint/pprint stmt)
          ;;(println (apply str (repeat num-dots ".")))
          ;;(far/batch-write-item
          ;; dynamo-db
          ;; stmt)
          (recur new-num)))))
  (println "Done!"))


