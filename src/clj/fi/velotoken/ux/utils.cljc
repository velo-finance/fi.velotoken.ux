(ns fi.velotoken.ux.utils)

(defmacro <p-float! [& p]
  `(fi.velotoken.ux.numbers/to-unsafe-float 
     (cljs.core.async.interop/<p! (do ~@p)) 18))
