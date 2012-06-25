(ns leiningen.guzheng
  (:use bultitude.core)
  (:use clojure.pprint)
  (:require leiningen.test)
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
  guzheng data collector and result displayer.
  This uses a shutdown hook in the JVM in order to be
  portable-ish between lein1 and lein2. This may
  cause problems if there is tons of missing coverage."
  [form nses lein2?]
  (let [libspecs-sym (gensym "libspecs")
        nses (map str nses)
        form
    `(do
       (-> (java.lang.Runtime/getRuntime)
         (.addShutdownHook (java.lang.Thread. guzheng.core/report-missing-coverage)))
       (defn require-instrumented#
         [f# & ~libspecs-sym]
         (let [loaded-ref# @#'clojure.core/*loaded-libs*
               loaded# (map str @loaded-ref#)]
           (doseq [ns# (vector ~@nses)]
             (when-not (some #{ns#} loaded#)
               (dosync (alter loaded-ref# conj (symbol ns#)))
               (guzheng.core/instrument-nses
                 guzheng.core/trace-if-branches
                 (vector ns#)))))
         (apply f# ~libspecs-sym))
       ~(when lein2?
          '(require 'robert.hooke))
       (~(if lein2?
           'robert.hooke/add-hook
           'leiningen.util.injected/add-hook) #'require #'require-instrumented#)
       ~form)]
    form))

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
  "Takes an init form and adds guzheng to it."
  [form nses]
  `(do
     ~form
     (require 'guzheng.core)))

(def ^:dynamic *instrumented-nses*)

(defn instrument-eip-1
  "Calls eval in project w/ instrumentation."
  [f project form x y init]
  (if-not (or x y)
    (f project
       (instrument-form form *instrumented-nses* false)
       nil nil
       (instrument-init init *instrumented-nses*))
    (f project form x y init)))

(defn instrument-eip-2
  "Calls eval in project w/ instrumentation."
  [f project form init]
  (f project
     (instrument-form form *instrumented-nses* true)
     (instrument-init init *instrumented-nses*)))

(defn guzheng
  "Takes a list of namespaces followed by -- and
  another leiningen task and executes that task
  with the given namespaces instrumented."
  [project & args]
  (let [project (-> project
                  (update-in [:dependencies] conj ['guzheng/guzheng "1.1.3"]))
        [nses [_ subtask & sub-args]] (split-ns-subtask args)
        [eip two?] (lein-probe)
        apply-task (if two?
                     (resolve 'leiningen.core.main/apply-task) 
                     (resolve 'leiningen.core/apply-task))
        ;must add a dependency on robert.hooke for lein2
        ;TODO: use injected hook in lein2 as well
        project (-> project
                  (update-in [:dependencies] conj ['robert/hooke "1.1.3"]))

        ]
    ;Instrument the correct eval-in-project fn
    (add-hook eip (if two?
                    #'instrument-eip-2
                    #'instrument-eip-1))
    (binding [*instrumented-nses* nses
              leiningen.test/*exit-after-tests* false]
      (apply apply-task subtask project sub-args
             (if two?
               [] ;lein1 has a 4 arg form, lein2 is 3 arg form
               [(resolve 'leiningen.core/task-not-found)])))))
