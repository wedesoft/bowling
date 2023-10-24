(ns bowling
    (:require [midje.sweet :refer :all]))

(def initial
  {:score 0
   :pins 10
   :throws 0
   :frames 0
   :score-multiplier (repeat 1)})

(defn score-roll [state pins]
  (-> state
      (update :score + (* pins (-> state :score-multiplier first)))
      (update :score-multiplier (fn [lst] (drop 1 lst)))))

(defn score-spare [state]
  (if (zero? (:pins state))
    (update state :score-multiplier (fn [lst] (cons (inc (first lst)) (rest lst))))
    state))

(defn score-strike [state]
  (if (and (zero? (:pins state)) (== (:throws state) 1))
    (update state :score-multiplier (fn [lst] (cons (first lst) (cons (inc (second lst)) (drop 2 lst)))))
    state))

(defn detect-frame-end [state]
  (if (or (>= (:throws state) 2) (zero? (:pins state)))
    (-> state
        (assoc :pins 10)
        (assoc :throws 0)
        (update :frames inc))
    state))

(defn ball [state pins]
  (-> state
      (update :pins - pins)
      (update :throws inc)
      (score-roll pins)
      score-spare
      score-strike
      detect-frame-end))

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

(fact "Extra ball for spare in last frame with reduced scoring"
      (:frames (-> last-frame (ball 5) (ball 5))) => 9
      (:throws (-> last-frame (ball 5) (ball 5))) => 2)
