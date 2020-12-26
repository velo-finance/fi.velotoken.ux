(ns fi.velotoken.ux.web3.provider
  (:require
    ["ethers" :as ethers]
    [oops.core :refer [ocall]]))

(defn provider []
  (let [web3-provider (. ethers/providers -Web3Provider)]
    (web3-provider. js/ethereum)))

 
