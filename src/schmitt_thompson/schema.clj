(ns schmitt-thompson.schema)

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
            

(defn select-list [fields]
  (clojure.string/join ", " (map #(name %) fields)))

(defn select-statement [schema]
  (str
   "select "
   (select-list (:fields schema))
   " from "
   (:table schema)))

     
