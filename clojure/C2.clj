(ns C2)

(defn primes []
  (letfn [(step [q composites]
            (lazy-seq
              (if-let [factors (composites q)]
                (let [next-composites
                      (reduce
                        (fn [m p]
                          (update m (+ q (* 2 p)) (fnil conj []) p))
                        (dissoc composites q)
                        factors)]
                  (step (+ q 2) next-composites))
                (cons q
                      (step (+ q 2)
                            (assoc composites (* q q) [q]))))))]
    (cons 2 (step 3 {}))))

(defn -main [& _]
  (let [p (primes)]
  (time (nth p 10000))
    (time (nth p 10001))))
