(ns io.degenshephard.re-frame.web3-fx
  (:require
    [re-frame.core :refer [reg-fx dispatch console reg-event-db reg-event-fx]]
    [cljs.core.async :refer [go]]
    [cljs.core.async.interop :refer-macros [<p!]]))

;; Web3 FX

(defmulti web3-method first)


(defmethod web3-method :connect []
  (go 
    (let [accounts (<p! (js/ethereum.request #js {:method "eth_requestAccounts"} ))]
      (if (empty? accounts)
        ;; metamask is locked
        (dispatch [:web3-locked])
        (dispatch [:web3-accounts accounts])))))

(defmethod web3-method :initialize []
  (if-not js/ethereum
    (dispatch [:web3-ethereum-not-present]))
  ;; register events of interest
  (js/ethereum.on "accountsChanged" (fn [accounts] (dispatch [:web3-accounts-changed accounts])))
  (js/ethereum.on "chainChanged" (fn [chainId] (dispatch [:web3-chain-changed chainId])))
  (js/ethereum.on "connect" (fn [connect-info] (dispatch [:web3-connected connect-info])))
  ;; RPC Error {:message .., :code .., :data ..}
  (js/ethereum.on "disconnect" (fn [rpc-error] (dispatch [:web3-disconnect rpc-error])))
  (js/ethereum.on "message" (fn [message] (dispatch [:web3-message message]))))

(reg-fx :web3 web3-method)

