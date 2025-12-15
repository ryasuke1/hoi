(ns task-c3.core)

(def nums (iterate inc 1))
(defn slow [a] (do
                 ;(println "start check" a)
                 (Thread/sleep 10)
                 ;(println "finish check" a)
                 (= (mod a 2) 0)))
(defn p-filter
  ([pred coll]
   (let [n 13
         parts (map doall (partition-all 50 coll))
         f #(doall (filter pred %))
         rets (map #(future (f %)) parts)

         step (fn step [[x & xs :as vs] fs]
                (if-let [s (seq fs)]
                  (lazy-seq
                    (lazy-cat (deref x) (step xs (rest s))))
                  (apply concat  (map deref vs))
                  ))]
     (step rets (drop n rets)))))



(defn -main []
  (do
    (println (doall (take 10 (p-filter slow (range 1 60)))))
    (println(time (doall (take 1000 (p-filter slow nums)))))
    (println(time (doall (take 1000 (filter slow nums)))))
    )
  )