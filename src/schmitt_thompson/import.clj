(ns schmitt-thompson.import
  (:require [schmitt-thompson.schema :as sch])
  (:require [schmitt-thompson.config :as cfg])
  (:require [taoensso.faraday :as far])
  (:require [clojure.java.jdbc :as j])
  (:require [clojure.core.async :as a :refer [chan <!! >!! <! go thread go-loop close!]]))

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
  (put-item sch/protocol protocol-key {:year year :type type}))

(defn put-algorithms [info protocol-item]
  (let [sql (sch/select-statement sch/algorithm)
        {:keys [protocol-key schmitt-db]} info
        out (chan)]
    (thread (j/query schmitt-db
                     [sql]
                     {:row-fn (fn [row] (put-item sch/algorithm protocol-key row))
                      :result-set-fn (fn [rs]
                                       (doseq [item rs]
                                         (>!! out item))
                                       (close! out))}))
    out))


   
(defn import-protocol [year type full-path-to-schmitt-db]
  (let [info (import-cfg year type full-path-to-schmitt-db)
        {:keys [protocol-key dynamo-opts dynamo-table]} info
        protocol-item (put-protocol protocol-key year type)]
    (far/put-item dynamo-opts dynamo-table protocol-item)
    (put-algorithms info protocol-item)))


(let [in (import-protocol
          2017
          "ADULT"
          "/home/monkey/p30m/protocols/import/databases/2017/Algorithms_adult_AH_data.mdb")]

  (go-loop []
    (let [batch (a/take 25 in)
          items (<!! (a/into [] batch))]
          
      (println (str "Count: " (count items)))
      (if (seq items) (recur)))))


  
  
  
