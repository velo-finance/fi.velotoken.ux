(ns fi.velotoken.ux.format
  (:require
   ["d3-format" :as d3-format]))

(def token-price (.format d3-format "$.6f"))

#_(token-price 0.01456)

(def token-volume (.format d3-format "=$5,d"))

#_(token-volume 45732111.20)

(def money (.format d3-format "=$5,.2f"))

#_(money 45732111)

(def perc-format (.format d3-format "=+.2%"))

(defn perc [n]
  (perc-format (/ n 100)))

#_(perc 0.0094)
#_(perc -1.0094)

(def si-prefix (.format d3-format "~s"))

#_(si-prefix 45732111)


