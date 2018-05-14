(ns schmitt-thompson.schema
  (:require [clojure.data.codec.base64
             :as base64
             :refer [encode]]))

(defn select-list [fields]
  (clojure.string/join ", " (map #(name %) fields)))

(defn default-query [schema]
  (str
   "select distinct "
   (select-list (:fields schema))
   " from "
   (:table schema)))

(def protocol
  (let [pk-fn (fn [key _] (str "PR:" key))]
    {:attrs [:year :type]
     :pk pk-fn
     :sk pk-fn
     :data pk-fn}))

(def algorithm
  (let [pk-fn (fn [key, row]
                (str "AL:" key ":" (:algorithmid row)))]
    {:table "Algorithm"
     :fields [:algorithmid :title]
     :attrs [:title]
     :pk pk-fn
     :sk (:pk protocol)
     :data pk-fn}))


(def search-word
  (let [pk-fn (fn [key row]
                (str
                 "SW:" key ":"
                 (String. (encode (.getBytes (:searchword row))))))]
    {:table "SearchWord"
     :fields [:searchword ]
     :attrs [:searchword]
     :pk pk-fn
     :sk (:pk protocol)
     :data pk-fn}))

     
