(ns fi.velotoken.ux.web3.contract.velo-token
  (:require
    ["ethers" :as ethers]
    [fi.velotoken.ux.web3.provider :refer [provider]]
    [fi.velotoken.ux.web3.config :refer [addresses]]))

(defn build []
  (let [provider (provider)
        abi ["function balanceOf(address) view returns (uint256)",
             "function totalSupply() view returns (uint256)"
             "function velosScalingFactor() view returns (uint256)"]]
    (ethers/Contract. (:velo-token addresses) (clj->js abi) provider)))


;; Interface
(defn balance-of [c account]
  )

(defn total-supply [c]
  )


