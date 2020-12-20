(ns fi.velotoken.ux.events
  (:require
   [re-frame.core :as re-frame :refer [inject-cofx]]
   [akiroz.re-frame.storage :refer [reg-co-fx!]]
   [fi.velotoken.ux.db :as db]))

(reg-co-fx! :fi-velotoken-ux ;; local storage key
            {:fx :store      ;; re-frame fx ID
             :cofx :store})  ;; re-frame cofx ID

(re-frame/reg-event-fx
 ::initialize-db
 [(inject-cofx :store)]
 (fn [{:keys [store]} _]
   {:db (-> db/default-db
            (assoc :accounts (:accounts store)))
    :web3 [:initialize]}))

;; Web3 add token

(re-frame/reg-event-fx
  ::web3-add-token
  (fn [_ _]
    {:web3 [:add-token {:type "ERC20"
                        :options 
                        {:address "0x98ad9b32dd10f8d8486927d846d4df8baf39abe2"
                         :symbol "VLO"
                         :decimals 18
                         :image "https://i.ibb.co/6gN5Mxb/logo-vector-red-on-yellow.png"
                         }}]}))

(re-frame/reg-event-db
  :web3-add-token-confirmed
  (fn [_ _]
    (prn "web3 add token confirmed")))


(re-frame/reg-event-db
  :web3-add-token-rejected
  (fn [_ _]
    (prn "web3 add token rejected")))

;; Web3 events

(re-frame/reg-event-fx
  ::web3-connect
  (fn [_ _]
    {:web3 [:connect]}))

(re-frame/reg-event-fx
  ::web3-velo-token-data
  (fn [_ _]
    {:web3 [:velo-token-data]}))

(re-frame/reg-event-db
  :web3-locked
  (fn [db _]
     (assoc db :provider-not-present true)))

(re-frame/reg-event-fx
  :web3-accounts-changed
  [(inject-cofx :store)]
  (fn [{:keys [store db]} [_ accounts]]
    {:db (assoc db :accounts accounts)
     :store (assoc store :accounts accounts)}
    ))

(re-frame/reg-event-db
  :web3-chain-changed
  (fn [_ [_ chain-id]]))

(re-frame/reg-event-db
  :web3-accounts-changed
  (fn [_ [_ accounts]]
     (prn "accounts-changed" accounts)))

(re-frame/reg-event-db
  :web3-connected
  (fn [_ [_ connect-info]]
     (prn "connection-info" connect-info)))

(re-frame/reg-event-db
  :web3-disconnect
  (fn [_ [_ rpc-error]]
     (prn "disconnected" rpc-error)))

(re-frame/reg-event-db
  :web3-message
  (fn [_ [_ message]]
     (prn "message" message)))
