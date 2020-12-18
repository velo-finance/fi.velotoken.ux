(ns fi.velotoken.ux.coingecko
  (:require [cljs.core.async :refer [go <!]]
            [cljs-http.client :as http]
            [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :as cske]))

(def eth-token-price-url "https://api.coingecko.com/api/v3/simple/token_price/ethereum")

(defn get-token-price []
  (go 
    (let [r (<! (http/get eth-token-price-url {:with-credentials? false
                                               :query-params 
                                               {"contract_addresses" "0x98ad9b32dd10f8d8486927d846d4df8baf39abe2"
                                                "vs_currencies" "USD"
                                                "include_24hr_vol" true 
                                                "include_24hr_change" true 
                                                "include_last_updated_at" true
                                                }}))]
      (cske/transform-keys csk/->kebab-case-keyword (-> r :body first second)))))

(get-token-price)
;; https://api.coingecko.com/api/v3/simple/token_price/ethereum?contract_addresses=0x98ad9b32dd10f8d8486927d846d4df8baf39abe2&vs_currencies=USD&include_24hr_vol=true&include_24hr_change=true&include_last_updated_at=true

;; {
;;   "0x98ad9b32dd10f8d8486927d846d4df8baf39abe2": {
;;     "usd": 0.0172767,
;;     "usd_24h_vol": 128039.50495433915,
;;     "usd_24h_change": -0.5856836628701415,
;;     "last_updated_at": 1608310700
;;   }
;; }
