(ns bowling)

(def initial
  {:score 0
   :pins 10
   :throws 0
   :frames 0
   :score-multiplier (repeat 21 1)
   :extend-frame false})

(defn score-roll [state pins]
  "Score roll taking into account score multiplier"
  (-> state
      (update :score + (* pins (-> state :score-multiplier first)))
      (update :score-multiplier (fn [lst] (drop 1 lst)))))

(defn extend-frame-if-last [state]
  "Set boolean to extend frame if it is the last one"
  (if (== (:frames state) 9)
    (-> state
        (assoc :extend-frame true)
        (update :score-multiplier #(map dec %)))
    state))

(defn all-pins-down? [state]
  "Check if all pins are down"
  (zero? (:pins state)))

(defn all-pins-down-after-n-throws? [state n]
  "Check if all pins are down and N balls where thrown in this frame"
  (and (all-pins-down? state) (== (:throws state) n)))

(defn increment-n-elements [lst n]
  "Increment first N elements of the list LST"
  (if (zero? n)
    lst
    (cons (inc (first lst)) (increment-n-elements (rest lst) (dec n)))))

(defn score-spare [state]
  "Update scoring multipliers for a spare (knocking over 10 pins with two throws)"
  (if (all-pins-down-after-n-throws? state 2)
    (-> state
        (update :score-multiplier #(increment-n-elements % 1))
        extend-frame-if-last)
    state))

(defn score-strike [state]
  "Update scoring multipliers for a strike (knocking over 10 pins with one throw)"
  (if (all-pins-down-after-n-throws? state 1)
    (-> state
        (update :score-multiplier #(increment-n-elements % 2))
        extend-frame-if-last)
    state))

(defn is-frame-end? [state]
  "Check if current frame is finished"
  (if (:extend-frame state)
    (>= (:throws state) 3)
    (or (>= (:throws state) 2) (all-pins-down? state))))

(defn handle-frame-end [state]
  "Detect end of frame and begin new frame"
  (if (is-frame-end? state)
    (-> state
        (assoc :pins 10)
        (assoc :throws 0)
        (update :frames inc))
    state))

(defn ball [state pins]
  "Perform STATE changes for throwing a ball knocking over number of PINS"
  (-> state
      (update :pins - pins)
      (update :throws inc)
      (score-roll pins)
      score-spare
      score-strike
      handle-frame-end))

(defn finished? [state]
  (>= (:frames state) 10))
