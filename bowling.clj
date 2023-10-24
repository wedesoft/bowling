(ns bowling
    (:require [midje.sweet :refer :all]))

(def initial
  {:score 0
   :pins 10
   :throws 0
   :frames 0
   :score-multiplier (repeat 1)
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

; ---------- tests ----------

(fact "Initial score is zero"
      (:score initial) => 0)

(fact "Knock over zero pins"
      (:score (ball initial 0)) => 0)

(fact "Knock over three pins"
      (:score (ball initial 3)) => 3)

(fact "Knock over three and then two pins"
      (:score (-> initial (ball 3) (ball 2))) => 5)

(fact "Throws of initial frame"
      (:throws initial) => 0)

(fact "Throws after throwing once"
      (:throws (ball initial 3)) => 1)

(fact "Initial frames"
      (:frames initial) => 0)

(facts "Start next frame after two throws"
       (let [state (-> initial (ball 3) (ball 2))]
         (:frames state) => 1
         (:throws state) => 0))

(facts "Initially ten pins are up"
       (:pins initial) => 10)

(facts "Knocking over pins decreases the count"
       (:pins (ball initial 3)) => 7)

(facts "Reset pins to ten when starting new frame"
       (:pins (-> initial (ball 3) (ball 2))) => 10)

(facts "Start next frame after a strike (knocking down all pins with first ball of a frame)"
       (let [state (-> initial (ball 10))]
         (:frames state) => 1
         (:throws state) => 0
         (:pins state) => 10))

(facts "Score knocked down pins of next ball again after a spare (knocking down all pins with two balls of a frame)"
      (:score (-> initial (ball 7) (ball 3) (ball 1))) => 12
      (:score (-> initial (ball 7) (ball 3) (ball 1) (ball 2))) => 14)

(facts "Score knocked down pins of next two balls after a strike (knocking down all pins with first ball of a frame)"
      (:score (-> initial (ball 10) (ball 3) (ball 1))) => 18
      (:score (-> initial (ball 10) (ball 3) (ball 1) (ball 2))) => 20)

(def last-frame
  {:score 0
   :pins 10
   :throws 0
   :frames 9
   :score-multiplier (repeat 1)})

(facts "Extra ball for spare in last frame with reduced scoring"
       (:frames (-> last-frame (ball 5) (ball 5))) => 9
       (:throws (-> last-frame (ball 5) (ball 5))) => 2
       (:frames (-> last-frame (ball 5) (ball 5) (ball 5))) => 10
       (:score (-> last-frame (ball 5) (ball 5) (ball 5))) => 15)

(facts "Extra ball for strike in last frame with reduced scoring"
       (:frames (-> last-frame (ball 10))) => 9
       (:throws (-> last-frame (ball 10))) => 1
       (:frames (-> last-frame (ball 10) (ball 5) (ball 5))) => 10
       (:score (-> last-frame (ball 10) (ball 5) (ball 5))) => 20)

(facts "Check if game has finished"
       (finished? {:frames 9}) => false
       (finished? {:frames 10}) => true)

(fact "Test best score"
      (:score (nth (iterate #(ball % 10) initial) 12)) => 300)
