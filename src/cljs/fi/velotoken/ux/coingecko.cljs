(ns fi.velotoken.ux.coingecko
  (:require [cljs.core.async :refer [go <!]]
            [cljs-http.client :as http]
            [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :as cske]
            [fi.velotoken.ux.config :refer [addresses]]
            ))

#_ (def eth-token-price-url "https://api.coingecko.com/api/v3/simple/token_price/ethereum")

(def eth-token-price-url "https://api.coingecko.com/api/v3/coins/velo-token")

(defn get-token-price 
  "fetches token price of contract address from coingecko api

    {:usd 0.01493716, 
     :usd-24h-vol 124252.50395066236, 
     :usd-24h-change -31.94642341461614, 
     :last-updated-at 1608473539}
  " 
  []
  (go 
    (let [r (<! (http/get eth-token-price-url {:with-credentials? false
                                               :query-params 
                                               {
                                                "localization" false
                                                "tickers" false
                                                "community_data" false
                                                "developer_data" false
                                                "sparkline" false
                                                }
                                               }))
          c (cske/transform-keys csk/->kebab-case-keyword (-> r :body ))
          market-data (-> c :market-data)]
       {
        :usd (-> market-data :current-price :usd)
        :usd-24h-vol (-> market-data :total-volume :usd)
        :usd-24h-low (-> market-data :low-24h :usd)
        :usd-24h-high (-> market-data :high-24h :usd)
        :24h-change (-> market-data :price-change-percentage-24h)
        :7d-change (-> market-data :price-change-percentage-7d)
        :total-supply (-> market-data :total-supply)
        :market-cap (* (-> market-data :current-price :usd)
                       (-> market-data :total-supply))
       }
      )))

#_ (go (prn  (<! (get-token-price))))
