(ns fi.velotoken.ux.views
  (:require
   [re-frame.core :as re-frame]
   [fi.velotoken.ux.subs :as subs]
   [fi.velotoken.ux.events :as events]
   [fi.velotoken.ux.format :as frm]
   [cljs.core.async :refer [go]]
   [cljs.core.async.interop :refer-macros [<p!]]
   ))

(def <su re-frame/subscribe)
(def >ev re-frame/dispatch)

;; (comment oind []
;;   [:div
;;        [:h1 "VELO Token " @name]
;;        [:p "Connect to metamask " [:a {:href "#" :on-click #(re-frame/dispatch [::events/web3-connect])} "Click here"]]
;;        [:p "Add token " [:a {:href "#" :on-click #(re-frame/dispatch [::events/web3-add-token])} "Click here"]]
;;        [:p "Velo Token data " [:a {:href "#" :on-click #(re-frame/dispatch [::events/web3-velo-token-data])} "Click here"]]
;;        ])


(defn price-section []
  [:div.price-section
   [:div.section
    [:div.title "TRADING / METRICS"]
    [:div.body 
     [:div.gauge.vlo-token-price 
      [:div.title "VLO TOKEN PRICE"]
      [:div.value.green (frm/token-price  @(<su [::subs/token-price]))]]
     [:div.gauges.grid.halves.price-change
      (let [v  @(<su [::subs/token-1d-change])]
        [:div.gauge.column
         [:div.title "1D CHANGE"]
         [:div.value {:class (if (pos? v) "green" "red")} (frm/perc v)]])
      (let [v  @(<su [::subs/token-7d-change])]
        [:div.gauge.column
         [:div.title "7D CHANGE"]
         [:div.value {:class (if (pos? v) "green" "red")} (frm/perc v)]])]

     [:div.seperator]

     [:div.gauges.grid.thirds
      [:div.gauge.column
       [:div.title " 24H VOLUME"]
       [:div.value (frm/token-volume @(<su [::subs/token-1d-volume]))]]

      [:div.gauge.column
       [:div.title "MARKET CAP"]
       [:div.value (frm/si-prefix @(<su [::subs/token-market-cap]))]]

      [:div.gauge.column
       [:div.title "TOTAL SUPPLY"]
       [:div.value (frm/si-prefix @(<su [::subs/token-total-supply]))]]]
     ]]]
       )

(defn main-panel []
  [:div.app-container
     [:div.app-bg]
     [:div.app-content
      [:div#menu
       [:ul 
        [:li.connect [:a {:href "#" :on-click #(>ev [::events/web3-connect])} "CONNECT"]]
        [:li.add-token [:a {:on-click #(>ev [::events/web3-add-token])} "ADD TOKEN"]]]]
      [:div.logo 
       [:img {:src "/images/logo+border.svg"}]]

      [:div.logo-title
       [:span "VELOTOKEN"]]
      
      [:div.main-section

       [:div.social-sidebar
        (letfn [(item [t u]
                  [:a {:href u} [:img {:src (str "/images/socials/" t)}]])]
          [:ul
           [:li (item "telegram.svg" "https://t.me/Velotoken")]
           [:li (item "medium.svg" "https://medium.com/@SuperMises")]
           [:li (item "discord.svg" "https://discord.gg/rGnnKTR")]
           [:li (item "github.svg" "https://github.com/velo-finance/velo-protocol")]
           [:li (item "reddit.svg" "https://www.reddit.com/r/ethtrader/comments/kgivpu/velotoken_vlo_audit_shows_high_level_security_and/")]
           ])] 

       [price-section]

       [:div.trading-sidebar
        (letfn [(item [t u]
                  [:a {:href u} [:img {:src (str "/images/trading/" t)}]])]
          [:ul
           [:li (item "uniswap.svg" "https://info.uniswap.org/pair/0x259E558892783fd8941EBBeDa694318C1C3d9263")]
           [:li (item "dextools.svg" "https://www.dextools.io/app/uniswap/pair-explorer/0x259e558892783fd8941ebbeda694318c1c3d9263")]
           [:li (item "coingecko.svg" "https://www.coingecko.com/en/coins/velo-token")]
           [:li (item "coinmarketcap.svg" "https://coinmarketcap.com/currencies/velo-token/")]
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

   ])
