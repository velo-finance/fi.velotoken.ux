(ns io.degenshephard.re-frame.web3-fx
  (:require
    [re-frame.core :refer [reg-fx dispatch console reg-event-db reg-event-fx]]
    ["ethers" :as ethers]
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
    (let [web3-provider (. ethers/providers -Web3Provider)
          provider (web3-provider. js/ethereum)
          abi ["function balanceOf(address) view returns ( uint256 )"] 
          velo-contract (ethers/Contract. "0x98ad9b32dd10f8d8486927d846d4df8baf39abe2" (clj->js abi) provider)

          balance (<p! (.balanceOf velo-contract "0x.."))
          ]

      (prn balance)
      )
    ))

(reg-fx :web3 web3-method)

