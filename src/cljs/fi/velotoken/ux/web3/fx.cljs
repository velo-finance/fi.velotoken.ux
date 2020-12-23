(ns fi.velotoken.ux.web3.fx
  (:require
    [re-frame.core :refer [reg-fx dispatch console reg-event-db reg-event-fx]]
    ["ethers" :as ethers]
    [cljs.core.async :refer [go]]
    [cljs.core.async.interop :refer-macros [<p!]]

    [fi.velotoken.ux.events :as events]
    
    [fi.velotoken.ux.numbers :as numbers]
    [fi.velotoken.ux.web3.contract.velo-token :as velo-token]
    [fi.velotoken.ux.web3.contract.rebaser :as rebaser]
    
    [fi.velotoken.ux.coingecko :as coingecko]))


;; Web3 FX

(defmulti web3-method first)

(defmethod web3-method :connect []
  (go 
    (let [accounts (<p! (js/ethereum.request #js {:method "eth_requestAccounts"} ))]
      (if (empty? accounts)
        ;; metamask is locked
        (dispatch [::events/web3-locked])
        (dispatch [::events/web3-accounts-changed (js->clj accounts)])))))


(defmethod web3-method :initialize []
  ;; NOTE: when not using exists? we get an not defined error
  (if-not (exists? js/ethereum)
    (dispatch [::events/web3-ethereum-not-present])
    (do
      ;; register events of interest
      (js/ethereum.on "accountsChanged" (fn [accounts] (dispatch [::events/web3-accounts-changed (js->clj accounts)])))
      (js/ethereum.on "chainChanged" (fn [chain-id] (dispatch [::events/web3-chain-changed (js->clj chain-id)])))
      (js/ethereum.on "connect" (fn [connect-info] (dispatch [::events/web3-connected (js->clj connect-info)])))
      ;; RPC Error {:message .., :code .., :data ..}
      (js/ethereum.on "disconnect" (fn [rpc-error] (dispatch [::events/web3-disconnect (js->clj rpc-error)])))
      (js/ethereum.on "message" (fn [message] (dispatch [::events/web3-message (js->clj message)])))

      (dispatch [::events/web3-initialized]))))

(defmethod web3-method :add-token [[_ token-info]]
  (go 
    ;; returns true when the token is watched on the wallet. So even when
    ;; "cancel" is pressed when the token is watched, this returns true
    (let [token-watched (<p! (js/ethereum.request (clj->js {:method "wallet_watchAsset",
                                                            :params token-info})))]
      (if token-watched 
        (dispatch [::events/web3-add-token-confirmed])
        (dispatch [::events/web3-add-token-rejected])))))


(defmethod web3-method :velo-rebase-data [[_ _]]
  (go
    (let [velo-contract (velo-token/build)
          rebaser-contract (rebaser/build)
          to-float #(numbers/to-unsafe-float % 18)]

      (dispatch [::events/web3-velo-rebase-data-recv 
                 {:velocity-relative (to-float (<p! (rebaser/relative-velocity rebaser-contract)))
                  :last-rebase (numbers/to-number (<p! (rebaser/last-rebase rebaser-contract)))}]))))

(defmethod web3-method :velo-token-data [[_ _]]
  (go
    (let [velo-contract (velo-token/build)
          rebaser-contract (rebaser/build)
          to-float #(numbers/to-unsafe-float % 18)]

      (prn {:balance (to-float  (<p! (velo-token/balance-of velo-contract "0x...")))
            :total-supply (to-float  (<p! (velo-token/total-supply velo-contract)))
            :velocity (to-float  (<p! (rebaser/velocity rebaser-contract)))
            :velocity-relative (to-float  (<p! (rebaser/relative-velocity rebaser-contract)))
            :last-rebase (numbers/to-number (<p! (rebaser/last-rebase rebaser-contract)))
            }))))

(reg-fx :web3 web3-method)

(reg-fx :web3-multiple 
        (fn [calls]
          (doseq [c calls] 
            (web3-method c))))
