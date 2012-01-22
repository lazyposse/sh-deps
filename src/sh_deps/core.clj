(ns ^{:doc "Shell dependency graph"}
  sh-deps.core
  (:use [midje.sweet]
        clojure.repl
        clojure.java.javadoc
        [clojure.pprint :only [pprint]]
        [clojure.tools.cli]) 
  (:require [clojure.string :as s]
            [clojure.java.shell :as sh]))

;; sh-deps --------------------------------------------------------------------------------

(prn "---------- begin ------------")

(unfinished)

(defn find-all "Find all the files from the directory d"
  [d] (s/split (:out (sh/sh "find" d)) #"\n"))

(fact "find-all"
  (> (count (find-all "/tmp")) 1) => true)

(defn find-path "Find only the script from the directory d"
  [d] (filter #(= (seq ".sh") (take-last 3 %)) (find-all d)))

(fact "find-path"
      (find-path :dir) => ["b.sh"]
      (provided
       (find-all :dir) => ["a.txt" "b.sh" "c.txt"]))

(defn path-to-node "basename"
  [p] (last (s/split p #"/")))

(fact "path-to-node"
      (path-to-node "/tmp/bash.sh") => "bash.sh")

(defn read-file "Read a file"
  [f] (slurp f))

(defn children "Given a list of nodes and a script p, returns the list of nodes that belongs to p"
  [p all-nodes] (let [rf (read-file p)] (filter #(.contains rf %) all-nodes)))

(fact "children"
      (children :path ["node1" "node2"]) => ["node2"]
      (provided
       (read-file :path) => " node2 "))

(defn graph-read "Given a directory d, return the corresponding graph"
  [d] (let [p (find-path d), k (map path-to-node p)]
        (zipmap k
                (map #(set (children % k)) p))))

(fact "graph-read"
      (graph-read :d) => {:node1 #{:node2}, :node2 #{:node1}}
      (provided
       (find-path :d) => [:path1 :path2]
       (path-to-node :path1) => :node1
       (path-to-node :path2) => :node2
       (children :path1 [:node1 :node2]) => [:node2]
       (children :path2 [:node1 :node2]) => [:node1]))

(defn write-lines
  [fname lines] (spit fname (s/join "\n" lines)))

(fact "write-lines"
      (let [fname "/tmp/tst.txt"]
        (write-lines fname [1 2]) => nil
        (slurp fname) => "1\n2"))

(defn graph-lines "Given a graph, create the graphviz dot lines in a seq. Add the nodes then the relations between them."
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

(defn graph-write "Given a graph and a filename, write the content of the graph into the filename f with a dot format."
  [g f] (write-lines f (graph-lines g)))

(fact "graph-write"
      (graph-write :g :filename) => nil
      (provided
       (graph-lines :g) => :lines
       (write-lines :filename :lines) => nil))

(defn graph "Output a dot file f representing the relationships between the scripts in the given directory d."
  [d f] (graph-write (graph-read d) f))

(fact "graph"
      (graph :dir :filename) => nil
      (provided
       (graph-read :dir) => :graph
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

(prn "---------- end ------------")
