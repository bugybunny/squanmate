(ns squanmate.pages.importer
  (:require [squanmate.ui.alg-importer :as alg-importer]))

(def importer-state (alg-importer/default-alg-importer-state))

(defn content []
  [:div.container
   [alg-importer/ui importer-state]])