(ns com.manigfeald.armada-test
  (:require [clojure.test :refer :all]
            [com.manigfeald.armada :refer :all]
            [clojure.core.async :as async])
  (:import (java.util.concurrent LinkedBlockingQueue)))

(deftest a-test
  (let [to 100
        m {1 (run 1 []
               (async/chan)
               (async/chan)
               (async/chan)
               (async/chan)
               to)
           2 (run 2 [1]
               (async/chan)
               (async/chan)
               (async/chan)
               (async/chan)
               to)
           3 (run 3 [1]
               (async/chan)
               (async/chan)
               (async/chan)
               (async/chan)
               to)
           4 (run 4 [2]
               (async/chan)
               (async/chan)
               (async/chan)
               (async/chan)
               to)}
        network (LinkedBlockingQueue.)
        failed-from (atom #{})
        failed-to (atom #{})]
    (doseq [[id n] m]
      (async/go-loop []
        (when-let [ping (async/<! (:out-pings n))]
          (.put network ping))
        (recur)))
    (future
      (while true
        (let [{:keys [from to] :as ping} (.take network)]
          (if (and (not (contains? @failed-from from))
                   (not (contains? @failed-to to)))
            (async/>!! (get-in m [to :in-pings]) ping)
            (async/>!! (get-in m [from :ping-fails]) to)))))
    (Thread/sleep 2000)
    (is (= {1 #{2 3 4}
            2 #{1 3 4}
            3 #{1 2 4}
            4 #{1 2 3}}
           (into {}
                 (for [[id {:keys [roster]}] m]
                   [id (set (members @roster))]))))
    (swap! failed-from conj 4)
    (swap! failed-to conj 4)
    (Thread/sleep 5000)
    (is (= {1 #{2 3}
            2 #{1 3}
            3 #{1 2}
            4 #{}}
           (into {}
                 (for [[id {:keys [roster]}] m]
                   [id (set (members @roster))]))))))
