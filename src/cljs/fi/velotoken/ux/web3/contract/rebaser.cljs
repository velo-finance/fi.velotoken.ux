(ns fi.velotoken.ux.web3.contract.rebaser
  (:require
    ["ethers" :as ethers]

    [oops.core :refer [ocall]]

    [fi.velotoken.ux.web3.provider :refer [provider]]
    [fi.velotoken.ux.config :refer [addresses]]))

;; NOTE: we can fetch data over a metamask
;;       wallet connection. If we want to get
;;       a signer. We need to be connected with
;;       an address.

(defn -build [signer?]
 (let [provider (provider)
        abi ["function getVelocity() view returns (uint256)",
             "function getRelativeVelocity() view returns (uint256)"
             "function lastRebase() view returns (uint256)"
             "function rebase()"]]
    (ethers/Contract. (:rebaser addresses) 
                      (clj->js abi) 
                      (if-not signer?
                        provider
                        (ocall provider :getSigner)))))

(def build (partial -build false))
(def build-signer (partial -build true))

;; Interface
(defn velocity [^ethers/Contract c]
  (ocall c :getVelocity))

(defn relative-velocity [^ethers/Contract c]
  (ocall c :getRelativeVelocity))

(defn last-rebase [^ethers.Contract c]
  (ocall c :lastRebase))

(defn rebase [^ethers.Contract c]
  (ocall c :rebase))

