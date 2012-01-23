(ns ^{:doc "Specific implem for shell scripts"}
  sh-deps.sh 
  (:use [midje.sweet]
        clojure.repl
        clojure.java.javadoc
        [clojure.pprint :only [pprint]]
        [clojure.tools.cli]) 
  (:require [clojure.string :as s]
            [clojure.java.shell :as sh]))

(defn- find-all "Find all the files from the directory d"
  [d] (s/split (:out (sh/sh "find" d)) #"\n"))

(fact "find-all"
  (> (count (find-all "/tmp")) 1) => true)

(defn- read-file "Read a file"
  [f] (slurp f))

(defn- children "Given a list of nodes and a script p, returns the list of nodes that belongs to p"
  [p all-nodes] (let [rf (read-file p)] (filter #(.contains rf %) all-nodes)))

(fact "children"
      (children :path ["node1" "node2"]) => ["node2"]
      (provided
       (read-file :path) => " node2 "))

(defn- find-path "Find only the script from the directory d"
  [d] (filter #(= (seq ".sh") (take-last 3 %)) (find-all d)))

(fact "find-path"
      (find-path :dir) => ["b.sh"]
      (provided
       (find-all :dir) => ["a.txt" "b.sh" "c.txt"]))

(defn- path-to-node "basename"
  [p] (last (s/split p #"/")))

(fact "path-to-node"
      (path-to-node "/tmp/bash.sh") => "bash.sh")

(defmulti graph-read :type)

(defmethod graph-read ::sh [m]
  (let [p (find-path (:dir m)), k (map path-to-node p)]
    (zipmap k
            (map #(set (children % k)) p)))) 

(fact "graph-read::sh"
      (graph-read {:type ::sh, :dir :d}) => {:node1 #{:node2}, :node2 #{:node1}}
      (provided
       (find-path :d) => [:path1 :path2]
       (path-to-node :path1) => :node1
       (path-to-node :path2) => :node2
       (children :path1 [:node1 :node2]) => [:node2]
       (children :path2 [:node1 :node2]) => [:node1]))
