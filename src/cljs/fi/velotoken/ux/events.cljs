(ns fi.velotoken.ux.events
  (:require
   [re-frame.core :as re-frame]
   [fi.velotoken.ux.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))
