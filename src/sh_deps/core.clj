(ns ^{:doc "Shell dependency graph"}
  sh-deps.core
  (:use [midje.sweet]
        clojure.repl
        clojure.java.javadoc
        [clojure.pprint :only [pprint]]
        [clojure.tools.cli]) 
  (:require [clojure.string     :as s]
            [clojure.java.shell :as sh]
            [sh-deps.sh         :as sd]))

(defn- write-lines
  [fname lines] (spit fname (s/join "\n" lines)))

(fact "write-lines"
      (let [fname "/tmp/tst.txt"]
        (write-lines fname [1 2]) => nil
        (slurp fname) => "1\n2"))

(defn- graph-lines "Given a graph, create the graphviz dot lines in a seq. Add the nodes then the relations between them."
  [g]
  (let [ks (keys g)]
    (concat
     ["digraph fn {"]
     (map #(str "\"" % "\";") ks)
     (mapcat
      (fn [k] (map #(str "\"" k "\" -> \"" % "\";") (g k)))
      ks)
     ["}"])))

(fact
  (graph-lines {"node1" #{"node2"}
                "node2" #{"node1" "node3"}
                "node3" #{}}) => (just
                                  ["digraph fn {"
                                   "\"node1\";"
                                   "\"node2\";"
                                   "\"node3\";"
                                   "\"node1\" -> \"node2\";"
                                   "\"node2\" -> \"node1\";"
                                   "\"node2\" -> \"node3\";"
                                   "}"] :in-any-order))

(defn- graph-write "Given a graph and a filename, write the content of the graph into the filename f with a dot format."
  [g f] (write-lines f (graph-lines g)))

(fact "graph-write"
      (graph-write :g :filename) => nil
      (provided
       (graph-lines :g) => :lines
       (write-lines :filename :lines) => nil))

(defn- graph "Output a dot file f representing the relationships between the scripts in the given directory d."
  [d f] (graph-write (sd/graph-read {:type :sh-deps.sh/sh, :dir d}) f))

(fact "graph"
      (graph :dir :filename) => nil
      (provided
       (sd/graph-read {:type :sh-deps.sh/sh, :dir :dir}) => :graph
       (graph-write :graph :filename) => nil))

(defn -main [& args]
  (let [[options args banner :as opts]
        (cli args
             ["-h" "--help"       "Show help" :default false :flag true]
             ["-d" "--directory"  "Directory to analyze"]
             ["-g" "--graph-file" "Graph dot file to generate" ])]

    (when (options :help)
      (println banner)
      (System/exit 0))

    (println "Scanning directory" (options :directory) "and generating graph dot file" (options :graph-file) ".")
    ;; generates the import files
    (graph (options :directory) (options :graph-file))))

