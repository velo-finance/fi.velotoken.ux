(ns fi.velotoken.ux.web3.contract.rebaser
  (:require
    ["ethers" :as ethers]

    [fi.velotoken.ux.web3.provider :refer [provider]]
    [fi.velotoken.ux.web3.config :refer [addresses]]))

(defn build []
 (let [provider (provider)
        abi ["function getVelocity() view returns (uint256)",
             "function getRelativeVelocity() view returns (uint256)"
             "function lastRebase() view returns (uint256)"]]
    (ethers/Contract. (:rebaser addresses) (clj->js abi) provider)))

;; Interface

(defn bn->float [n]
  n)

(defn velocity [c]
  (-> c .getVelocity bn->float))

(defn relative-velocity [c]
  )

(defn last-rebase [c]
  )
