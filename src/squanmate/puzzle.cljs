(ns squanmate.puzzle
  (:require [cats.monad.either :as either]
            [cats.core :as m]))

(defrecord Puzzle [top-layer bottom-layer])

;; the order of the pieces starts at the bottom left corner and goes clockwise
(defrecord TopLayer [pieces])
(defrecord BottomLayer [pieces])

(defrecord Piece [type])
(defrecord LayerError [msg layer])

(def edge (Piece. "e"))
(def corner (Piece. "c"))

(def square-square
  (let [e edge
        c corner]
    (Puzzle.
     (TopLayer. [c e c e
                 c e c e])
     (BottomLayer. [e c e c
                    e c e c]))))

(defn piece-value [piece]
  (condp = (:type piece)
    "c" 2
    "e" 1
    (throw (str "unknown piece " (pr-str piece)))))

(defn pieces-str [layer]
  (->> layer
       :pieces
       (map :type)
       (apply str)))
