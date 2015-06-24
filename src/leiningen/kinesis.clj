(ns leiningen.kinesis
  (:require [leiningen.core.main :as main]
            [me.raynes.conch :refer [programs with-programs let-programs] :as sh]))

(programs which)
(programs npm)
(programs kinesalite)

(defn- installed?
  "Returns true if `exe` is a program that exists on the path."
  [exe]
  (not (clojure.string/blank? (which exe {:throw false}))))

(defn- run-kinesalite
  "Run the kinesalite process as a background process."
  [port ssl]
  (kinesalite "--port" port "--ssl" ssl {:background true :verbose true}))

(defn- config-value
  "Get a value from project config or, optionally, use a default value."
  [project k & [default]]
  (get (project :memcached) k default))

(defn- prerequisites-missing?
  "node and npm need to be installed first."
  []
  (not (and (installed? "node")
            (installed? "npm"))))

(defn- install-kinesalite-if-necessary
  []
  (when (not (installed? "kinesalite"))
    (println "lein-kinesis: installing kinesalite")
    (npm "install" "-g" "kinesalite")))

(defn kinesis
  "Run kinesalite in memory."
  [project & args]
  (if (prerequisites-missing?)
    (println "lein-kinesis: cannot execute as node and npm are not installed. Please install them and try again!")
    (let [port (config-value project :port 8083)
          ssl (config-value project :ssl false)]
      (install-kinesalite-if-necessary)
      (println (str "lein-kinesis: starting in-memory kinesalite instance on port " port "."))
      (when ssl
        (println "lein-kinesis: ssl enabled."))
      (let [kinesis-server (run-kinesalite port ssl)]
        (if (seq args)
          (try
            (main/apply-task (first args) project (rest args))
            (finally (future-cancel kinesis-server)))
          (while true (Thread/sleep 5000)))))))
