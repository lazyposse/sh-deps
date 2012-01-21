(ns ^{:doc "Shell dependency graph/home/tony/repositories/pro/wikeo/wikeo-parent/wikeo-deployer/src/main/resource//universal-kp"}
  sh-deps.core
  (:use [midje.sweet]
        clojure.repl
        clojure.java.javadoc
        [clojure.pprint :only [pprint]]) 
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

(defn graph-lines "Given a graph, create the graphviz dot lines in a seq."
  [g]
  (mapcat
   (fn [k] (map #(str k " -> " % ";") (g k)))
   (keys g)))

(fact
  (graph-lines {"node1" #{"node3" "node2"}
                "node2" #{"node1" "node3"}}) => (just
                                                 ["node1 -> node3;"
                                                  "node1 -> node2;"
                                                  "node2 -> node1;"
                                                  "node2 -> node3;"] :in-any-order))

(defn graph-write
  [g] (write-lines (graph-lines g)))

(fact "graph-write"
      (graph-write :g) => nil
      (provided
       (graph-lines :g) => :lines
       (write-lines :lines) => nil))

(defn graph "Output a dot file representing the relationships between the scripts in the given directory."
  [dir] (graph-write (graph-read dir)))

(fact "graph"
      (graph :dir) => nil
      (provided
       (graph-read :dir) => :graph
       (graph-write :graph) => nil))

(prn "---------- end ------------")
