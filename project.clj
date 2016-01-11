(defproject lein-kinesis "0.1.6"
  :description "lein plugin that wraps 'kinesalite' a local implementation of Amazon Kinesis."
  :url "http://github.com/mdaley/lein-kinesis"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[me.raynes/conch "0.8.0"]]
  :scm {:name "git"
        :url "https://github.com/mdaley/lein-kinesis"}
  :eval-in-leiningen true
  :repositories [["releases" {:url "https://clojars.org/repo"
                              :creds :gpg}]]

  :kinesis {:port 8083})
