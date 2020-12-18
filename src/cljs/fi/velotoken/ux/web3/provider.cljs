(ns fi.velotoken.ux.web3.provider
  (:require
    ["ethers" :as ethers]))

(defn provider []
  (let [web3-provider (. ethers/providers -Web3Provider)]
    (web3-provider. js/ethereum)))
