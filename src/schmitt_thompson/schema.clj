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

(defn row-attr [attr]
  (fn [_ row]
    (attr row)))

(defn default-key-fn [type id-attr]
  (fn [key row]
    (str type ":" key ":" (id-attr row))))

(def protocol
  (let [pk-fn (fn [key _] (str "PR:" key))]
    {:attrs [:year :type]
     :pk pk-fn
     :sk pk-fn}))


(def search-word
  {:table "SearchWord"
   :fields [:searchword ]
   :attrs [:searchword]
   :pk (:pk protocol)
   :sk (default-key-fn "SW" #(String. (encode (.getBytes (:searchword %)))))
   :import-sql #(default-query search-word)})

(def algorithm
  {:table "AlgorithmSearchWords"
   :fields [:algorithmid :searchword :title]
   :attrs [:title]
   :pk (:sk search-word)
   :sk (default-key-fn "AL" :algorithmid)
   :import-sql #(str "select asw.AlgorithmID, asw.SearchWord, a.Title "
                     "from AlgorithmSearchWords asw "
                     "inner join Algorithm a on "
                     "asw.AlgorithmID = a.AlgorithmID")})

;;Use for Express only
(def advice
  {:table "Advice"
   :fields [:adviceid :algorithmid :advice]
   :attrs [:advice]
   :pk (:sk algorithm)
   :sk (default-key-fn "ADV" :adviceid)
   :import-sql #(default-query advice)})

(def question
  {:table "Question"
   :fields [
            :questionid :algorithmid :questionorder :question
            :maindisposition :dispositionheading
            ]
   :attrs [:question :questionorder :maindisposition :dispositionheading]
   :pk (:sk algorithm)
   :sk (default-key-fn "QU" :questionid)
   :import-sql #(str "select q.QuestionID, q.AlgorithmID, q.QuestionOrder, "
                     "q.Question, d.MainDisposition, d.DispositionHeading "
                     "from Question q inner join Disposition d on "
                     "q.DispositionLevel = d.LevelID")})

;;Use for After-Hours only
(def advice-question
  {:table "QuestionAdvice"
   :fields [:questionid :adviceid :questionadviceorder :advice]
   :attrs [:advice :questionadviceorder]
   :pk (:sk question)
   :sk (:sk advice)
   :import-sql #(str "select qa.questionid, qa.adviceid, "
                     "qa.questionadviceorder, a.advice "
                     "from QuestionAdvice qa "
                     "inner join Advice a on qa.AdviceID = a.AdviceID")})

