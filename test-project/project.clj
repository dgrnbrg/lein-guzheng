(defproject test-project "1.0.0-SNAPSHOT" 
  :dependencies [[org.clojure/clojure "1.3.0"]]

  ;; Lein 1
  :dev-dependencies [[lein-guzheng ~(nth (read-string (slurp "../project.clj")) 2)]] 

  ;; Lein 2
  :plugins [[lein-guzheng ~(nth (read-string (slurp "../project.clj")) 2) ]
            [lein-midje "2.0.0-SNAPSHOT"]]) 
