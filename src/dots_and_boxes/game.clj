(ns dots-and-boxes.game
  (:gen-class))


(defn get-blank-squere
  "vraca praznu pocetnu tablu za igru"
  [dimension]
  (vec (take 2 (repeat (vec (take (* dimension (- dimension 1)) (repeat false))))))
  )

(defn get-dimension
  "vraca dimenziju table za igru"
  [squere]
  (let [y (first (map count squere))]
    (int (/ (+ 1 (Math/sqrt (+ 1 (* 4 y)))) 2))))

(defn check-validity-of-squere
  "proverava validnost celokupne table za igru"
  [squere]
  (try (let [dimension (get-dimension squere)]
         (and
          (= (count squere) 2)
          (let [size (int (* dimension (- dimension 1)))]
            (and
             (= size (first (map count squere)))
             (= size (second (map count squere)))))))
       (catch Exception e
         (print e)
         false)))


(defn get-move-boxes
  "vraca kutije na koje dati potez ima efekat"
  [squere horistonal? position]
  (let [dimension (get-dimension squere)]
    (filter #(not (nil? %)) (if horistonal?
                              ;dole
                              [(let [box (if (>= position (* (- dimension 1) (- dimension 1)))
                                           nil
                                           (vector
                                            (get-in squere [0 position])
                                            (get-in squere [1 (+ 1 (+ position (int (/ position (- dimension 1)))))])
                                            (get-in squere [0 (- (+ position dimension) 1)])
                                            (get-in squere [1 (+ position (int (/ position (- dimension 1))))])))]
                                 (if (some nil? box) nil box))
                               ;gore
                               (let [box (if (< position (- dimension 1))
                                           nil
                                           (vector
                                            (get-in squere [0 position])
                                            (get-in squere [1 (+ (- position dimension) (int (/ position (- dimension 1))))])
                                            (get-in squere [0 (+ (- position dimension) 1)])
                                            (get-in squere [1 (+ (+ (- position dimension) (int (/ position (- dimension 1)))) 1)])))]
                                 (if (some nil? box) nil box))]
                              ;desno
                              [(let [box (if (= (rem position dimension) (- dimension 1))
                                           nil
                                           (vector
                                            (get-in squere [1 position])
                                            (get-in squere [0 (- position (int (/ position dimension)))])
                                            (get-in squere [1 (+ 1 position)])
                                            (get-in squere [0 (- (+ dimension (- position (int (/ position dimension)))) 1)])))]
                                 (if (some nil? box) nil box))
                               ;levo
                               (let [box (if (= (rem position dimension) 0)
                                           nil
                                           (vector
                                            (get-in squere [1 position])
                                            (get-in squere [0 (+ (- dimension 1) (- (- position 1) (int (/ position dimension))))])
                                            (get-in squere [1 (- position 1)])
                                            (get-in squere [0 (- (- position 1) (int (/ position dimension)))])))]
                                 (if (some nil? box) nil box))]))))

(defn check-box
  "provera kutije sa check-fn funkcijom"
  [box check-fn]
  (when (not (and box (= (count box) 4))) (throw (Exception. "invalid box")))
  (check-fn (count (filter true? box))))


(defn free-move?
  "da li potez slobodan (protivnik nece moci da zatvori ni jednu kutiju nakon poteza)"
  [squere horistonal? position]
  (println horistonal? position)
  (println (get-move-boxes squere horistonal? position))
  (if (get-in squere [(if horistonal? 0 1) position])
    false
    (every? (fn [box] (check-box box #(< % 2))) (get-move-boxes squere horistonal? position))))

(defn close-move?
  "da li potez zatvara kutiju"
  [squere horistonal? position]
  (if (get-in squere [(if horistonal? 0 1) position])
    false
    (some (fn [box] (check-box box #(= % 3))) (get-move-boxes squere horistonal? position))))

(defn close-box-position
  "koju kutiju zatvara potez"
  [squere horistonal? position]
  (if (get-in squere [(if horistonal? 0 1) position])
    -1
    (let [position (keep-indexed (fn [pos, box] (if (check-box box #(= % 3)) pos)) (get-move-boxes squere horistonal? position))]
    (if (empty? position) -1 (first position))
    )))

(defn unplayed?
  "da li potez vec odigran"
  [squere x y]
  (false? (get-in squere [x y])))


(defn all-free-moves
  "svi slobodni potezi"
  [squere]
  (filter #(not (nil? %)) (let [ver (vec (get squere 0)) hor (vec (get squere 1))]
                            (into [] (concat
                                      (loop [position 0 rest-of-ver ver free-moves []]
                                        (let [x (first rest-of-ver) move (free-move? squere true position)]
                                          (if (not (nil? x))
                                            (recur (inc position) (rest rest-of-ver) (if move (conj free-moves [0 position]) free-moves))
                                            (if (empty? free-moves) nil free-moves))))
                                      (loop [position 0 rest-of-hor hor free-moves []]
                                        (let [x (first rest-of-hor) move (free-move? squere false position)]
                                          (if (not (nil? x))
                                            (recur (inc position) (rest rest-of-hor) (if move (conj free-moves [1 position]) free-moves))
                                            (if (empty? free-moves) nil free-moves)))))))))

(defn first-close-move
  [squere]
  (let [ver (vec (get squere 0)) hor (vec (get squere 1))]
    (loop [position 0 rest-of-ver ver]
      (let [x (first rest-of-ver) move (close-move? squere true position)]
        (if move
          [0 position]
          (if (not (nil? x))
            (recur (inc position) (rest rest-of-ver))
            (loop [position 0 rest-of-hor hor]
              (let [x (first rest-of-hor) move (close-move? squere false position)]
                (if move
                  [1 position]
                  (if (not (nil? x))
                    (recur (inc position) (rest rest-of-hor))
                    nil))))))))))

(defn close-boxes
  "zatvara sve kutije koje je moguce zatvoriti"
  [squere]
  (loop [new-squere squere close-move (first-close-move squere) moves []]
    (if close-move
      (let [x (assoc-in new-squere close-move true)]
        (recur x (first-close-move x) (conj moves close-move)))
      {:moves moves :squere new-squere})))

(defn all-unplayed-moves
  "svi neodigrani potezi"
  [squere]
  (loop [i 0 moves []]
    (if (< i (count squere))
      (recur (inc i) (into moves (loop [j 0 moves []]
                                   (if (< j (count (get-in squere [0])))
                                     (recur (inc j) (if (unplayed? squere i j) (conj moves [i j]) moves))
                                     moves))))
      moves)))


(defn get-move
  "vraca potez"
  [squere move]
  (if move
    {:squere (assoc-in squere move true)
     :moves (vector move)}
    {:squere squere
     :moves []})
  )

(defn first-free-move
  "prvi slobodan potez"
  [squere]
  (let [move (first (all-free-moves squere))]
    (get-move squere move)))

(defn rand-free-move
  "nasumican slobodan potez"
  [squere]
  (let [move (try  (rand-nth (all-free-moves squere))
                   (catch Exception e nil))]
    (get-move squere move)))

(defn first-unplayed-move
  "prvi potez"
  [squere]
  (let [move (first (all-unplayed-moves squere))]
    (get-move squere move)))

(defn rand-unplayed-move
  "nasumican potez"
  [squere]
  (let [move (try (rand-nth (all-unplayed-moves squere))
                  (catch Exception e nil))]
    (get-move squere move)))

(defn best-unplayed-move
  "'najbolji' potez (protivnik ce moći da zatvori minimaln broj kutija pre sledećeg poteza)"
  [squere]
  (loop [all-moves (all-unplayed-moves squere) best-move {:move nil :box-counter 100}]
    (println all-moves)
    (if (empty? all-moves)
      (get-move squere (:move best-move))
      (recur (rest all-moves)
             (let [move (first all-moves)
                   box-counter (count (:moves (close-boxes (assoc-in squere move true))))]
               (println move box-counter)
               (if (< box-counter (:box-counter best-move))
                 (do
                   (println move box-counter)
                   {:move move
                    :box-counter box-counter})
                 best-move))))))

(defn next-move
  "odigrava naredni potez"
  [squere]
  (let [fp-result (close-boxes squere)
        sp-result (rand-free-move (:squere fp-result))]
    (if (empty? (:moves sp-result))
      (let [tp-result (best-unplayed-move (:squere sp-result))]
        {:moves (vec (concat (:moves fp-result) (:moves tp-result)))
         :squere (:squere tp-result)})
      {:moves (vec (concat (:moves fp-result) (:moves sp-result)))
       :squere (:squere sp-result)})))
