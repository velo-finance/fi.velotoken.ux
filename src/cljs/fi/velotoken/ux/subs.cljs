(ns fi.velotoken.ux.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
  ::token-price
  (fn [db]
    (-> db :coingecko :usd)))


(re-frame/reg-sub
  ::token-1d-change
  (fn [db]
    (-> db :coingecko :24h-change)))

(re-frame/reg-sub
  ::token-7d-change
  (fn [db]
    (-> db :coingecko :7d-change)))

(re-frame/reg-sub
  ::token-1d-volume
  (fn [db]
    (-> db :coingecko :usd-24h-vol)))


(re-frame/reg-sub
  ::token-market-cap
  (fn [db]
    (-> db :coingecko :market-cap)))

(re-frame/reg-sub
  ::token-total-supply
  (fn [db]
    (-> db :coingecko :total-supply)))
