(ns leiningen.guzheng
  (:use bultitude.core)
  (:use clojure.pprint)
  (:use robert.hooke))

(defn split-ns-subtask
  "Takes the namespaces followed by \"--\" followed
  by the leiningen command to run with an instrumented
  eval-in-project."
  [args]
  (let [[nses subtask] (split-with #(not= "--" %) args)]
    [(map symbol nses) subtask]))

(defn instrument-form
  "Takes the form to be wrapped with the
  guzheng data collector and result displayer."
  [form nses]
  (println "instrumenting form")
  (let [x  `(do
     (println "Hello from inside the wall")
     (flush)
     ;(require ~'guzheng.core)
     (println "hi i got some guzheng")
     (flush)
     (guzheng.core/instrument-nses
       guzheng.core/trace-if-branches
       (vector ~@(map str nses))) 
     ~form
     (guzheng.core/report-missing-coverage))]
    ;(pprint ["form is " x])
    x))

(defn lein-probe
  "Returns eip and whether this is lein 1 or lein 2.
  If it's lein 2, the 2nd element of the returned vector
  will be true."
  []
  (or (try (require 'leiningen.core.eval)
        [(resolve 'leiningen.core.eval/eval-in-project)
         true]
        (catch java.io.FileNotFoundException _))
      (try (require 'leiningen.compile)
        [(resolve 'leiningen.compile/eval-in-project)]
        (catch java.io.FileNotFoundException _))) )

(defn instrument-init
  "Takes an init form and adds guzheng to it"
  [form nses]
  (println "instrumenting init")
  `(do ~form (require ~'guzheng.core))
  (let [x  `(apply require 'guzheng.core '~nses)]
    (require 'clojure.pprint)
    (clojure.pprint/pprint ["new-init is " x])
    (flush)
    x))

(def ^:dynamic *instrumented-nses*)

(defn instrument-eip-1
  "Calls eval in project w/ instrumentation."
  [f project form x y init]
  (if-not (or x y)
    (f project
       (instrument-form form *instrumented-nses*)
       nil nil
       (instrument-init init *instrumented-nses*))
    (f project form x y init)))

(defn instrument-eip-2
  "Calls eval in project w/ instrumentation."
  [f project form init]
  (f project
    (instrument-form form *instrumented-nses*)
     (instrument-init init *instrumented-nses*)))

(defn guzheng
  "I am the eggman."
  [project & args]
  (println "guzheng starts now")
  (let [project (-> project
                  (update-in [:dependencies] conj ['guzheng/guzheng "1.1.0"]))
        [nses [_ subtask & sub-args]] (split-ns-subtask args)
        [_ two?] (lein-probe)
        subtask-ns-sym (symbol (str "leiningen." subtask))]
    ;Instrument the correct eval-in-project fn
    (if two?
      (add-hook (ns-resolve 'leiningen.core.eval 'eval-in-project)
                #'instrument-eip-2)
      (add-hook (ns-resolve 'leiningen.compile 'eval-in-project)
                #'instrument-eip-1))
    (require subtask-ns-sym)
    (binding [*instrumented-nses* nses]
      (apply (ns-resolve subtask-ns-sym
                         (symbol subtask))
             project
             sub-args))))
