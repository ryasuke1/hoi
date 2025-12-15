(ns task-c5.core)

(def n_philosophers 6)
(def n_forks n_philosophers)
(def t_think 10)
(def t_eat 9)
(def n_periods 100)

(def transactions_starts (atom 0))
(def transactions_finishes (atom 0))

(def forks (doall (map (fn [a] (ref 0)) (range n_forks))))

(defn philosopher [left_fork, right_fork, i]
  (dorun n_periods
         (repeatedly
           (fn []
             (do
               (print (format "Philosopher %d thinking\n" i))
               (Thread/sleep t_think)
               (print (format "Philosopher %d trying to eat\n" i))
               (dosync
                 (print (format "Philosopher %d (re)started transaction\n" i))
                 (swap! transactions_starts inc)
                 (Thread/sleep t_eat)
                 (alter left_fork inc)
                 (alter right_fork inc))
               (swap! transactions_finishes inc)
               (print (format "Philosopher %d finished transaction\n" i)))))))

(def threads
  (doall
    (map
      (fn [i]
        (new Thread
             (fn []
               (philosopher (nth forks i) (nth forks (mod (inc i) n_forks)) i))
             ))
      (range n_philosophers))
    ))


;(println threads)
(doall (map (memfn start) threads))
(doall (map (memfn join) threads))

(println "Transaction starts" transactions_starts)
(println "Transaction finishes" transactions_finishes)
(println "Transaction restarts" (- @transactions_starts @transactions_finishes))
(println forks)
