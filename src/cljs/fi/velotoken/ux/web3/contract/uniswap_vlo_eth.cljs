(ns fi.velotoken.ux.web3.contract.uniswap-vlo-eth
  (:require
    ["ethers" :as ethers]

    [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                       oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]

    [fi.velotoken.ux.web3.provider :refer [provider]]
    [fi.velotoken.ux.config :refer [addresses]]))

;; NOTE: we can fetch data over a metamask
;;       wallet connection. If we want to get
;;       a signer. We need to be connected with
;;       an address.

(defn -build [signer?]
  (let [provider (provider)
        abi ["function balanceOf(address) view returns (uint256)",
             "function totalSupply() view returns (uint256)"]]
    (ethers/Contract. (:uni-vlo-eth addresses) 
                      (clj->js abi) 
                      (if-not signer?
                        provider
                        (ocall provider :getSigner)))))

(def build (partial -build false))
(def build-signer (partial -build true))

;; Interface
(defn balance-of [^ethers/Contract c account]
  (ocall c :balanceOf account))

(defn total-supply [^ethers/Contract c]
  (ocall c :totalSupply))
