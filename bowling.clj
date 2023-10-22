(ns bowling
    (:require [midje.sweet :refer :all]))

(def initial
  {:score 0
   :pins 10
   :remaining-throws 2
   :remaining-frames 9})

(defn progress-frame [state]
  (if (or (zero? (:remaining-throws state)) (zero? (:pins state)))
    (-> state
        (assoc :pins 10)
        (assoc :remaining-throws 2)
        (update :remaining-frames dec))
    state))

(defn ball [state pins]
  (-> state
      (update :pins - pins)
      (update :score + pins)
      (update :remaining-throws dec)
      progress-frame))

(fact "Initial score is zero"
      (:score initial) => 0)

(fact "Knock over zero pins"
      (:score (ball initial 0)) => 0)

(fact "Knock over three pins"
      (:score (ball initial 3)) => 3)

(fact "Knock over three and then two pins"
      (:score (-> initial (ball 3) (ball 2))) => 5)

(fact "Remaining throws of initial frame"
      (:remaining-throws initial) => 2)

(fact "Remaining throws after throwing once"
      (:remaining-throws (ball initial 3)) => 1)

(fact "Initial remaining frames"
      (:remaining-frames initial) => 9)

(facts "Start next frame after two throws"
       (let [state (-> initial (ball 3) (ball 2))]
         (:remaining-frames state) => 8
         (:remaining-throws state) => 2))

(facts "Initially ten pins are up"
       (:pins initial) => 10)

(facts "Knocking over pins decreases the count"
       (:pins (ball initial 3)) => 7)

(facts "Reset pins to ten when starting new frame"
       (:pins (-> initial (ball 3) (ball 2))) => 10)

(facts "Start next frame after a strike (knocking down all pins with first ball of a frame)"
       (let [state (-> initial (ball 10))]
         (:remaining-frames state) => 8
         (:remaining-throws state) => 2
         (:pins state) => 10))

(fact "Score knocked down pins of next ball again after a spare (knocking down all pins with two balls of a frame)"
      (:score (-> initial (ball 7) (ball 3) (ball 1))) => 12)
