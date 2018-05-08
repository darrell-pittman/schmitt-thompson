(def project 'schmitt-thompson)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"resources" "src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "1.9.0"]
                            [adzerk/boot-test "RELEASE" :scope "test"]
                            [org.clojure/java.jdbc "0.7.6"]
                            [net.sf.ucanaccess/ucanaccess "4.0.4"]
                            [digest "1.4.8"]
                            [com.taoensso/faraday "1.9.0"]
                            [aero "1.1.3"]
                            [org.clojure/core.async "0.4.474"]])

(task-options!
 aot {:namespace   #{'schmitt-thompson.core}}
 pom {:project     project
      :version     version
      :description "FIXME: write description"
      :url         "http://example/FIXME"
      :scm         {:url "https://github.com/yourname/schmitt-thompson"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}}
 repl {:init-ns    'schmitt-thompson.core}
 jar {:main        'schmitt-thompson.core
      :file        (str "schmitt-thompson-" version "-standalone.jar")})

(deftask build
  "Build the project locally as a JAR."
  [d dir PATH #{str} "the set of directories to write to (target)."]
  (let [dir (if (seq dir) dir #{"target"})]
    (comp (aot) (pom) (uber) (jar) (target :dir dir))))

(deftask run
  "Run the project."
  [a args ARG [str] "the arguments for the application."]
  (with-pass-thru fs
    (require '[schmitt-thompson.core :as app])
    (apply (resolve 'app/-main) args)))

(require '[adzerk.boot-test :refer [test]])
