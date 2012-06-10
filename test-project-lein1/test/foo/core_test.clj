(ns foo.core-test
  (:use clojure.test
        foo.core))

(deftest a-test
  (is (= (hello-world) "hello world"))
  (is (nil? (branches true)))
  (is (= (goodbye2) "goodbye")))
