(ns fi.velotoken.ux.views
  (:require
   [re-frame.core :as re-frame]
   [fi.velotoken.ux.subs :as subs]
   [fi.velotoken.ux.events :as events]
   [cljsjs.web3]
   [cljs.core.async :refer [go]]
   [cljs.core.async.interop :refer-macros [<p!]]
   ))

(defn connect-to-metamask []
  (re-frame/dispatch [::events/web3-connect]))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1 "VELO Token " @name]
     [:p "Connect to metamask " [:a {:href "#" :on-click connect-to-metamask} "Click here"] ] ]))



