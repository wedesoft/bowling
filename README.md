# Bowling

## Scoring rules
Traditional Bowling scoring (source: [Wikipedia][1]):

In traditional scoring, one point is scored for each pin that is knocked over, and when fewer than all ten pins are knocked down in two rolls in a frame (an open frame), the frame is scored with the total number of pins knocked down.
However, when all ten pins are knocked down with either the first or second rolls of a frame (a mark), bonus pins are awarded as follows:

* **Strike:** When all ten pins are knocked down on the first roll (marked "X" on the scorescreen), the frame receives ten pins plus a bonus of pinfall on the next two rolls (not necessarily the next two frames). A strike in the tenth (final) frame receives two extra rolls for bonus pins.
* **Spare:** When a second roll of a frame is needed to knock down all ten pins (marked "/" on the scorescreen), the frame receives ten pins plus a bonus of pinfall in the next roll (not necessarily the next frame). A spare in the first two rolls in the tenth (final) frame receives a third roll for bonus pins.

The maximum score is 300, achieved by getting twelve strikes in a row within the same game (known as a perfect game).

## Run project

To run the tests do:

```
clj -M:test
```

To play with the code, start the clj REPL and do:

```Clojure
(require '[bowling :refer :all])
(:score initial)
(:score (-> initial (ball 10)))
(:score (-> initial (ball 10) (ball 5) (ball 5)))
```

[1]: https://en.wikipedia.org/wiki/Ten-pin_bowling#Traditional_scoring
