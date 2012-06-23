(ns foo.core-test
  (:use midje.sweet) 
  (:use foo.core))

(fact (foo.core/hello-world) => "hello world")
(fact (foo.core/branches true) => nil?)
(fact (foo.core/goodbye2) => "goodbye")
