(ns schmitt-thompson.import
  (:require [schmitt-thompson.schema :as sch])
  (:require [schmitt-thompson.config :as cfg])
  (:require [schmitt-thompson.utils :as utils])
  (:require [schmitt-thompson.dynamo :as dyn])
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

(defn import-protocol [year type info schemas reader]
  (let [{:keys [protocol-key]} info]
    (reader (put-protocol protocol-key year type))
    (map (fn [schema]
           (let [sql ((:import-sql schema))]
             (reader (put-entity schema sql info))))
         schemas)))

;;Beyond here be testing


(def console-writer
  (partial utils/sync-reader
           #(clojure.pprint/pprint (dyn/table-items dyn/table %))
           dyn/batch-size))
           



(let [type "ADULT"
      year 2017
      path (str "/home/monkey"
                "/p30m/protocols/import/databases"
                "/2017/Algorithms_adult_AH_data.mdb")
      info (import-cfg year type path)
      schemas [sch/question]]
  ;;(console-writer (a/merge (import-protocol year type info schemas identity))))
  (import-protocol year type info schemas console-writer))
  
 
      
  
      
  


