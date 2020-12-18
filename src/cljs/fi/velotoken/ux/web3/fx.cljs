(ns fi.velotoken.ux.web3.fx
  (:require
    [re-frame.core :refer [reg-fx dispatch console reg-event-db reg-event-fx]]
    ["ethers" :as ethers]
    [cljs.core.async :refer [go]]
    [cljs.core.async.interop :refer-macros [<p!]]
    
    [fi.velotoken.ux.web3.contract.velo-token :as velo-token]
    [fi.velotoken.ux.web3.contract.rebaser :as rebaser]))


;; Web3 FX

(defmulti web3-method first)

(defmethod web3-method :connect []
  (go 
    (let [accounts (<p! (js/ethereum.request #js {:method "eth_requestAccounts"} ))]
      (if (empty? accounts)
        ;; metamask is locked
        (dispatch [:web3-locked])
        (dispatch [:web3-accounts-changed (js->clj accounts)])))))

(defmethod web3-method :initialize []
  (when-not js/ethereum
    (dispatch [:web3-ethereum-not-present]))
  (when js/ethereum
    ;; register events of interest
    (js/ethereum.on "accountsChanged" (fn [accounts] (dispatch [:web3-accounts-changed (js->clj accounts)])))
    (js/ethereum.on "chainChanged" (fn [chain-id] (dispatch [:web3-chain-changed (js->clj chain-id)])))
    (js/ethereum.on "connect" (fn [connect-info] (dispatch [:web3-connected (js->clj connect-info)])))
    ;; RPC Error {:message .., :code .., :data ..}
    (js/ethereum.on "disconnect" (fn [rpc-error] (dispatch [:web3-disconnect (js->clj rpc-error)])))
    (js/ethereum.on "message" (fn [message] (dispatch [:web3-message (js->clj message)])))))

(defmethod web3-method :add-token [[_ token-info]]
  (go 
    ;; returns true when the token is watched on the wallet. So even when
    ;; "cancel" is pressed when the token is watched, this returns true
    (let [token-watched (<p! (js/ethereum.request (clj->js {:method "wallet_watchAsset",
                                                            :params token-info})))]
      (if token-watched 
        (dispatch [:web3-add-token-confirmed])
        (dispatch [:web3-add-token-rejected])))))

(defmethod web3-method :velo-token-data [[_ _]]
  (go
    (let [velo-contract (velo-token/build)
          rebaser-contract (rebaser/build)]

      (prn {:balance (<p! (.balanceOf velo-contract "0x.."))
            :total-supply (<p! (.totalSupply velo-contract))
            :velocity (<p! (rebaser/velocity rebaser-contract))
            :velocity-relative (<p! (.getRelativeVelocity rebaser-contract))
            :last-rebase (<p! (.lastRebase rebaser-contract))
            }))))

(reg-fx :web3 web3-method)
