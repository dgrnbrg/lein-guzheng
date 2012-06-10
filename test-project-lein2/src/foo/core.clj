(ns foo.core)

(defn hello-world
  []
  "hello world")

(defn goodbye
  []
  (if true
    "lol"
    "bol")
  "goodbye")

(defn goodbye2
  []
  (if true
    "lol"
    "bol")
  "goodbye")

(defn branches
  [x]
  (if x
    (println "hello world")
    (println "never happens")))
