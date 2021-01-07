(ns fi.velotoken.ux.utils)

(defmacro <p-float! [& p]
  `(fi.velotoken.ux.numbers/to-unsafe-float
    (cljs.core.async.interop/<p! (do ~@p))
    18))

(defmacro <p-fixed-number! [& p]
  `(fi.velotoken.ux.numbers/to-fixed-number
    (cljs.core.async.interop/<p! (do ~@p)) 18))

(defmacro try-flash! [ftype message & p]
  `(try
     ~@p
     (catch js/Error e#
       (let [error# (or 
                      (let [cause# (oops.core/oget e# :?cause)]
                        (and (string? cause#) cause#))
                      (oops.core/oget e# :?cause.?message)
                      (oops.core/oget e# :?message))]
         (re-frame.core/dispatch [:fi.velotoken.ux.events/flash
                                  {:type ~ftype
                                   :message ~message
                                   :error error#}]))
       (throw e#))))


