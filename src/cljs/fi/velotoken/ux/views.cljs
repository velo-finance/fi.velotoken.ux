(ns fi.velotoken.ux.views
  (:require
   [re-frame.core :as re-frame]
   [fi.velotoken.ux.subs :as subs]
   [fi.velotoken.ux.events :as events]
   [cljsjs.web3]
   [cljs.core.async :refer [go]]
   [cljs.core.async.interop :refer-macros [<p!]]
   ))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1 "VELO Token " @name]
     [:p "Connect to metamask " [:a {:href "#" :on-click #(re-frame/dispatch [::events/web3-connect])} "Click here"]]
     [:p "Add token " [:a {:href "#" :on-click #(re-frame/dispatch [::events/web3-add-token])} "Click here"]]
     [:p "Velo Token data " [:a {:href "#" :on-click #(re-frame/dispatch [::events/web3-velo-token-data])} "Click here"]]
     ]))



