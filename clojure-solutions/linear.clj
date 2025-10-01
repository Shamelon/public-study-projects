(defn binOp [f] (fn [v1 v2] (mapv f v1 v2)))

(def v+ (binOp +))
(def v- (binOp -))
(def v* (binOp *))
(def vd (binOp /))

(defn scalar [v1 v2] (apply + (mapv * v1 v2)))
(defn vect [v1 v2] (mapv #(-
                            (* (get-in v1 [(mod (+ % 1) 3)]) (get-in v2 [(mod (+ % 2) 3)]))
                            (* (get-in v1 [(mod (+ % 2) 3)]) (get-in v2 [(mod (+ % 1) 3)]))
                            )
                         (range (count v1))))

(defn v*s [v k] (mapv #(* % k) v))

(def m+ (binOp v+))
(def m- (binOp v-))
(def m* (binOp v*))
(def md (binOp vd))

(defn getCol [m i]  (vec (apply concat (mapv #(drop i (take (+ i 1) %)) m))))
(defn transpose [m] (mapv #(getCol m, %) (range (count (get-in m [0])))))

(defn m*s [m k] (mapv #(v*s % k) m))
(defn m*v [m, v] (mapv #(scalar % v) m))
(defn m*m [m1, m2] (transpose (mapv #(m*v m1 %) (transpose m2))))

(defn shapeless [f] (fn [m1, m2] (cond
                               (vector? m1) (mapv #(cond (vector? %1) ((shapeless f) %1 %2) :else (f %1 %2)) m1 m2)
                               :else (f m1 m2))))
(def s+ (shapeless +))
(def s- (shapeless -))
(def s* (shapeless *))
(def sd (shapeless /))
