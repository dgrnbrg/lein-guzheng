(ns lein-guzheng.test.core
  (:use [clojure.java.shell :only [sh]])
  (:use [clojure test pprint]))

(def expected-output
  "in ns foo.core: false branch is not covered in \"if\" on line 23\nin ns foo.core: false branch is not covered in \"if\" on line 16\nin ns foo.core: goodbye is not covered in \"defn\" on line 7\n") 

(deftest test-lein1
  (let [{:keys [err out exit]}
        (sh "lein" "clean," "deps," "version" :dir "test-project-lein1")]
    (println out)
    (is (re-find #"Leiningen 1\." out))
    (is (= 0 exit))) 
  (let [{:keys [err out exit]}
        (sh "lein" "guzheng" "foo.core" "--" "test" :dir "test-project-lein1")]
    (println out)
    (is (= 0 exit))
    (is (.endsWith out expected-output))))

(deftest test-lein2
  (let [{:keys [err out exit]}
        (sh "lein2" "clean," "deps," "version" :dir "test-project-lein2")]
    (println out)
    (is (re-find #"Leiningen 2\." out))
    (is (= 0 exit))) 
  (let [{:keys [err out exit ] :as lein2-output}
        (sh "lein2" "guzheng" "foo.core" "--" "test" :dir "test-project-lein2")]
    (pprint ["lein2 output:" lein2-output])
    (flush)
    (is (= 0 exit))
    (is (.endsWith out expected-output))))
