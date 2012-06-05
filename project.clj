(defproject denlab-4clojure-exercices/denlab-4clojure-exercices "1.0.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clj-stacktrace "0.2.4"]
                 [org.clojure/tools.cli "0.2.1"]]
  :profiles {:dev
             {:dependencies
              [[midje "1.4.0"]
               [com.intelie/lazytest
                "1.0.0-SNAPSHOT"
                :exclusions
                [swank-clojure]]]}}
  :main sh-deps.core
  :min-lein-version "2.0.0"
  :description "Shell script dependency graph analyzer")
