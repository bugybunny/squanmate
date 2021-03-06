(ns squanmate.pages.algorithm-trainer
  (:require [squanmate.scramblers.alg-trainer :as alg-trainer]
            [squanmate.pages.page-content :as page-content]))

(defn initial-state []
  (let [settings-atom (alg-trainer/new-default-state)
        maybe-saved-settings (alg-trainer/try-load-settings)]
    (when maybe-saved-settings
      (swap! settings-atom merge maybe-saved-settings))
    (add-watch settings-atom nil
               (fn [_key _ref _old-value new-state]
                 (alg-trainer/save-settings! new-state)))
    settings-atom))

(defonce page-state (initial-state))

(defn content []
  [:div
   [alg-trainer/trainer-component page-state]])

(defmethod page-content/page :algorithm-trainer []
  [:div
   [content]])
