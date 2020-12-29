(ns fi.velotoken.ux.web3.contract.mises-legacy-pool
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
        abi ["function rewardRate() view returns (uint256)",
             "function balanceOf(address) view returns (uint256)"
             "function totalSupply() view returns (uint256)"
             "function earned(address) view returns (uint256)"
             "function stake(uint256)" 
             "function getReward()"
             "function exit()"
             ]]
    (ethers/Contract. (:mises-legacy-pool addresses) 
                      (clj->js abi) 
                      (if-not signer?
                        provider
                        (ocall provider :getSigner)))))

(def build (partial -build false))
(def build-signer (partial -build true))

(defn balance-of [^ethers/Contract c account]
  (ocall c :balanceOf account))

(defn earned [^ethers/Contract c account]
  (ocall c :earned account))

(defn total-supply [^ethers/Contract c]
  (ocall c :totalSupply))

(defn reward-rate [^ethers.Contract c]
  (ocall c :rewardRate))

(defn stake [^ethers.Contract c amount]
  (ocall c :stake amount))

(defn get-reward [^ethers.Contract c]
  (ocall c :getReward))

(defn exit [^ethers.Contract c]
  (ocall c :exit))





