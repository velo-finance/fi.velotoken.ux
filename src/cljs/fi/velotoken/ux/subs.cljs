(ns fi.velotoken.ux.subs
  (:require
   [re-frame.core :as re-frame]
   ["moment" :as moment]
   ["moment-duration-format" :as moment-duration-format]
   [clojure.string :as str]
   ))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::ethereum-injected?
 (fn [db]
   (:ethereum-injected? db)))

(re-frame/reg-sub
 ::connected?
 (fn [db]
   (seq (:accounts db))))

(re-frame/reg-sub
  ::web3-account-connected
  (fn [db]
    (when-let [account (get-in db [:accounts 0])]
      (-> (subs account 0 8)
          str/lower-case))))

(re-frame/reg-sub
 ::flash-message
 (fn [db]
   (-> db :flash)))

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

(re-frame/reg-sub
  ::token-velocity-relative
  (fn [db]
    (-> db :rebase-data :velocity-relative)))

(re-frame/reg-sub
  ::token-last-rebase
  (fn [db]
    (-> db :rebase-data :last-rebase)))

;; MisesLegacyPool
(re-frame/reg-sub 
  ::mises-legacy-pool-data
  (fn [db]
    (-> db :mises-legacy-pool-data)))

;; Rebase
(re-frame/reg-sub
  ::last-rebase-counter
  (fn [db]
    (-> db :last-rebase-counter)))

(re-frame/reg-sub
  ::last-rebase-countdown
  (fn [db]
    (when-let [last-rebase-counter (-> db :last-rebase-counter)]
      (if (pos? last-rebase-counter)
        (let [m (.duration moment  last-rebase-counter "seconds")]
          (.format m "HH:mm:ss"))
        "00:00:00"))))

