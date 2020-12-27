(ns fi.velotoken.ux.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [reagent-forms.core :refer [bind-fields]]

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
     ]]])

(defn cond-value-sub [subscription placeholder & [{:keys [fmtfn] :or {fmtfn identity}}]]
  (if-let [v @(<su [subscription])] 
    (fmtfn v)
    [:div.connect-wallet 
     [:span.placeholder placeholder]
     [:span.subtext "CONNECT WALLET"]])
  )


(defn cond-value [v placeholder & [{:keys [fmtfn] :or {fmtfn identity}}]]
  (if v 
    (fmtfn v)
    [:div.connect-wallet 
     [:span.placeholder placeholder]
     [:span.subtext "CONNECT WALLET"]]))


(defn rebase-section []
  [:div.rebase-section
   [:div.section
    [:div.title "12H VELOCITY REBASE"]
    [:div.body 
     [:div.gauges.grid.halves
      [:div.gauge.velocity.column
       [:div.title "VELOCITY"]
       [:div.value 
        [cond-value-sub ::subs/token-velocity-relative "0.00%" {:fmtfn frm/perc}] ]]
      [:div.gauge.countdown.column
       [:div.title "COUNTDOWN"]
       [:div.value 
        [cond-value-sub ::subs/last-rebase-countdown "00:00:00"]]]]

     (let [msg (reagent/atom nil)
           messages ["Try again when the countdown has expired"
                     "Well, what to say?"
                     "The countdown is there for a reason mister.."
                     "Aren't we all eager to write history!"
                     "A bit impatient are we?"]
           on-click #(cond
                       (pos? @(<su [::subs/last-rebase-counter])) 
                       (reset! msg (rand-nth messages))
                       :else
                       (do
                         (reset! msg "Are you the one?")
                         (>ev [::events/web3-call-rebase])))]
       [:div.wrapper 
        [:div.rebase-button-section.grid.thirds
         [:div.rocket.n0.column
          [:img {:src "/images/rocket-bg-0.svg"}]]
         [:div.rebase-button-wapper.column
          [:div.rebase-button
           [:a {:on-click on-click} "REBASE"]]]
         [:div.rocket.n1.column
          [:img {:src "/images/rocket-bg-1.svg"}]]]
        [(fn []
           (when msg
             [:div.message @msg]))]]
       )]]])

(defn menu-section []
  (let [eth-inj? @(<su [::subs/ethereum-injected?])
        address @(<su [::subs/web3-account-connected])]
    (when eth-inj?
        [:div#menu
         [:ul 
          [:li.connect 
           [:a {:href "#" :on-click #(>ev [::events/web3-connect])}
                        (or address  "CONNECT")]]
          [:li.add-token 
           [:a {:on-click #(>ev [::events/web3-add-token])} "ADD TOKEN"]]]])))


(defn install-ethereum-compatible-wallet []
  (let [eth-inj? @(<su [::subs/ethereum-injected?])]
    (when-not eth-inj?
        [:div#install-ethereum-compatible-wallet
         [:span 
          "Install an ETH compatible wallet like MetaMask"]])))

(defn flash-message []
  (let [{:keys [message error type]} @(<su [::subs/flash-message])]
    (when message
        [:div#flash {:class (or type "error")}
         [:span 
          message]
         (when error
           [:span.error-message
            error])])))

(defn stake-section [doc selected balance]
  [:div.stake-section
   [:div.seperator]
   [bind-fields  [:div.input-field
    [:div.balance 
     [:a {:on-click #(swap! doc assoc :amount balance)} "BALANCE " [:span balance]]]
    [:input {:field :numeric :id :amount}]
   [:div.grid.halves 
    [:div.cancel-button.column
     [:a {:on-click #(reset! selected nil)} "CANCEL"]]
    [:div.stake-button.column
     [:a {:on-click #(if (pos? (:amount @doc)) 
                       (>ev [::events/web3-mlp-stake (:amount @doc)])
                       (>ev [::events/flash {:type :warning 
                                             :message  "Can only stake positive amount"}]))} "STAKE"]]
    ]] doc]])

(defn yield-farming-section []
  ;; {:apy 0.6332610195499611, 
  ;;  :apr 0.49090847085091577, 
  ;;  :staked-usd 5718.047050293853, 
  ;;  :total-staked 55570.135820930554, 
  ;;  :earned-vlo 24127.77788713968
  ;;  }
  (let [mlp-data @(<su [::subs/mises-legacy-pool-data])]
    [:div.yield-farming-section
     [:div.section
      [:div.title "YIELD FARMING SECTION"]
      [:div.body 
       [:div.gauges
        [:div.gauge.mises-legacy-pool
         [:div.title "MISES LEGACY POOL APY"]
         [:div.value [cond-value (:apy mlp-data) "00.00%" {:fmtfn #(frm/perc (* 100 %))}] ]]

        [:div.gauge.total-deposited
         [:div.title "TOTAL STAKED"]
         [:div.value [cond-value (:total-staked mlp-data) "$00,000.00" {:fmtfn frm/money} ]]]

        [:p "STAKE VLO/ETH UNI-V2, EARN VLO"]

        [:div.seperator]

        (if-not @(<su [::subs/connected?])
          [:div.connect-message "CONNECT WALLET TO STAKE"]
          [:div
           [:div.gauges.grid.halves
            [:div.gauge.velocity.column
             [:div.title "STAKED USD"]
             [:div.value [cond-value (:staked-usd mlp-data) "$00,000.00" {:fmtfn frm/money} ]]]
            [:div.gauge.countdown.column
             [:div.title "$VLO EARNED"]
             [:div.value [cond-value (:earned-vlo mlp-data) "000,000" {:fmtfn frm/si-prefix} ]]]]


           (let [selected (reagent/atom nil)
                 doc (reagent/atom {:amount 0.0})]
             [:div.buttons 

              [(fn []
                 (case @selected
                   :stake [stake-section doc selected (:balance-uve-lp-tokens mlp-data)]
                   nil))]

              [(fn []
                 (if-not @selected 
                   [:div.buttons
                    [:div.staking-button-section.grid.halves
                     [:div.stake-button.column
                      [:a {:on-click #(reset! selected :stake)} "STAKE"]]
                     [:div.unstake-button.column
                      [:a {:on-click #(>ev [::events/web3-mlp-exit])} "EXIT"]]]

                    [:div.harvest-button-section.grid.ones
                     [:div.harvest-button.column
                      [:a {:on-click #(>ev [::events/web3-mlp-harvest])} "HARVEST"]]
                     ]]))]])])]]]]))


(defn main-panel []
  [:div.app-container
     [:div.app-bg]
     [:div.app-content
      ;; informational messages
      [install-ethereum-compatible-wallet]
      [flash-message]

      [menu-section]
      [:div.logo 
       [:img {:src "/images/logo+border.svg"}]]

      [:div.logo-title
       [:span "VELOTOKEN"]]
      
      [:div.main-section

       [:div.social-sidebar
        (letfn [(item [t u]
                  [:a {:href u :target t} [:img {:src (str "/images/socials/" t)}]])]
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
                  [:a {:href u :target t} [:img {:src (str "/images/trading/" t)}]])]
          [:ul
           [:li (item "uniswap.svg" "https://info.uniswap.org/pair/0x259E558892783fd8941EBBeDa694318C1C3d9263")]
           [:li (item "dextools.svg" "https://www.dextools.io/app/uniswap/pair-explorer/0x259e558892783fd8941ebbeda694318c1c3d9263")]
           [:li (item "coingecko.svg" "https://www.coingecko.com/en/coins/velo-token")]
           [:li (item "coinmarketcap.svg" "https://coinmarketcap.com/currencies/velo-token/")]
           ])]]
      
      [rebase-section]
      
      [yield-farming-section]

      [:div#footer]
     ]

   ])
