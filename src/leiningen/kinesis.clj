(ns leiningen.kinesis
  (:require [leiningen.core.main :as main]
            [me.raynes.conch :refer [programs]]
            [me.raynes.conch.low-level :refer [proc stream-to-out exit-code]]))

(programs which)
(programs npm)
(programs mkdir)

(def home-dir (System/getProperty "user.home"))
(def kinesalite-dir (str home-dir "/.kinesalite/"))
(def kinesalite-exe (str kinesalite-dir "node_modules/kinesalite/cli.js"))

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
  (mkdir "-p" kinesalite-dir)
  (when (not (installed? kinesalite-exe))
    (println "lein-kinesis: installing kinesalite")
    (let [p (proc "npm" "install" "kinesalite" :dir kinesalite-dir)]
      (future (stream-to-out p :out))
      (future (stream-to-out p :err))
      (println "Waiting for installation to complete.")
      (exit-code p)
      (println "Installation completed."))))

(defn- run-kinesis
  "Run the kinesis server"
  [port]
  (let [p (proc kinesalite-exe "--port" (str port) :verbose :very)]
    (future (stream-to-out p :out))
    (future (stream-to-out p :err))
    (:process p)))

(defn kinesis
  "Run kinesalite in memory."
  [project & args]
  (if (prerequisites-missing?)
    (do (println "lein-kinesis: cannot execute as node and npm are not installed. Please install them and try again!")
        (println "lein-kinesis: if running on debian/ubuntu, which uses 'nodejs', please install node using: sudo apt-get install nodejs-legacy"))
    (let [port (config-value project :port 8083)]
      (install-kinesalite-if-necessary)
      (println (str "lein-kinesis: starting in-memory kinesalite instance on port " port "."))
      (let [server (run-kinesis port)]
        (.addShutdownHook (Runtime/getRuntime) (Thread. #(.destroy server)))
        (if (seq args)
          (try
            (main/apply-task (first args) project (rest args))
            (finally (.destroy server)))
          (while true (Thread/sleep 5000)))))))
