(ns leiningen.guzheng
  (:use [guzheng.core]))

(defn guzheng
  "I don't do a lot."
  [project & args]
  (println @main-trace-atom))
