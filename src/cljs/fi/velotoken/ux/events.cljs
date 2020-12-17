(ns fi.velotoken.ux.events
  (:require
   [re-frame.core :as re-frame]
   [fi.velotoken.ux.db :as db]))

(re-frame/reg-event-fx
 ::initialize-db
 (fn [_ _]
   {:db db/default-db
    :web3 [:initialize]}))

(re-frame/reg-event-fx
  ::web3-connect
  (fn [_ _]
    {:web3 [:connect]}))

(re-frame/reg-event-db
  :web3-locked
  (fn [db _]
     (assoc db :provider-not-present true)))

(re-frame/reg-event-fx
  :web3-accounts
  (fn [_ [_ accounts]]
    (prn accounts)))

(re-frame/reg-event-db
  :web3-chain-changed
  (fn [_ [_ chain-id]]))

(re-frame/reg-event-db
  :web3-accounts-chaned
  (fn [_ [_ accounts]]
     (prn "acccounts-chaned" accounts)))


