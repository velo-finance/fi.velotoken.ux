(ns fi.velotoken.ux.web3-fx
  (:require
    [re-frame.core :refer [reg-fx dispatch console reg-event-db reg-event-fx]]
    ["ethers" :as ethers]
    [cljs.core.async :refer [go]]
    [cljs.core.async.interop :refer-macros [<p!]]))


;; Helpers
(def addresses 
  {:velo-token "0x98ad9b32dd10f8d8486927d846d4df8baf39abe2" 
   :uni-vlo-eth "0x259E558892783fd8941EBBeDa694318C1C3d9263"
   :bal-vlo-eth "0xE52E551141D29e4D08A826ff029059f1fB5F6f52"
   :timelock "0x22daA1F74A8785965E841270B9aED601F9eD310D"
   :gov-alpha "0xA1D8800AE2f4794F2910CfCD835831FAae69CeA0"
   :rebaser "0x1785e8d6adE68b4937137F07C15b098aE0caF001"
   :fee-charger "0xeBd8065CbBe0C13917a0E31FE1F85D91649E2244"
   :mises-legacy-pool "0x3d3Fddb7B10F46938F8a644D4612Af2827C1e577"})

(defn provider []
  (let [web3-provider (. ethers/providers -Web3Provider)]
    (web3-provider. js/ethereum)))

(defn velo-contract []
  (let [provider (provider)
        abi ["function balanceOf(address) view returns (uint256)",
             "function totalSupply() view returns (uint256)"
             "function velosScalingFactor() view returns (uint256)"]]
    (ethers/Contract. (:velo-token addresses) (clj->js abi) provider)))

(defn rebaser-contract []
 (let [provider (provider)
        abi ["function getVelocity() view returns (uint256)",
             "function getRelativeVelocity() view returns (uint256)"
             "function lastRebase() view returns (uint256)"]]
    (ethers/Contract. (:rebaser addresses) (clj->js abi) provider)))

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
    (let [velo-contract (velo-contract)
          rebaser-contract (rebaser-contract)]
      (prn {:balance (<p! (.balanceOf velo-contract "0x.."))
            :total-supply (<p! (.totalSupply velo-contract))
            :velocity (<p! (.getVelocity rebaser-contract))
            :velocity-relative (<p! (.getRelativeVelocity rebaser-contract))
            :last-rebase (<p! (.lastRebase rebaser-contract))
            })
      )
    ))

(reg-fx :web3 web3-method)

