(ns fi.velotoken.ux.coingecko-fx
  (:require
    [re-frame.core :refer [reg-fx dispatch console reg-event-db reg-event-fx]]
    [cljs.core.async :refer [go]]
    [cljs.core.async.interop :refer-macros [<p!]]

    [fi.velotoken.ux.events :as events]
    
    [fi.velotoken.ux.coingecko :as coingecko]))

(reg-fx :coingecko 
        (fn [_]
          (go
            (let [info (<! (coingecko/get-token-price))]
              (dispatch [::events/coingecko-update info])))))
