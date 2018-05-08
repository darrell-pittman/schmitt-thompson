(ns schmitt-thompson.core
  (:require [taoensso.faraday :as far])
  (:require [clojure.java.jdbc :as j])
  (:require [schmitt-thompson.config :as cfg])
  (:gen-class))

(defn schmitt-db [full-path]
  {:connection-uri
   (str "jdbc:ucanaccess://" full-path)})

(def ^:const app-config (cfg/config :dev))

(defn dynamo-db [app-config]
  {:access-key (:aws-access-key app-config)
   :secret-key (:aws-secret-key app-config)
   :endpoint (:aws-endpoint app-config)})

(defn first-five []
  (j/query (schmitt-db "/home/monkey/p30m/protocols/import/databases/2017/Algorithms_adult_AH_data.mdb")
           ["select AlgorithmID, Title from algorithm"],
           {:row-fn
            (fn [row]
              (println row))}))

(first-five)


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!xxx" args))







