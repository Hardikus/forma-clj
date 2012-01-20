(ns forma.trends.analysis
  (:use [forma.matrix.utils]
        [clojure.math.numeric-tower :only (sqrt floor abs expt)]
        [forma.trends.filter :only (deseasonalize make-reliable hp-filter)])
  (:require [forma.utils :as utils]
            [incanter.core :as i]
            [incanter.stats :as s]))

(defn element-sum
  "returns a vector of sums of each respective element in a vector of vectors,
  i.e., a vector with the first element being the sum of the first
  elements in each sub-vector."
  [coll]
  (apply (partial map +) coll))

(defn ols-trend
  "returns the OLS trend coefficient from the input vector; an
  intercept is implicitly assumed"
  [v]
  (let [time-step (utils/idx v)]
    (second (:coefs (s/simple-regression v time-step)))))

(defn min-short-trend
  "returns the minimum value of piecewise linear trends of length
  `long-block` on a time series `ts`, after smoothing by moving
  average of window length `short-block`"
  [long-block short-block ts]
  (->> (utils/windowed-map ols-trend long-block ts)
       (utils/moving-average short-block)
       (apply min)))

(defn long-stats
  "returns a list with both the value and t-statistic for the OLS
  trend coefficient for a time series, conditioning on a variable
  number of cofactors"
  [ts & cofactors]
  (let [time-step (utils/idx ts)
        X (if (empty? cofactors)
            (i/matrix time-step)
            (apply i/bind-columns time-step cofactors))]
    (try
      (map second (map (s/linear-model ts X) [:coefs :t-tests]))
      (catch IllegalArgumentException e))))

(defn expt-residuals
  "returns a list of residuals from the linear regression of `y` on
  `X`, raised to `power`"
  [y X power]
  (map #(expt % power) (:residuals (s/linear-model y X))))

(defn first-order-conditions
  "returns a matrix with residual weighted cofactors (incl. constant
  vector) and deviations from mean; corresponds to first-order
  conditions $f_t$ in the following reference: Hansen, B. (1992)
  Testing for Parameter Instability in Linear Models, Journal for
  Policy Modeling, 14(4), pp. 517-533"
  [y & x]
  (let [time-step (utils/idx y)
        X (if (empty? x)
            (i/matrix time-step)
            (apply i/bind-columns time-step x))
        [resid sq-resid] (map (partial expt-residuals y X) [1 2])]
    (vector (map * resid X)
            (map * resid (repeat (count y) 1))
            (map #(- % (utils/average sq-resid)) sq-resid))))

(defn hansen-mats
  "returns the matrices of element-wise sums of (1) the first-order
  conditions, and (2) the cumulative first-order conditions.  This is
  only an intermediate step in the calculation of the Hansen (1992)
  test for parameter instability in linear models.
  TODO: remove redundancy, keep readability"
  [y & x]
  (let [foc (apply first-order-conditions y x)]
    [(element-sum (map outer-product (transpose foc)))
     (element-sum (map outer-product (transpose (map i/cumulative-sum foc))))]))

(defn hansen-stat
  "returns the Hansen (1992) test statistic; number of first-order
  conditions `num-foc`accounts for the demeaned residuals, intercept,
  and time-step introduced by `first-order-conditions`"
  [y & x]
  (let [num-foc (+ (count x) 3) 
        [foc cumul-foc] (apply hansen-mats y x)]
    (i/trace
     (i/mmult
      (i/solve (i/matrix (map #(* (count y) %) foc) num-foc))
      (i/matrix cumul-foc num-foc)))))
