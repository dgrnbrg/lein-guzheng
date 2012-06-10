(ns foo.core-test
  (:use clojure.test) 
  (:use foo.core))

(deftest a-test
  (is (= (foo.core/hello-world) "hello world"))
  (is (nil? (foo.core/branches true)))
  (is (= (foo.core/goodbye2) "goodbye")))
