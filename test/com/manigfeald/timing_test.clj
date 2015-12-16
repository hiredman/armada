(ns com.manigfeald.timing-test
  (:require [clojure.test :refer :all]
            [com.manigfeald.armada :refer :all]
            [clojure.core.async :as async]
            [clojure.java.io :as io])
  (:import (java.util.concurrent LinkedBlockingQueue)
           (java.util UUID)
           (java.io File)))

(defn node [bs]
  (run (UUID/randomUUID) bs
    (async/chan)
    (async/chan)
    (async/chan)
    (async/chan)
    10))

(defn data [n]
  (let [nodes (reduce
               (fn [nodes _]
                 (conj nodes
                       (node
                        (when-let [node (first nodes)]
                          [(:id node)]))))
               []
               (range n))
        indexed-nodes (into {} (map (juxt :id identity) nodes))
        a (atom true)
        network (async/go-loop []
                  (let [[{:keys [from to] :as ping} chan] (async/alts!
                                                           (map :out-pings nodes))]
                    (async/>!! (:in-pings (get indexed-nodes to))
                               ping))
                  (when @a (recur)))
        start (System/currentTimeMillis)]
    (while (apply not= (dec (count nodes))
                  (for [{:keys [roster]} nodes]
                    (count (members @roster)))))
    (reset! a false)
    (doseq [node nodes]
      (async/close! (:abort node)))
    [n (- (System/currentTimeMillis) start)]))

(defn plot [plot-commands]
  (let [sql (.start
             (ProcessBuilder.
              ["gnuplot"]))]
    #_(future
        (doseq [l (line-seq (io/reader (.getErrorStream sql)))]
          (println l)))
    (with-open [sql-stream (.getOutputStream sql)
                sql-writer (io/writer sql-stream)]
      (binding [*out* sql-writer]
        (println plot-commands))
      (.close sql-writer))
    (.waitFor sql)))

(deftest a-test
  (.delete (io/file "plot.png"))
  (let [tmp (File/createTempFile "sometemp" "sometemp")]
    (try
      (with-open [out (io/writer tmp)]
        (dotimes [_ 100]
          (.write out (with-out-str (apply println (data (inc (rand-int 100))))))
          (.flush out)))
      (plot
       (str "
set terminal pngcairo size 1024,768 enhanced font 'Verdana,8'
set output 'plot.png'

set xlabel 'nodes'
set ylabel 'milliseconds'
set title '" (java.util.Date.) "'

plot '" (.getAbsolutePath tmp) "' using 1:2"))
      (finally
        (.delete tmp)))))
