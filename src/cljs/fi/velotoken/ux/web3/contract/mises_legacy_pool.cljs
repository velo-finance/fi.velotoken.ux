(ns fi.velotoken.ux.web3.contract.mises-legacy-pool
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
        abi ["function rewardRate() view returns (uint256)",
             "function balanceOf(address) view returns (uint256)"
             "function totalSupply() view returns (uint256)"
             "function earned(address) view returns (uint256)"
             ]]
    (ethers/Contract. (:mises-legacy-pool addresses) 
                      (clj->js abi) 
                      (if-not signer?
                        provider
                        (ocall provider :getSigner)))))

(def build (partial -build false))
(def build-signer (partial -build true))


(defn velocity [^ethers/Contract c]
  (ocall c :getVelocity))

(defn relative-velocity [^ethers/Contract c]
  (ocall c :getRelativeVelocity))

(defn last-rebase [^ethers.Contract c]
  (ocall c :lastRebase))

(defn rebase [^ethers.Contract c]
  (ocall c :rebase))

