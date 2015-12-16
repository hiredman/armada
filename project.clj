(defproject com.manigfeald/armada "0.1.0"
  :description "clojure gossip based group membership"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.2.374"]]
  :profiles {:dev {:dependencies [[ring/ring-jetty-adapter "1.4.0"]
                                  [clj-http "2.0.0"]]}})
