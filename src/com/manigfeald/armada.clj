(ns com.manigfeald.armada
  (:require [clojure.core.async :as async]))

(defprotocol Roster
  (members [roster])
  (fail [roster member])
  (add [roster member])
  (sample-good [roster])
  (sample-any [roster]))

(defrecord ARoster [threshold hosts]
  Roster
  (members [roster]
    (when (seq hosts)
      (vec (keys hosts))))
  (fail [roster member]
    (->ARoster
     threshold
     (into {}
           (for [[host fails] hosts
                 :let [fails (if (= host member)
                               (inc fails)
                               fails)]
                 :when (> threshold fails)]
             [host fails]))))
  (add [roster member]
    (->ARoster threshold (assoc hosts member (get hosts member 0))))
  (sample-good [roster]
    (when (seq hosts)
      (when-let [s (seq (for [[host fails] hosts
                              :when (not (pos? fails))]
                          host))]
        (rand-nth s))))
  (sample-any [roster]
    (when (seq hosts)
      (rand-nth (keys hosts)))))

(defrecord Ping [from to other])

(defn run [me bootstrap in-pings out-pings ping-fails abort ping-timeout]
  (let [a (atom (->ARoster 3 {}))]
    (async/go
      (doseq [host bootstrap]
        (async/>! out-pings (->Ping me host nil))))
    (async/go-loop []
      (async/alt!
        abort ([_])
        (async/timeout ping-timeout) ([_]
                                        (when-let [h (sample-any @a)]
                                          (async/>! out-pings (->Ping me h (sample-good @a))))
                                        (recur))))
    (async/go-loop []
      (async/alt!
        abort ([_])
        in-pings ([ping]
                    (swap! a add (:from ping))
                    (when (and (:other ping)
                               (not= (:other ping) me))
                      (swap! a add (:other ping)))
                    (recur))
        ping-fails ([host]
                      (swap! a fail host)
                      (recur))))
    {:id me
     :roster a
     :in-pings in-pings
     :out-pings out-pings
     :ping-fails ping-fails
     :abort abort}))
