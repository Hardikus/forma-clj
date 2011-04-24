;; The functions in this namespace allow reprojection of a gridded
;; (lat, lon) dataset at arbitrary resolution into the MODIS
;; sinusoidal grid at arbitrary resolution.

(ns forma.reproject
  (:use cascalog.api      
        [forma.matrix.utils :only (idx->colrow
                                   colrow->idx)]
        [clojure.contrib.generic.math-functions :only (floor)])
  (:require [forma.source.modis :as m]))

(defn bucket
  "Takes a floating-point value and step size, and returns the
  step-sized bucket into which the value falls. For example:

     (index 0.4 1.3)
     ;=> 3

     (index 0.9 1.3)
     ;=> 1"
  [step val]
  (int (floor (* val (/ step)))))

(defn dimensions-at-res
  "returns the <horz, vert> dimensions of a WGS84 grid at the supplied
  spatial resolution."
  [res]
  (map #(quot % res) [360 180]))

;; ### WGS84 -> MODIS Index Mapping
;;
;; Now that we have a method of calculating the wgs84 coordinates of a
;; single MODIS pixel, we're able to consider the problem of
;; generating sampling indices for chunks of MODIS pixels. If we have
;; a dataset in WGS84 gridded at some spatial resolution, to reproject
;; into MODIS we'll need to generate lists that tell us which WGS84
;; indices to sample for the data at each MODIS pixel. (The following
;; assume that WGS84 data is held in a row vector.)
;;
;; In their current version, these functions make the assumption that
;; the WGS84 grid begins at (-90, 0), with columns moving east and
;; rows moving north. This goes against the MODIS convention of
;; beginning at (-180, 90), and moving east and south. Future versions
;; will accomodate arbitrary zero-points.

(defn fit-to-grid
  "Takes a coordinate pair and returns its [row, col] position on a
  WGS84 grid with the supplied spatial resolution and width in
  columns.

 (`fit-to-grid` assumes that the WGS84 grid begins at -90 latitude and
  0 longitude. Columns move east, wrapping around the globe, and rows
  move north.)"
  [ll-res max-width lat lon]
  (let [abs-lon (if (neg? lon) (- lon) lon)
        lon-idx (bucket ll-res abs-lon)
        lat-idx (bucket ll-res (+ lat 90))]
    (vector lat-idx
            (if (neg? lon)
              (- (dec max-width) lon-idx)
              lon-idx))))

(defn wgs84-index
  "takes a modis coordinate at the supplied resolution, and returns
  the index within a row vector of WGS84 data at the supplied
  resolution."
  [m-res ll-res mod-h mod-v sample line]
  {:pre [(m/valid-modis? m-res mod-h mod-v sample line)]}
  (let [[max-width] (dimensions-at-res ll-res)
        [lat lon] (m/modis->latlon m-res mod-h mod-v sample line)
        [row col] (fit-to-grid ll-res max-width lat lon)]
    (colrow->idx max-width col row)))

;; ## MODIS Sampler
;;
;; If a dataset we need for our algorithm happens to be in a
;; projection different from the MODIS products, we need to reproject
;; onto the MODIS grid. NOAA's PREC/L dataset, for example, is
;; arranged in a lat, long grid, at a resolution of 0.5 degrees per
;; pixel.
;;
;; To resample in parallel, we decided to pre-generate a set of array
;; indices, to map from a specific (lat, lon) resolution into some
;; MODIS resolution. When this map in hand, running
;;
;;     (map dataset new-coords)
;;
;; will sample the old dataset into MODIS at the resolution we want.
;;
;; To parellelize this process effectively, we'll generate maps for
;; chunks of pixels at fixed size, rather than for each tile. If we
;; use the tile level, we have to use one mapper for each tile -- at
;; high resolutions, this becomes inefficient. With "chunks", mappers
;; scale with total number of pixels, which scales directly with
;; spatial resolution.
;;
;; We made the decision to take a single MODIS tile id and a month of
;; rain data as dynamic variables, against my earlier idea of
;; pregenerating indices for each chunk, and pairing the rain months
;; up against these. The idea of pregenerating indices was spurred by
;; the thought that the sampling process, applied at the tile level,
;; wouldn't scale well as resolution increased. This is still true;
;; for high resolutions, this function takes on a rather heavy
;; burden. Additionally, with this current method, we generate the
;; sampling indices for every tile & rain-data combination.
;;
;; Still, we decided to stay at the tile level, as the tuple blowup
;; caused by pairing each rain-month with chunks of 24,000 was
;; enormous. Each of these methods requires a cross join between
;; rain-months and some dataset, either tiles or the chunk indices,
;; and a cross join has to funnel through a single reducer. The
;; intermediate data for a chunk-size of 24,000 at 250m resolution
;; would have produced 2TB of intermediate data.
;;
;; If it becomes clear how to send a `(* 1200 1200)` vector out of
;; this function, one way to guarantee efficiency here would be to
;; pregenerate indices, but choose a chunk-size of `(* 1200 1200)`,to
;; produce a single chunk for 1km tiles. 250 meter data, with 16x the
;; pixels, would run at the same speed with 16x the machines.

(defmapcatop [rain-sampler [m-res ll-res chunk-size]]
  ^{:doc "Returns chunks of the indices within a WGS84 array at the
  specified resolution corresponding to MODIS chunks of the supplied
  size, within the tile at the supplied MODIS coordinates."}
  [rain-month mod-h mod-v]
  (let [edge (m/pixels-at-res m-res)
        numpix (#(* % %) edge)
        rdata (vec rain-month)]
    (for [chunk (range (/ numpix chunk-size))
          :let [indexer (partial wgs84-index m-res ll-res mod-h mod-v)
                tpos (partial m/tile-position m-res chunk-size chunk)]]
      (vector chunk
              (map rdata
                   (for [pixel (range chunk-size)]
                     (apply indexer (tpos pixel))))))))
