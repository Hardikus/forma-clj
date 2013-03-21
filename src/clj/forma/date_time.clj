;; This namespace allows for conversion of dates into integer time
;; periods, as measured from some reference date. This allows for
;; proper temporal comparison of two unrelated datasets.

(ns forma.date-time
  (:use [clj-time.core :only (date-time month year)]
        [forma.matrix.utils :only (sparse-expander)])
  (:require [clj-time.core :as time]
            [clj-time.format :as f]))

(defn parse
  "Returns a `DateTime` object generated by parsing the supplied
  string `s` using the supplied format. Options for `format` can be
  viewed with a call to `(clj-time.format/show-formatters)`."
  [s format]
  (->> s (f/parse (f/formatters format))))

(defn unparse
  "Returns a string generated by unparsing the supplied `DateTime`
  object using the supplied format. Options for `format` can be viewed
  with a call to `(clj-time.format/show-formatters)`."
  [dt format]
  (->> dt (f/unparse (f/formatters format))))

(defn convert
  "Converts the supplied string `s` between the two supplied
  formats. Options for each format keyword can be viewed with a call
  to `(clj-time.format/show-formatters)`."
  [s from-format to-format]
  (-> s
      (parse from-format)
      (unparse to-format)))

(defn within-dates?
  "Returns true if the supplied datestring `dt` falls within the dates
  described by start and start end (exclusive)`, false otherwise. the
  `:format` keyword argument can be used to specify the format of the
  datestrings. Options for `:format` can be viewed with a call
  to `(clj-time.format/show-formatters)`.

  For example:
    (within? \"2005-12-01\" \"2011-01-02\" \"2011-01-01\")
    ;=> true"
  [start end dt & {:keys [format]
                   :or {format :year-month-day}}]
  (let [[start end dt] (map #(parse % format) [start end dt])]
    (time/within? (time/interval start end) dt)))

;; ### Reference Time
;;
;; In developing the time period conversion functions, we noticed that
;; as long as the time period remained consistent from dataset to
;; dataset, the ability to 0-index each dataset became irrelevant.
;; Our choice of the [Unix epoch](http://goo.gl/KyuLr) as a common
;; reference date ensures that, regardless of the date on which the
;; specific MODIS product became active, datasets at the same temporal
;; resolution will match up.

;; ## Date -> Time Period Conversion
;;
;; For NASA's composite products, MODIS data is composited into either
;; monthly, 16-day, or 8-day intervals. Each of these scales begins
;; counting on January 1st. Monthly datasets begin counting on the
;; first of each month, while the others mark off blocks of 16 or 8
;; ordinal days from the beginning of the year.
;;
;; (It's important to note that time periods are NOT measured from the
;; activation date of a specific product. The first available NDVI
;; 16-day composite, for example, has a beginning date of February
;; 18th 2001, or ordinal day 49, the 1st day of the 4th period as
;; measured from January 1st. With our reference of January 1st, This
;; dataset will receive an index of 3, and will match up with any
;; other data that falls within that 16-day period. This provides
;; further validation for our choice of January 1st, 2000 as a
;; consistent reference point.
;;
;; Additionally, it should be noted that the final dataset is never
;; full; the 23rd 16-day dataset of the year holds data for either 13
;; or 14 days, depending on leap year.)
;;
;; So! To construct a date, the user supplies a number of date
;; "pieces", ranging from year down to microsecond. To compare two
;; times on ordinal day, we use the clj-time library to calculate the
;; interval between a given date and January 1st of the year in which
;; the date occurs.

(defn ordinal
  "Returns the ordinal day index of a given date."
  [dt]
  (time/in-days
   (time/interval (time/date-time (year dt))
                  dt)))

;; the `ordinal` function complements the other "date-piece" functions
;; supplied by the clj-time library; day, month, year, and the others
;; each allow for extraction of that particular element of a date. We
;; define the delta function to allow us to take the difference of a
;; specific date-piece between two dates.

(defn delta [f a b] (- (f b) (f a)))

;; For example,
;;
;;     (delta ordinal a b)
;;
;; returns the difference between the ordinal day values of the two
;; supplied dates. (Note that `(partial delta ordinal)` ignores the
;; year value of each of the dates. That function returns the same
;; value for a = Jan 1st, 2000, b = Feb 25th, 2000 and b = Feb 25th,
;; 2002.)

(defn per-year
  "Calculates how many periods of the given span of supplied units can
  be found in a year. Includes the final period, even if that period
  isn't full."
  [unit span]
  (let [m {ordinal 365 month 12}]
    (-> (m unit) (/ span) Math/ceil int)))

(defn delta-periods
  "Calculates the difference between the supplied start and end dates
  in span-sized groups of unit (months or ordinals). [unit span] could be
  [ordinal 16], for example."
  [unit span start end]
  (let [[years units] (map #(delta % start end) [time/year unit])]
    (+ (* years (per-year unit span))
       (quot units span))))

;; The following partial functions take care of the issue with delta
;; above, by calculating the span in periods across years. (Note that
;; these functions only work for MODIS products using regular
;; production. All terra products meet this restriction -- the aqua
;; products use phased production with 16- and 8-day temporal
;; resolution, so time periods begin on January 9th.)

(def months (partial delta-periods month 1))
(def sixteens (partial delta-periods ordinal 16))
(def eights (partial delta-periods ordinal 8))

(defn periodize
  "Converts the supplied `org.joda.time.DateTime` object into a
reference time interval at the supplied temporal
resolution. `DateTime` objects can be created with `clj-time`'s
`date-time` function."
  [temporal-res date]
  (let [converter (case temporal-res
                        "32" months
                        "16" sixteens
                        "8" eights)]
    (converter (time/epoch) date)))

(defn datetime->period
  "Converts a formatted datestring, into an integer time period at the
  supplied temporal resolution. The default format is
  `:year-month-day`, or `YYYY-MM-DD`."
  ([res datestring]
     (datetime->period res datestring :year-month-day))
  ([res datestring format]
     (let [date (parse datestring format)]
       (periodize res date))))

(defn period->datetime
  "Converts an integer time period at the supplied temporal resolution
  into a formatted datestring. The default format is
  `:year-month-day`, or `YYYY-MM-DD`."
  ([res period]
     (period->datetime res period :year-month-day))
  ([res period format]
     (let [[unit span f] (case res
                               "32" [month 1 time/months]
                               "16" [ordinal 16 time/days]
                               "8" [ordinal 8 time/days]
                               "1" [ordinal 1 time/days])
           [yrs pds] ((juxt quot mod) period (per-year unit span))]
       (-> (time/epoch)
           (time/plus (time/years yrs) (f (* span pds)))
           (unparse format)))))

(defn beginning
  "Accepts a string representation of a date-time object, and returns
a new string object representing the absolute beginning of time period
in which `string` lies (according to the supplied resolution, `res`)."
  ([res string]
     (beginning res string :year-month-day))
  ([res string format]
     (let [period (datetime->period res string format)]
       (period->datetime res period format))))

(defn diff-in-days
  "Returns the difference in ordinal days between the supplied date
  strings."
  [one-date two-date]
  (->> [one-date two-date]
       (map #(parse % :year-month-day))
       (apply time/interval)
       (time/in-days)))

(defn date-offset
  "Returns the difference in ordinal days between the beginnings of
  the two supplied periods. For example:

 (date-offset \"16\" 1 \"32\" 1) => 15"
  [from-res from-period to-res to-period]
  (diff-in-days (period->datetime from-res from-period)
                (period->datetime to-res to-period)))

(defn period-span
  "Returns the length in days of the supplied period at the supplied
  resolution. For example:

  (period-span \"32\" 11) => 31"
  [t-res pd]
  (->>  [pd (inc pd)]
        (map (partial period->datetime t-res))
        (apply diff-in-days)))

(defn shift-resolution
  "Takes a period at `from-res`, returns the corresponding period at
  `to-res`."
  [from-res to-res period]
  (->> period
       (period->datetime from-res)
       (datetime->period to-res)))

(defn current-period
  "Returns the current time period for the supplied resolution. For
  example:

    (current-period \"32\")
    => 495 ;; (on April 27, 2011, this function's birthday!)"
  [res]
  (periodize res (time/now)))

(defn relative-period
  "convert periods (string) to an integer value that is relative to
  the start period.

  Example usage:
  (relative-period \"32\" 391 [\"2005-02-01\" \"2005-03-01\"])
  => (30 31)"
  [t-res start period-seq]
  (map (comp #(long (- % start))
             (partial datetime->period t-res))
       period-seq))

(defn msecs-from-epoch
  "Total number of milliseconds passed since January 1st, 1970 (the
  Unix epoch)."
  [date]
  (time/in-msecs (time/interval (time/epoch) date)))

(defn monthly-msec-range
  "Returns a sequence of msec values corresponding to each time period
  at monthly resolution from the supplied `start-date` (inclusive) to
  the `end-date` (exclusive). Both inputs must be date-time objects
  created with `clj-time.core/date-time`."
  [start-date end-date]
  (let [total-months (inc (delta-periods month 1 start-date end-date))]
    (for [month-offset (range total-months)]
      (msecs-from-epoch (time/plus start-date
                                   (time/months month-offset))))))

(defn res->period-count
  [res]
  (apply per-year
         (case res
           "32" [month 1]
           "16" [ordinal 16]
           "8" [ordinal 8]
           "1" [ordinal 1])))

(defn convert-period-res
  "Convert a period from in-res to corresponding period at out-res.

   By converting a period to a date, we get the first date within a
   period. Converting date to period, we get the period in which that
   first date falls, at the new resolution."
  [res-in res-out period]
  (->> (period->datetime res-in period)
       (datetime->period res-out)))

(defn date-str->vec-idx
  "Return the index of a vector that corresponds to a given date.
   Returns `nil` if date does not correspond to a period in the vector.

   Note: Because this function uses the period and date conversion
         functions, it will snap any date to the initial date of the
         period that contains it, given a temporal resolution.

   Usage:
     (date-str->vec-idx \"16\" \"2000-01-01\" [1 2 3] \"2000-01-18\")
     => 1

     (date-str->vec-idx \"16\" \"2000-01-01\" [1 2 3] \"2000-12-01\")
     => nil"
  [t-res start-dt v dt]
  (let [start-idx (datetime->period t-res start-dt)
        idx (- (datetime->period t-res dt) start-idx)
        length (count v)]
    (if (and (<= idx (dec length))
             (>= idx 0))
      idx
      nil)))

(defn get-val-at-date
  "Returns the value of a vector at the index corresponding to a given
   date. If there is no corresponding index (e.g. date comes before
   start of series or after the end), returns `nil` by default.

   Optional arguments:
     `:out-of-bounds-val`: provide an alternative value to the `nil'
     default if the date falls outside of the series.

     `:out-of-bounds-idx`: choose a value from the series at the index
     given, as an alternative to the default `nil' if the date falls
     outside the series.

   Note: Because this function uses the period and date conversion
         functions, it will snap any date to the initial date of the
         period that contains it, given a temporal resolution.

   Usage:
     (date-str->vec-idx \"16\" \"2000-01-01\" [2 4 6] \"2000-01-18\")
     => 4
   
     (date-str->vec-idx \"16\" \"2000-01-01\" [2 4 6] \"2012-05-01\")
     => nil"
  [t-res start-dt v dt & {:keys [out-of-bounds-val out-of-bounds-idx]}]
  (let [idx (date-str->vec-idx t-res start-dt v dt)]
    (if idx
      (nth v idx)
      (or out-of-bounds-val
          (if out-of-bounds-idx
            (nth v out-of-bounds-idx))
          nil))))

(defn sorted-ts
  "Accepts a map with date keys and time series values, and returns a
  vector with the values appropriately sorted.

  Example:
    (sorted-ts {:2005-12-31 3 :2006-08-21 1}) => (3 1)"
  [m]
  (vals (into (sorted-map) m)))

(defn key->period
  "Convert a date keyword to a period.

   Usage:
     (key->period \"16\" :2006-01-01)
     ;=> 827"
  [t-res k]
  (datetime->period t-res (name k)))

(defn period->key
  "Convert a period to a date keyword.

   Usage:
     (period->key \"16\" 827)
     ;=> :2006-01-01"
  [t-res pd]
  (keyword (period->datetime t-res pd)))

(defn key-span
  "Returns a list of date keys, beginning and end date inclusive.  The
  first and last keys represent the periods that start just before the
  supplied boundary dates.

  Example:
    (key-span :2005-12-31 :2006-01-18 \"16\")
    => (:2005-12-19 :2006-01-01 :2006-01-17)"
  [init-key end-key t-res]
  (let [init-idx (key->period t-res init-key)
        end-idx  (inc (key->period t-res end-key))]
    (map (partial period->key t-res)
         (range init-idx end-idx))))

(defn same-len?
  "Checks whether two collections have the same number of elements.

   Usage:
     (same-len? [1 2 3] [4 5 6])
     ;=> true"
  [coll1 coll2]
  (= (count coll1) (count coll2)))

(defn all-unique?
  "Checks whether all the elements in `coll` are unique.

   Usage:
     (all-unique? [1 2 3])
     ;=> true

     (all-unique? [1 1 2])
     ;=> false"
  [coll]
  (same-len? coll (set coll)))

(defn inc-eq?
  "Checks whether the first integer immediately preceeds the second one.

   Usage:
     (inc-eq? [1 2]) => true
     (inc-eq? [0 2]) => false
     (inc-eq? 1 2) => true
     (inc-eq? 0 2) => false"
  ([[a b]]
     (inc-eq? a b))
  ([a b]
     (= (inc a) b)))

(defn consecutive?
  "Checks whether a collection of date keys is consecutive (i.e. has no gaps or repetition).

   Usage:
     (consecutive? \"16\" [:2006-01-01 :2006-01-17]) => true
     (consecutive? \"16\" [:2006-01-01 :2006-01-01]) => false
     (consecutive? \"16\" [:2006-01-01 :2007-21-31]) => false"
  [t-res date-coll]
  (let [pds (map (partial key->period t-res) (sort date-coll))
        tuples (partition 2 1 pds)]
    (every? true? (map inc-eq? tuples))))

(defn ts-vec->ts-map
  "Accepts a date key (a la :2012-01-01) for the first element in a
   time series, plus a temporal resolution and a collection. Returns a
   map containing dates as keys and series elements as values.

   Usage:
     (ts-vec->ts-map :2006-01-01 \"16\" [1 2 3])
     ;=> {:2006-01-01 1 :2006-01-17 2 :2006-02-02 3}"
  [init-date-key t-res coll]
  (let [init (key->period t-res init-date-key)
        end-key  (period->key t-res (+ init (count coll)))]
    (zipmap (key-span init-date-key end-key t-res) coll)))

(defn ts-map->ts-vec
  "Accepts a temporal resolution, time series map, and a nodata value.
   Returns the corresponding time series as a vector.

   If :consecutive is false (default), holes in the time series will be
   filled with the nodata value. If :consecutive is true, holes in the
   time series will trip the precondition.

  Usage:
    (ts-map->ts-vec \"16\" {:2006-01-01 1 :2006-01-17 2 :2006-02-02 3} -9999.0)
    ;=> [1 2 3]

    (ts-map->ts-vec \"16\" {:2006-01-01 1 :2006-01-17 2 :2006-02-18 3} -9999.0)
    ;=> [1 2 -9999.0 3]

    (ts-map->ts-vec \"16\" {:2006-01-01 1 :2006-01-17 2 :2006-02-18 3} -9999.0
    :consecutive true)
    ;=> throws AssertionError"
  [t-res m nodata & {:keys [consecutive] :or {consecutive false}}]
  {:pre [(or (false? consecutive)
             (consecutive? t-res (keys m)))]}
  (let [date-ks (sort (keys m))
        pds-vals (for [k date-ks]
                   [(key->period t-res k) (k m)])]
    (sparse-expander nodata pds-vals)))

(defn get-ts-map-start-idx
  "Given a map of dates and values, return the first date converted to a period.

   Usage:
     (get-ts-map-start-idx \"16\" {:2006-01-01 1 :2006-01-17 2 :2006-02-18 3})
     ;=> 828"
  [t-res ts-map]
  (key->period t-res (first (sort (keys ts-map)))))

(defn overlap?
  "Checks for collisions between keys in provided maps.

   Usage:
     (overlap? {:a 1} {:b 2})
     ;=> false

     (overlap? {:a 1 :b 2} {:b 3})
     ;=> true

     (overlap? {:2006-01-01 1 :2006-01-17 2} {2006-01-17 30})
     ;=> true"
  [m1 m2]
  (let [ks-m1 (set (keys m1))
        ks-m2 (set (keys m2))]
    (not (empty? (clojure.set/intersection ks-m1 ks-m2)))))

(defn merge-ts
  "Merges (ostensibly) two time series hashmaps. With :update true,
   key collisions are ok and updates will occur. Otherwise, key
   collisions will trip up the precondition.

   Note that this function only merges hashmaps, and does not ensure
   that the result is a complete time series. It doesn't even check
   that keys are dates. So there may be holes in the time series that
   comes out, if it's even a time series at all. Use `ts-map->ts-vec`
   to convert to a vector with holes filled with nodata values.

   Usage:
     (merge-ts {:2006-01-01 1 :2006-01-17 2} {:2006-02-02 3})
     ;=> {:2006-01-01 1 :2006-01-17 2 :2006-02-02 3}

     (merge-ts {:2006-01-01 1 :2006-01-17 2} {2006-01-17 30} :update true)
     ;=> {:2006-01-01 1 :2006-01-17 30}

     (merge-ts {:2006-01-01 1 :2006-01-17 2} {2006-01-17 30})
     ;=> throws AssertionError for tripping the precondition

     (merge-ts {:a 1 :b 2} {:c 3})
     ;=> {:a 1 :b 2 :c 3} ;; this function works for any hashmap."
  [master new & {:keys [update] :or {update false}}]
  {:pre [(or (true? update)
             (not (overlap? master new)))]}
  (into master new))
