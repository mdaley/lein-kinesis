(ns leiningen.kinesis
  (:require [leiningen.core.main :as main]
            [me.raynes.conch :refer [programs]]
            [me.raynes.conch.low-level :refer [proc stream-to-out]]))

(programs which)
(programs npm)

(defn- installed?
  "Returns true if `exe` is a program that exists on the path."
  [exe]
  (not (clojure.string/blank? (which exe {:throw false}))))

(defn- config-value
  "Get a value from project config or, optionally, use a default value."
  [project k & [default]]
  (get (project :kinesis) k default))

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

(defn- run-kinesis
  "Run the kinesis server"
  [port]
  (let [p (proc "kinesalite" "--port" (str port) :verbose :very)]
    (future (stream-to-out p :out))
    (future (stream-to-out p :err))
    (while true (Thread/sleep 5000))))

(defn kinesis
  "Run kinesalite in memory."
  [project & args]
  (if (prerequisites-missing?)
    (println "lein-kinesis: cannot execute as node and npm are not installed. Please install them and try again!")
    (let [port (config-value project :port 8083)]
      (install-kinesalite-if-necessary)
      (println (str "lein-kinesis: starting in-memory kinesalite instance on port " port "."))
      (let [server (future (run-kinesis port))]
        (.addShutdownHook (Runtime/getRuntime) (Thread. #(future-cancel server)))
        (if (seq args)
          (try
            (main/apply-task (first args) project (rest args))
            (finally (future-cancel server)))
          (while true (Thread/sleep 5000)))))))
