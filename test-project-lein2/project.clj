(defproject foo/foo "0.1.0-SNAPSHOT" 
  :license {:name "Eclipse Public License",
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.3.0"]]
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :plugins [[lein-guzheng ~(nth (read-string (slurp "../project.clj")) 2) ]]
  :description "FIXME: write description")
