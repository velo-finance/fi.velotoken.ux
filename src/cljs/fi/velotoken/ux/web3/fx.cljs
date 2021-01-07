(ns fi.velotoken.ux.web3.fx
  (:require
   [re-frame.core :refer [reg-fx dispatch]]
   [cljs.core.async :refer [go <!]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [async-error.core :refer-macros [<?]]

   [oops.core :refer [oget ocall]]

   [fi.velotoken.ux.utils :as u]
   [fi.velotoken.ux.events :as events]
   [fi.velotoken.ux.numbers :as numbers]
   [fi.velotoken.ux.web3.bignumber :as bignumber]
   [fi.velotoken.ux.web3.provider :refer [provider connect disconnect]]
   [fi.velotoken.ux.web3.contract.rebaser :as rebaser]
   [fi.velotoken.ux.web3.contract.mises-legacy-pool :as mlp-c]
   [fi.velotoken.ux.web3.contract.uniswap-vlo-eth :as uve-c]
   [fi.velotoken.ux.config :refer [addresses]]
   [fi.velotoken.ux.mises-legacy-pool :as mises-legacy-pool]))

;; Web3 FX

(defmulti web3-method first)

(defmethod web3-method :connect []
  (go
    ;; NOTE: when not using exists? we get an not defined error
    (let [pvdr 
          (u/try-flash! :error "Problem connecting"
                        (<? (connect)))]
      (if-not pvdr 
        (dispatch [::events/web3-ethereum-not-present])
        (do
          ;; register events of interest
          (.on pvdr "accountsChanged" (fn [accounts] (dispatch [::events/web3-accounts-changed (js->clj accounts)])))
          (.on pvdr "chainChanged" (fn [chain-id] (dispatch [::events/web3-chain-changed (js->clj chain-id)])))
          (.on pvdr "networkChanged" (fn [chain-id] (dispatch [::events/web3-network-changed (js->clj chain-id)])))
          (.on pvdr "connect" (fn [connect-info] (dispatch [::events/web3-connected (js->clj connect-info)])))
          ;; RPC Error {:message .., :code .., :data ..}
          (.on pvdr "disconnect" (fn [rpc-error] (dispatch [::events/web3-disconnect (js->clj rpc-error)])))
          (.on pvdr "message" (fn [message] (dispatch [::events/web3-message (js->clj message)])))

          ;; lets try to get the address of the signer
          ;; if we have none, it means we have no account
          ;; connected. If we have one, metamask is connected
          ;; and we initialize with that address
          (try
            (let [signer (ocall pvdr :getSigner)
                  address (<p! (ocall signer :getAddress))]
              (dispatch [::events/web3-initialized {:accounts [address]}]))
            (catch js/Error _
              (dispatch [::events/web3-initialized {:accounts []}]))))))))

(defmethod web3-method :disconnect []
  (go
    ;; NOTE: when not using exists? we get an not defined error
    (u/try-flash! :error "Problem disconnecting"
                  (<? (disconnect))
                  (dispatch [::events/web3-disconnected]))))

(defmethod web3-method :add-token [[_ token-info]]
  (go
    ;; returns true when the token is watched on the wallet. So even when
    ;; "cancel" is pressed when the token is watched, this returns true
    (let [token-watched (<p! (js/ethereum.request (clj->js {:method "wallet_watchAsset",
                                                            :params token-info})))]
      (if token-watched
        (dispatch [::events/web3-add-token-confirmed])
        (dispatch [::events/web3-add-token-rejected])))))

(defmethod web3-method :mises-legacy-pool-data [[_ {:keys [velo-price address]}]]
  (go
    (if-not (provider)
      (dispatch [::events/web3-mises-legacy-pool-data-not-ready])
      (let [mlp (mises-legacy-pool/build velo-price)]
        (dispatch [::events/web3-mises-legacy-pool-data-recv
                   (cond->  {:apy (<! (mises-legacy-pool/annual-perc-yield mlp))
                             :apr (<! (mises-legacy-pool/annual-perc-rate mlp))
                             :total-staked (<! (mises-legacy-pool/total-staked-usd mlp))}
                     (seq address)
                     (assoc :staked-usd (<! (mises-legacy-pool/staked-usd mlp address))
                            :earned-vlo (<! (mises-legacy-pool/earned-vlo mlp address))
                            :balance-uve-lp-tokens
                            (<! (mises-legacy-pool/balance-uve-lp-tokens mlp address))))])))))

#_(web3-method [:mises-legacy-pool-data {:velo-price 0.0167}])
#_(web3-method [:mises-legacy-pool-data {:velo-price 0.0167 :address "0x.."}])

(defmethod web3-method :mises-legacy-pool-stake [[_ {:keys [amount address]}]]
  (go
    (let [mlp-c (mlp-c/build-signer)
          uve-c (uve-c/build-signer)
          ;; tranform the "floaty" amount to a bignumber (^18)
          amount (bignumber/parse-ether amount)
          ;; how much is the mlp allowed to spend of this address
          ;; returned as a bignumber. returned as a bignumber (^18))
          allowance (<p! (uve-c/allowance uve-c address (:mises-legacy-pool addresses)))]
      ;; check if the contract is allowed to tranfer
      ;; amount, if not, pop-up an allowance dialog first.
      (when (ocall allowance :lt amount)
        (u/try-flash! :warning "Approval needed to be able to stake"
                      (<p! (uve-c/approve uve-c (:mises-legacy-pool addresses) (uve-c/max-approval)))))

      ;; if not rejected, we end up at the staking dialog
      (u/try-flash! :error "Problem trying to stake"
                    (<p! (mlp-c/stake mlp-c amount))))))

(defmethod web3-method :mises-legacy-pool-harvest [[_ {:keys [address]}]]
  (go
    (let [mlp-c (mlp-c/build-signer)
          earned (u/<p-float! (mlp-c/earned mlp-c address))]
      (if (zero? earned)
        (dispatch [::events/flash
                   {:type :warning
                    :message  "There is nothing to harvest at this moment"}])
        (u/try-flash! :error "Problem trying harvest rewards"
                      (<p! (mlp-c/get-reward mlp-c)))))))

(defmethod web3-method :mises-legacy-pool-exit [[_ {:keys [address]}]]
  (go
    (let [mlp-c (mlp-c/build-signer)
          balance (u/<p-float! (mlp-c/balance-of mlp-c address))]
      (if (zero? balance)
        (dispatch [::events/flash
                   {:type :warning
                    :message  "Cannot exit when nothing has been staked"}])
        (u/try-flash! :error "Problem exiting pool"
                      (<p! (mlp-c/exit mlp-c)))))))

(defmethod web3-method :velo-rebase-data [[_ _]]
  (go
    (let [rebaser-contract (rebaser/build)
          to-float #(numbers/to-unsafe-float % 18)]

      (dispatch [::events/web3-velo-rebase-data-recv
                 {:velocity-relative (to-float (<p! (rebaser/relative-velocity rebaser-contract)))
                  :last-rebase (numbers/to-number (<p! (rebaser/last-rebase rebaser-contract)))}]))))

#_(dispatch [::events/flash
             {:type :error
              :message  "Error example"}])

#_(dispatch [::events/flash
             {:type :warning
              :message  "Warning example"}])

#_(dispatch [::events/flash
             {:type :notice
              :message  "Notice example"}])

(defmethod web3-method :velo-call-rebase [[_ _]]
  (go
    (let [rebaser-contract (rebaser/build-signer)]
      (u/try-flash! :warning "Problem rebasing"
                    (<p! (rebaser/rebase rebaser-contract)))
      (dispatch [::events/web3-call-rebase-success]))))

(reg-fx :web3 web3-method)

(reg-fx :web3-multiple
        (fn [calls]
          (doseq [c calls]
            (web3-method c))))
