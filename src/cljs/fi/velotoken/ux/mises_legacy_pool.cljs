(ns fi.velotoken.ux.mises-legacy-pool
  (:require 
    [cljs.core.async :refer [go]]
    [cljs.core.async.interop :refer-macros [<p!]]

    [fi.velotoken.ux.web3.contract.velo-token :as vlo-c]
    [fi.velotoken.ux.web3.contract.uniswap-vlo-eth :as uve-c]
    [fi.velotoken.ux.web3.contract.mises-legacy-pool :as mlp-c]

    [fi.velotoken.ux.config :refer [addresses]]

    [fi.velotoken.ux.numbers :refer [to-unsafe-float]]
    [fi.velotoken.ux.utils :refer-macros [<p-float!]]))

;; Abstraction over the Mises Legacy Pool, brings together:
;;   a. MisesLegacyPool
;;   b. VLO-ETH UniSwapPool
;;   c. VeloToken

;; To display correct information on the Mises Legacy Pool

;; GLOBAL STATS

;; To calculate APR, and APYs we need to use the rewardRate on the mlp-c
;; contract. This is VLO/s rewarded to the total sum of participants.

;; APR calculationas are baed on a stake amount. In this case 100K

;; * TOTAL LIQUIDITY VLO-ETH UNISWAP POOL

;; get balance of vlo eth uniswap pool
;; vlo-balance = vlo-c.balanceOf(uve-c.address)

;; since uniswap pools are 50% balanced out, we can determine total-liquidity
;; by taking
;;
;; total-liquidity = 2 * vlo-balance * vlo-usd-price

;; * TOTAL STAKED
;; 
;; total staked is the balance of the mlp, divided by totalSupply of the
;; vlo-eth unswap pool times the total liquidity 

;; * MISES LEGACY APR
;;
;; APR is a projection of what someone would earn when staking 100K. In our
;; case.
;; 
;;     reward-per-second = ( 100K / Total Staked ) * mlp.rewardRate
;;
;; since this is annual
;;
;;     reward-per-year = reward-per-second * 365 * 24 * 60 * 60
;;
;; and then in usd
;;
;;     reward-per-year-usd = reward-per-year * vlo-price
;;
;; which would give us an APR of:
;;
;;     APR = reward-per-year-usd / 100K 
;;   
;; * MISES LEGACY APY
;; 
;; The APY is a compounded number, lets assume we compound daily. So we can
;; use the reward-per-second to calculate a daily reward.
;;
;;     reward-per-day = reward-per-second * 24 * 60 * 60
;;
;; now the daily percentage yield is:
;;
;;     DPR = reward-per-day / 100K
;;
;; wich makes the APY
;;
;;     APY = (1+DPR)^365 

;; USER STATS
;; * STAKED USD
;;  
;;  we get the balance in the 
;;  staked-usd = (mlp-c.balance / mlp-c.totalSupply) * total-staked-usd
;;  
;; * VLO EARNED
;;
;;   vlo-earned = mlp.earned(address) * vlo-c.velosScalingFactor

;; * USD EARNED
;;
;;   usd-earned = vlo-earned * vlo-price-usd

;; MisesLegacyPool
;;    velo
;;    erc20_token => UniSwap Pool
;;                     token0 => VLO Token
;;                     token1 => wETH Token

;; Now we make the following assumption on the

;; which is linked to the LP Pool 0x259e558892783fd8941ebbeda694318c1c3d9263
;; (:uni-vlo-eth addresses)

;; to determine the value staked, we need to determine the share this person
;; has in the liquidity pool.

;; the LP pools has 2 balances, on balance in VLO and one balance in ETH
;; we can use the balance in VLO * 2 to determine the total value in the 
;; LP Pool. Now value-per-lp-token  = total-value / totalSupply
;;
;; smp.balanceOf(address) * usd-value-per-lp-token = staked

;; vlo-earned = mlp.earned * vlo_token.scalingFactor
;; vlo-earned-usd = vlo-earned * vlo-usd-price
;;
(defn build [vlo-price-usd]
  {:vlo-price-usd vlo-price-usd
   :vlo-c (vlo-c/build)
   :uve-c (uve-c/build)
   :mlp-c (mlp-c/build)})

(defn total-liquidity-usd 
  "Total amount of liquidity in the UNI-V2 VLO ETH Pool" 
  [{:keys [vlo-price-usd vlo-c]}]
  (go 
    (let [vlo-balance (<p-float! (vlo-c/balance-of vlo-c (:uni-vlo-eth addresses)))]
      (* 2 vlo-balance vlo-price-usd))))

;; double checked an working
#_ (go (prn (<! (total-liquidity-usd (build 0.0067)))))

(defn total-staked-usd
  "Total amount staked in the Mises Legacy Pool"
  [{:keys [uve-c] :as c}]
  (go 
    (let [total-supply (<p-float! (uve-c/total-supply uve-c))
          balance (<p-float! (uve-c/balance-of uve-c (:mises-legacy-pool addresses)))]
      (if (zero? total-supply)
        0.0
        (* (/ balance total-supply) 
           (<! (total-liquidity-usd c)))))))

#_ (go (prn (<! (total-staked-usd (build 0.0067)))))

(defn x-perc-rate [{:keys [vlo-price-usd uve-c mlp-c] :as c} period-in-seconds]
  (go 
    (let [total-staked (<! (total-staked-usd c))
          ;; calculate reward rate when staking 100K extra
          ;; in the pool
          reward-rate-vlo (<p-float! (mlp-c/reward-rate mlp-c))
          reward-rate-vlo (* (/ 100000 (+ total-staked 100000))
                             reward-rate-vlo)]
      (/ (* reward-rate-vlo vlo-price-usd period-in-seconds)
         100000))))

#_ (go (prn (<! (x-perc-rate (build 0.0067) (* 365 24 60 60)))))

(defn daily-perc-rate [c]
  (x-perc-rate c (* 24 60 60)))

#_ (go (prn (<! (daily-perc-rate (build 0.0067)))))

(defn annual-perc-rate [c]
  (x-perc-rate c (* 365 24 60 60)))

#_ (go (prn (<! (annual-perc-rate (build 0.0067)))))

(defn annual-perc-yield [c]
  (go
    (let [dpr (<! (daily-perc-rate c))]
       (- (Math/pow (+ 1.0 dpr) 365) 1))))

#_ (go (prn (<! (annual-perc-yield (build 0.0167)))))

(defn staked-usd 
  "the amount of usd this address has staked in the pool" 
  [{:keys [mlp-c] :as c} address]
  (go
    (let [total-staked (<! (total-staked-usd c))
          balance (<p-float! (mlp-c/balance-of mlp-c address))
          total-supply (<p-float! (mlp-c/total-supply mlp-c))]
      (prn total-staked balance total-supply)
      (if (zero? total-supply)
        0.0
        (* (/ balance total-supply) total-staked)))))

;; 11479390
#_ (go (prn (<! (staked-usd (build 0.0167) "0x..."))))

(defn vlo-earned [{:keys [mlp-c vlo-c] :as c} address]
  (go 
    (let [earned (<p-float! (mlp-c/earned mlp-c address))
          scaling-factor (<p-float! (vlo-c/scaling-factor vlo-c))]
      (* earned scaling-factor))))

#_ (go (prn (<! (vlo-earned (build 0.0167) "0x..."))))

