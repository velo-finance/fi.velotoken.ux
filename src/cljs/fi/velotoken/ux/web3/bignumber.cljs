(ns fi.velotoken.ux.web3.bignumber
  (:require ["ethers" :as ethers]))

;; https://docs.ethers.io/v5/api/utils/bignumber/

(defn bignumber? [x]
  ; Doesn't work for web3.js, leaving it here for the future record
  ; (and x (cljs.core/= (aget x "constructor" "name") "BigNumber"))
  (and x (aget x "toNumber")))

(defn bignumber [n] (ethers/BigNumber.from (str n)))

;; (bignumber "100000000000000000000")

(defn to-number
  "converts to a javascript integer, overflows when problematic"
  [bn]
  (. bn toNumber))

;; (to-number (bignumber "1000000000"))

;; wei	    0	 
;; kwei	    3	 
;; mwei	    6	 
;; gwei	    9	 
;; szabo   12	 
;; finney  15	 
;; ether   18	 

(defn format-units
  "Returns a string representation of value formatted with unit digits (if it
  is a number) or to the unit specified (if a string)"
  [n u]
  (. ethers/utils formatUnits n u))

#_(format-units "100000000000000000000" 18) ;; => "100.0"

(defn format-ether
  "format-ether with unit ether"
  [n]
  (format-units n "ether"))

(defn parse-units
  "Returns a BigNumber representation of value, parsed with unit digits (if it
  is a number) or from the unit specified (if a string)."
  [n u]
  (. ethers/utils parseUnits n u))

#_(parse-units "100" 18) ;; => #object[BigNumber 100000000000000000000]

(defn parse-ether
  "parse-units with unit ether"
  [n]
  (parse-units n "ether"))

#_(parse-ether "2727.578608087181823502")
