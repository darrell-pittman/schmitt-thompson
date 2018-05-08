(ns schmitt-thompson.config
  (:require [aero.core :refer (read-config, root-resolver, resource-resolver)]))

(defn config [profile]
  (read-config (clojure.java.io/resource "config.edn")
               {:profile profile}))


(defn dynamo-db [app-config]
  {:access-key (:aws-access-key app-config)
   :secret-key (:aws-secret-key app-config)
   :endpoint (:aws-endpoint app-config)})

(defn schmitt-db [full-path]
  {:connection-uri
   (str "jdbc:ucanaccess://" full-path)})


(defn profile []
  (keyword (or (System/getenv "PROTOCOLS_ENV") "dev")))

