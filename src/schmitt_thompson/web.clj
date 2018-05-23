(ns schmitt-thompson.web)

(defn handler [request]
  {:status 200
   :headers {"Content-type" "text/plain"}
   :body "Hello World!"})
