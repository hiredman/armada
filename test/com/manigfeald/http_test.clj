(ns com.manigfeald.http-test
  (:require [clojure.test :refer :all]
            [com.manigfeald.armada :refer :all]
            [clojure.core.async :as async]
            [ring.adapter.jetty :refer [run-jetty]]
            [clj-http.client :as http]))

(defrecord Node [armada jetty port pinger])

(defn node [port bootstrap]
  (let [arm (run
              (format "http://localhost:%s" port)
              bootstrap
              (async/chan)
              (async/chan)
              (async/chan)
              (async/chan)
              500)
        j (run-jetty
           (fn [req]
             (let [b (read-string (slurp (:body req)))]
               (async/>!! (:in-pings arm) b))
             {:status 200
              :body ""})
           {:join? false
            :port port
            :host "localhost"})
        pinger (future
                 (while true
                   (when-let [ping (async/<!! (:out-pings arm))]
                     (try
                       (http/put (:to ping) {:body (pr-str ping)})
                       (catch Throwable t
                         (async/>!! (:ping-fails arm) (:to ping)))))))]
    (->Node arm j port pinger)))

(deftest a-test
  (let [start (System/currentTimeMillis)
        nodes (reduce
               (fn [nodes offset]
                 (conj nodes
                       (node (+ 9000 offset)
                             (when-let [node (first nodes)]
                               [(:id (:armada node))]))))
               []
               (range 10))]
    (while (apply not= 9 (for [{:keys [armada]} nodes]
                           (count (members @(:roster armada))))))
    (doseq [{:keys [armada]} nodes]
      (is  (= 9 (count (members @(:roster armada))))))))
