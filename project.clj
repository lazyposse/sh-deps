(defproject denlab-4clojure-exercices "1.0.0-SNAPSHOT"
  :description "Shell script dependency graph analyzer"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [clj-stacktrace      "0.2.4"]
                 [org.clojure/tools.cli "0.2.1"]]
  :dev-dependencies [[midje "1.3.1"]
                     [com.intelie/lazytest "1.0.0-SNAPSHOT" :exclusions [swank-clojure]]]
  :main sh-deps.core)
