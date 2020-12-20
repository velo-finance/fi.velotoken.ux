(ns fi.velotoken.ux.web3.fixed-number
  (:require ["ethers" :as ethers]))

(defn from-value [v u]
  (ethers/FixedNumber.fromValue v u))

#_ (from-value "100000" 18)
#_ (from-value (ethers/BigNumber.from "100000") 18)

(defn to-unsafe-float [afn]
  (. afn toUnsafeFloat))

#_ (to-unsafe-float (from-value "100000" 18))
