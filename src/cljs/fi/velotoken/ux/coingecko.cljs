(ns fi.velotoken.ux.coingecko
  (:require [cljs.core.async :refer [go <!]]
            [cljs-http.client :as http]
            [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :as cske]
            [fi.velotoken.ux.config :refer [addresses]]
            ))

(def eth-token-price-url "https://api.coingecko.com/api/v3/simple/token_price/ethereum")

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
                                               {"contract_addresses" (:velo-token addresses)
                                                "vs_currencies" "USD"
                                                "include_24hr_vol" true 
                                                "include_24hr_change" true 
                                                "include_last_updated_at" true
                                                }}))]
      (cske/transform-keys csk/->kebab-case-keyword (-> r :body first second)))))

#_ (go (prn  (<! (get-token-price))))
