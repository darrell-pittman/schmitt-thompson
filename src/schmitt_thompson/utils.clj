(ns schmitt-thompson.utils)

(defn filter-map [m entry-fn pred]
  (into {} (filter (comp pred entry-fn) m)))

(defn filter-map-by-val [m pred]
  (filter-map m val pred))

(defn filter-map-by-key [m pred]
  (filter-map m key pred))
