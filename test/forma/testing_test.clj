(ns forma.testing-test
  (:use forma.testing
        forma.midje
        cascalog.api
        midje.sweet)
  (:require [cascalog.vars :as v]
            [clojure.contrib.io :as io]))

(fact
  "the development resources path must exist! I don't want to pull
this out of cake or lein, so I've hardcoded it into
`dev-resources-subdir`. I'm counting on this test to fail if the
directory moves."
  (-> (dev-path) io/file .exists) => true?)

(fact?<- "tuples->string testing."
         [["(1)(2)(3)"]] [?str] ([1 2 3] ?a) (tuples->string ?a :> ?str))

;; Tests for cascalog midje stuff.
(defn whoop [x] [[x]])
(defn bang [x y] [[x y]])

(defn my-query [x y z]
  (let [foo (whoop x)
        bar (bang y z)]
    (<- [?a ?b]
        (foo ?a)
        (bar ?a ?b))))

(defn a-query [x] (<- [?a] (x ?a)))

(fact (whoop :a) => 10
  (provided (whoop :a) => 10)
  (against-background (whoop :a) => 2))

(against-background [(whoop :a) => 10]
  (fact (whoop :a) => 10))

;; Similar to clojure.test's "are".
(tabular
 (fact?- ?res (apply ?func ?args))
 ?res    ?func    ?args
 [[3 5]] my-query [3 3 5]
 [[1]]   a-query  [[[1]]])

(let [some-seq [[10]]]
  (fact?<- some-seq
           [?a]
           ((whoop :a) ?a)
           (provided (whoop :a) => [[10]])))

(let [result-seq [[3 5]]]
  "Showing that we can draw from the background."
  (fact?- result-seq (my-query .a. .a. .b.)
          [[3 10]] (my-query .a. .a. .c.)
          (against-background
            (whoop .a.) => [[3]]
            (bang .a. .b.) => [[3 5]]
            (bang .a. .c.) => [[3 10]])))

(fact?- "the first query pulls from the stuff defined in
 against-background down below."
        
        [[12 15]] (my-query .a. .a. .b.)

        "The provided block applies to this query..."
        [[100 2]] (my-query .a. .a. .b.)
        (provided (whoop .a.) => [[100]]
                  (bang .a. .b.) => [[100 2]])

        "And, again, drawing from the background."
        [] (my-query .a. .d. .e.)

        (against-background
          (whoop .a.) => [[12]]
          (bang .a. .b.) => [[12 15]]
          (bang .d. .e.) => [[10 15]]))

(fact?<- :info "the provided and background clauses work at the end of
          fact?<- as well."
         [[10]]
         [?a]
         ((whoop) ?a)
         (provided (whoop) => [[10]]))
