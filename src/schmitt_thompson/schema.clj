(ns schmitt-thompson.schema
  (:require [clojure.data.codec.base64
             :as base64
             :refer [encode]]))


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
  {:table "AlgorithmSearchWords"
   :fields [:searchword, :algorithmid ]
   :attrs [:searchword]
   :pk (fn [key row]
         (str
          "SW:" key ":"
          (String. (encode (.getBytes (:searchword row))))))
   :sk (:pk protocol)
   :data (fn [key row]
           (str
            ((:pk search-word) key row)
            "#"
            ((:pk algorithm) key row)))})

(defn select-list [fields]
  (clojure.string/join ", " (map #(name %) fields)))

(defn select-statement [schema]
  (str
   "select "
   (select-list (:fields schema))
   " from "
   (:table schema)))

     
