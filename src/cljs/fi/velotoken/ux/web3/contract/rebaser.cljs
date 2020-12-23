(ns fi.velotoken.ux.web3.contract.rebaser
  (:require
    ["ethers" :as ethers]

    [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                       oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]

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
  (ocall c :getVelocity))

(defn relative-velocity [^ethers/Contract c]
  (ocall c :getRelativeVelocity))

(defn last-rebase [^ethers.Contract c]
  (ocall c :lastRebase))

