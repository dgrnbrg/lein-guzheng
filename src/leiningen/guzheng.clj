(ns leiningen.guzheng
  (:use [clojure.pprint]))

(defn eval-in-project
  "Support eval-in-project in both Leiningen 1.x and 2.x."
  [project form init]
  (let [[eip two?] (or (try (require 'leiningen.core.eval)
                         [(resolve 'leiningen.core.eval/eval-in-project)
                          true]
                         (catch java.io.FileNotFoundException _))
                       (try (require 'leiningen.compile)
                         [(resolve 'leiningen.compile/eval-in-project)]
                         (catch java.io.FileNotFoundException _)))]
    (if two?
      (eip project form init)
      (eip project form nil nil init))))

(defn guzheng
  "I don't do a lot."
  [project & args]
  (let [project (-> project
                  (update-in [:dependencies] conj ['guzheng/guzheng "1.0.0"]))]
    (pprint project)
    (eval-in-project project
                     `(do
                        (println "lol")
                        (clojure.pprint/pprint "hi")
                        ;(require 'guzheng.core)
                        );@guzheng.core/main-trace-atom)
                     `(require 'guzheng.core)
                     ;`(require 'clojure.pprint)
                     )))
