(ns fi.velotoken.ux.utils)

(defmacro <p-float! [& p]
  `(fi.velotoken.ux.numbers/to-unsafe-float 
     (cljs.core.async.interop/<p! (do ~@p)) 18))


(defmacro <p-fixed-number! [& p]
  `(fi.velotoken.ux.numbers/to-fixed-number
     (cljs.core.async.interop/<p! (do ~@p)) 18))
