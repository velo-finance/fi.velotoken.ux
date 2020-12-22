(ns fi.velotoken.ux.views
  (:require
   [re-frame.core :as re-frame]
   [fi.velotoken.ux.subs :as subs]
   [fi.velotoken.ux.events :as events]
   [cljs.core.async :refer [go]]
   [cljs.core.async.interop :refer-macros [<p!]]
   ))


(defn oind []
  [:div
       [:h1 "VELO Token " @name]
       [:p "Connect to metamask " [:a {:href "#" :on-click #(re-frame/dispatch [::events/web3-connect])} "Click here"]]
       [:p "Add token " [:a {:href "#" :on-click #(re-frame/dispatch [::events/web3-add-token])} "Click here"]]
       [:p "Velo Token data " [:a {:href "#" :on-click #(re-frame/dispatch [::events/web3-velo-token-data])} "Click here"]]
       ])

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div.app-container
     [:div.app-bg]
     [:div.app-content
      [:div.logo 
       [:img {:src "/images/logo+border.svg"}]]
      
      [:div.main-section

       [:div.social-sidebar
        (letfn [(item [t u]
                  [:a {:href u} [:img {:src (str "/images/socials/" t)}]])]
          [:ul
           [:li (item "telegram.svg" "#")]
           [:li (item "medium.svg" "#")]
           [:li (item "discord.svg" "#")]
           [:li (item "github.svg" "#")]
           [:li (item "reddit.svg" "#")]
           ])] 

       [:div.price-section
        [:div.section
         [:div.title "TRADING / METRICS"]
         [:div.body 
          [:div.gauge.vlo-token-price 
           [:div.title "VLO TOKEN PRICE"]
           [:div.value.green "$0.01454"]]
          [:div.gauges.grid.halves.price-change
           [:div.gauge.column
            [:div.title "1D CHANGE"]
            [:div.value.red "-31.94%"]]
           [:div.gauge.column
            [:div.title "7D CHANGE"]
            [:div.value.green "+100.94%"]]
           ]

          [:div.seperator]

          [:div.gauges.grid.thirds
           [:div.gauge.column
            [:div.title "VOLUME"]
            [:div.value "$124.252"]]

           [:div.gauge.column
            [:div.title "MARKET CAP"]
            [:div.value "$644.7K"]]

           [:div.gauge.column
            [:div.title "TOTAL SUPPLY"]
            [:div.value "45.7M VLO"]]]
          ]]]

       [:div.trading-sidebar
        (letfn [(item [t u]
                  [:a {:href u} [:img {:src (str "/images/trading/" t)}]])]
          [:ul
           [:li (item "uniswap.svg" "#")]
           [:li (item "dextools.svg" "#")]
           [:li (item "coingecko.svg" "#")]
           [:li (item "coinmarketcap.svg" "#")]
           ])]]
      
      
      [:div.rebase-section
       [:div.section
         [:div.title "12H VELOCITY REBASE"]
         [:div.body 
          [:div.gauges.grid.halves
           [:div.gauge.velocity.column
            [:div.title "VELOCITY"]
            [:div.value "13.4833%"]]
           [:div.gauge.countdown.column
            [:div.title "COUNTDOWN"]
            [:div.value "12:44:13"]]]

          [:div.rebase-button-section.grid.thirds
           [:div.rocket.0.column
            [:img {:src "/images/rocket-bg-0.svg"}]]
           [:div.rebase-button.column
            [:a {:href "#"} "REBASE"]]
           [:div.rocket.1.column
            [:img {:src "/images/rocket-bg-1.svg"}]]]]]]
      
      [:div.yield-farming-section
       [:div.section
        [:div.title "YIELD FARMING SECTION"]
        [:div.body 
         [:div.gauges
          [:div.gauge.mises-legacy-pool
           [:div.title "MISES LEGACY POOL APY"]
           [:div.value "261.48%"]]
         
         [:div.gauge.total-deposited
           [:div.title "TOTAL DEPOSITED"]
           [:div.value "99.58k"]]

         [:p "DEPOSIT VLO/ETH UNI-V2, EARN VLO"]

         [:div.seperator]

         [:div.gauges.grid.halves
          [:div.gauge.velocity.column
           [:div.title "TOTAL STAKED USD"]
           [:div.value "20,000.00"]]
          [:div.gauge.countdown.column
           [:div.title "$VLO EARNED"]
           [:div.value "~23222.64"]]]

         [:div.staking-button-section.grid.halves
          [:div.stake-button.column
           [:a {:href "#"} "STAKE"]]
          [:div.unstake-button.column
           [:a {:href "#"} "UNSTAKE"]]]
         
         [:div.harvest-button-section.grid.ones
          [:div.harvest-button.column
           [:a {:href "#"} "HARVEST"]]
          ]]]]
       
       ] 
      [:div#footer]
     ]

   ]))
