(ns fi.velotoken.ux.web3.provider
  (:require
    [cljs.core.async :refer [go]]
    [cljs.core.async.interop :refer [<p!]]
    [async-error.core :refer-macros [go-try]]
    [oops.core :refer [ocall]]
    ["ethers" :as ethers]
    ["web3modal" :as w3m :default Web3Modal]
    ["@walletconnect/web3-provider" :default WalletConnectProvider]
    ))

(defonce ^:dynamic *provider* (atom nil))

(def provider-options
  {:walletconnect 
   {:package WalletConnectProvider, 
    :options 
    {:infuraId "663e21b048c2434bb01f364537fc4706" 
     }}})

(defn provider []
  (when @*provider*
    (let [web3-provider (. ethers/providers -Web3Provider)]
      (web3-provider. @*provider*))))

(defn connect []
  (go-try 
    (let [modal (Web3Modal. 
                   (clj->js {:providerOptions provider-options
                             :disableInjectedProvider false
                             :cacheProvider false
                             :theme "dark"
                             }))]
      (try 
        (reset! *provider* (<p! (ocall modal :connect)))
        (provider)
        (catch js/Error e
          ;; NOTE: after reading web3modal code, noticed there
          ;; is no way to get to the specific provider error.
          ;; https://github.com/Web3Modal/web3modal/blob/master/src/core/index.tsx#L71
          ;; maybe this changes in the future.
          (reset! *provider* nil)
          (throw (ex-info "Web3Modal connect error" {:type ::connect} (ex-cause e))))))))

(defn disconnect []
  (go-try 
    (ocall @*provider* :?disconnect)
    (reset! *provider* nil)))


#_ (prn "BD" (.-cachedProvider (Web3Modal. (clj->js {:providerOptions  provider-options}))))
#_ (connect)



