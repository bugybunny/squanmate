(ns squanmate.ui.alg-importer
  (:require [cats.core :as m]
            [cats.monad.either :as either]
            [clojure.string :as str]
            [reagent.core :as reagent]
            [squanmate.alg.execution :as execution]
            [squanmate.alg.serialization :as serialization]
            [squanmate.pages.links :as links]
            [squanmate.shapes :as shapes]
            [squanmate.ui.common :as common]
            [squanmate.ui.drawing.newmonochrome :as newmonochrome]
            [squanmate.ui.parity :as parity]
            [squanmate.slicing :as slicing]))

(defn default-alg-importer-state []
  (reagent/atom {:algorithm nil}))

(defn starting-puzzle-for-alg [alg-string]
  (m/mlet [transformation-steps (parity/cubeshape-start-&-end-positions alg-string)]
          (m/return (-> transformation-steps last))))

(defn import-alg [alg-string]
  (m/mlet [start-transformation-step (starting-puzzle-for-alg alg-string)]
          (let [puzzle-spec (-> start-transformation-step
                                :puzzle
                                serialization/puzzle-specification)]
            (m/return {:algorithm alg-string
                       :starting-puzzle-spec puzzle-spec}))))

(defn puzzle-from-spec [spec]
  (let [top (-> spec :starting-puzzle-spec :top-name)
        bottom (-> spec :starting-puzzle-spec :bottom-name)
        p (shapes/puzzle-with-layers top bottom)
        rotation (-> spec :starting-puzzle-spec :initial-rotation) ]
    (m/mlet [result (execution/transformation-result p rotation)]
            (m/return (:puzzle result)))))

(defn- error-box [error]
  [common/alert {:bs-style :warning}
   [:div [:strong "Error: "] error]])

(defn- import-button [spec]
  [common/button {:bs-style :success
                  :on-click #(links/set-link-to-visualization
                              (let [puzzle-spec (:starting-puzzle-spec spec)]
                                {:top-name (:top-name puzzle-spec)
                                 :bottom-name (:bottom-name puzzle-spec)
                                 :initial-rotation (:initial-rotation puzzle-spec)
                                 :algorithm (:algorithm spec)}))}
   "Import to Algorithm shape visualizer"])

(defn non-sliceable-notification []
  [common/well {:bs-size :small}
   [:div
    "The puzzle is not at a position that can be sliced. Only sliceable starting
   positions are supported. Typically you can fix this by removing a rotation
   before the algorithm's first / (slice)."
    [:div
     "If you want, you can manually add an initial rotation after the algorithm
     has been imported."]]])

(defn- success-box [spec]
  (let [p (puzzle-from-spec spec)]
    (either/branch
     p
     (fn [error]
       (println "Internal error: could not render starting step: " (pr-str error))
       [:div "Internal error. Please report this as an issue! " (pr-str error)])
     (fn [starting-puzzle]
       [common/alert {:bs-style :success}
        [:strong "Success!"]
        [:div "Looks like the algorithm starts at this state:"]
        [newmonochrome/monochrome-puzzle starting-puzzle]
        (if (slicing/sliceable? starting-puzzle)
          [import-button spec]
          [non-sliceable-notification])]))))

(defn- import-alg-component [state]
  (let [alg-string (:algorithm @state)]
    (when-not (str/blank? alg-string)
      (let [result (import-alg alg-string)]
        [:div
         (either/branch result
                        error-box
                        success-box)]))))

(defn ui [state]
  [:div.center
   [:div.col-md-8.col-lg-8
    [:h2 "Instructions:"]
    "Use this if you want to inspect or manipulate an algorithm with Squanmate."
    [:div
     "Enter an algorithm that ends in cubeshape. Acceptable ending positions are (0) or (1,-1)."]
    [:div.top10
     [common/input-box (reagent/cursor state [:algorithm]) "Cubeshape algorithm"]]
    [import-alg-component state]]])
