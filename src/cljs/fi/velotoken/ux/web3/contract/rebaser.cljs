(ns fi.velotoken.ux.web3.contract.rebaser
  (:require
    ["ethers" :as ethers]

    [fi.velotoken.ux.web3.provider :refer [provider]]
    [fi.velotoken.ux.config :refer [addresses]]))

(defn build []
 (let [provider (provider)
        abi ["function getVelocity() view returns (uint256)",
             "function getRelativeVelocity() view returns (uint256)"
             "function lastRebase() view returns (uint256)"]]
    (ethers/Contract. (:rebaser addresses) (clj->js abi) provider)))

;; Interface
(defn velocity [^ethers/Contract c]
  (-> c .getVelocity))

(defn relative-velocity [^ethers/Contract c]
  (-> c .getRelativeVelocity))

(defn last-rebase [^ethers.Contract c]
  (-> c .lastRebase))
