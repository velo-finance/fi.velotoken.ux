(ns fi.velotoken.ux.numbers
  (:require [fi.velotoken.ux.web3.bignumber :as bignumber]
            [fi.velotoken.ux.web3.fixed-number :as fixed-number]))

(defn to-number [bn]
  (.toNumber bn))

(defn to-fixed-number [bn u]
  (fixed-number/from-value bn u))

(defn to-unsafe-float 
  "takes a bignumberish and a unit size and unsafely
  converts it to a javascript float"
  [bn u]
  (-> (fixed-number/from-value bn u)
      (fixed-number/to-unsafe-float)))

#_ (to-unsafe-float (bignumber/bignumber "100000") 2)
