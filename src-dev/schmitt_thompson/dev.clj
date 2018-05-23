(ns schmitt-thompson.dev
  (:require [ring.adapter.jetty :refer [run-jetty]])
  (:require [ring.middleware.reload :refer [wrap-reload]])
  (:require [schmitt-thompson.web :as web]))

(def dev-handler
  (wrap-reload #'web/handler))

(defn run-dev-server [port]
  (run-jetty dev-handler {:port port}))

