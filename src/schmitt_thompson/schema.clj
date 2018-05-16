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
     :data pk-fn
     :import-sql #(default-query protocol)}))

(def algorithm
  (let [pk-fn (fn [key, row]
                (str "AL:" key ":" (:algorithmid row)))]
    {:table "Algorithm"
     :fields [:algorithmid :title]
     :attrs [:title]
     :pk pk-fn
     :sk (:pk protocol)
     :data pk-fn
     :import-sql #(default-query algorithm)}))


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
     :data pk-fn
     :import-sql #(default-query search-word)}))

(def algorithm-searchwords
  {:table "AlgorithmSearchWords"
   :fields [:algorithmid :searchword :title]
   :attrs [:title :searchword]
   :pk (:pk search-word)
   :sk (:pk algorithm)
   :data (:pk search-word)
   :import-sql #(str "select asw.AlgorithmID, asw.SearchWord, a.Title "
                    "from AlgorithmSearchWords asw "
                    "inner join Algorithm a on asw.AlgorithmID = a.AlgorithmID")})

;;Use for Express only
(def advice
  (let [pk-fn (fn [key row]
                (str "ADV: " key ":"
                     (:adviceid row)))]
    {:table "Advice"
     :fields [:adviceid :algorithmid :advice]
     :attrs [:advice]
     :pk pk-fn
     :sk (:pk algorithm)
     :data pk-fn
     :import-sql #(default-query advice)}))

(def question
  (let [pk-fn (fn [key row]
                (str "QU:" key ":"
                     (:questionid row)))]
    {:table "Question"
     :fields [:questionid :algorithmid :questionorder :question]
     :attrs [:questionorder :question]
     :pk pk-fn
     :sk (:pk algorithm)
     :data pk-fn
     :import-sql #(default-query question)}))

;;Use for After-Hours only
(def advice-question
  {:table "QuestionAdvice"
   :fields [:questionid :adviceid :questionadviceorder :advice]
   :attrs [:questionadviceorder :advice]
   :pk (:pk advice)
   :sk (:pk question)
   :data (:pk advice)
   :import-sql #(str "select qa.questionid, qa.adviceid, "
                     "qa.questionadviceorder, a.advice "
                     "from QuestionAdvice qa "
                     "inner join Advice a on qa.AdviceID = a.AdviceID")})




     
