(ns ^{:doc "Shell dependency graph/home/tony/repositories/pro/wikeo/wikeo-parent/wikeo-deployer/src/main/resource//universal-kp"}
  sh-deps.core
  (:use [midje.sweet])
  (:use clojure.repl)
  (:use clojure.java.javadoc)
  (:require [clojure.string :as s])
  (:require [clojure.java.shell :as sh]))
;; sh-deps --------------------------------------------------------------------------------

(prn "---------- begin ------------")

(unfinished graph-lines )

(defn find-all
  [d] (sh/sh "find" d))

(fact "find-all"
      (> (count (:out (find-all "/tmp")))
         1) => true)

(defn find-path
  [d] (filter #(= (seq ".sh") (take-last 3 %)) (find-all d)))

(fact "find-path"
      (find-path :dir) => ["b.sh"]
      (provided
       (find-all :dir) => ["a.txt" "b.sh" "c.txt"]))

(defn path-to-node
  [p] (last (s/split p #"/")))

(fact "path-to-node"
      (path-to-node "/tmp/bash.sh") => "bash.sh")

(defn read-file
  [f] (slurp f))

(defn children
  [p all-nodes] (filter #(.contains (read-file p) %)
                        all-nodes))

(fact "children"
      (children :path ["node1" "node2"]) => ["node2"]
      (provided
       (read-file :path) => " node2 "))

(defn graph-read "Given a dir name return the corresponding graph"
  [dir] (let [p (find-path :dir), k (map path-to-node p)]
          (zipmap k
                  (map #(set (children % k)) p))))

(fact "graph-read"
      (graph-read :dir) => {:node1 #{:node2}, :node2 #{:node1}}
      (provided
       (find-path :dir) => [:path1 :path2]
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
