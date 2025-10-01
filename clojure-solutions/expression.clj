;; --------------------------------------- COMBINATORS ------------------------------------------------- ;;
(load-file "examples/parser.clj")

(def *space (+char " \t\n\r"))
(def *ws (+ignore (+star *space)))

(def *digit (+char "0123456789"))
(def *double (+map read-string (+seqf str (+str (+plus *digit)) (+char ".") (+str (+plus *digit)))))

(defn *seq [begin parser end]
  (+seqn 1
         (+char begin)
         (+opt (+seqf cons *ws parser (+star (+seqn 1 (+star *space) parser))))
         *ws
         (+char end)))

(defn *list [parser] (+map vec (*seq "(" parser ")")))

(def *all-chars (mapv char (range 32 128)))
(def *words (+map read-string (+str (+plus (+char (apply str *all-chars))))))
(defn *variables [elements] (str (apply #(Character/toLowerCase %) (subs (str elements) 0 1))))

(def *expr
  (letfn [(*value []
            (delay (+or
                     *double
                     *words
                     (*list (*value)))))]
    (+parser (+seqn 0 *ws (*value) *ws))))
;; ------------------------------------------------------------------------------------------------- ;;

;; ---------------------------------------------HW-10----------------------------------------------- ;;

;; Elements
(defn constant [val] (fn [_] (double val)))
(defn variable [name] (fn [variables] (variables name)))

;; Operations
(defn fabric [f]
  (fn [& args]
    (fn [variables] (apply f (map #(% variables) args)))))

(def add (fabric +))
(def subtract (fabric -))
(def multiply (fabric *))
(def divide (fabric (fn [x y] (/ (double x) (double y)))))
(def negate (fabric -))
(def exp (fabric (fn [arg] (Math/exp arg))))
(def ln (fabric (fn [arg] (Math/log arg))))
;; ------------------------------------------------------------------------------------------------- ;;

;; ---------------------------------------------HW-11----------------------------------------------- ;;

;; Supporting functions
(defn pget [obj key]
  (cond
    (contains? obj key) (obj key)
    (contains? obj :proto) (pget (obj :proto) key)
    :else nil)
  )
(defn pcall [obj key & args] (apply (pget obj key) obj args))

(defn field [key] #(pget % key))
(defn method [key] #(apply pcall %1 key %&))

(defn constructor [ctor proto]
  (fn [& args] (apply ctor {:proto proto} args)))

(def evaluate (method :evaluate))
(def toString (method :toString))
(def toStringPostfix (method :toStringPostfix))

;; Elements
(defn Var [this name]
  (assoc (assoc this :name (*variables name)) :realName name))
(def VarPrototype {
                   :evaluate (fn [this vars] (vars (:name this)))
                   :toString (fn [this] (:realName this))
                   :toStringPostfix (fn [this] (:realName this))
                   })
(def Variable (constructor Var VarPrototype))

(defn Cnst [this value]
  (assoc this :value value))
(def CnstPrototype {
                    :evaluate (fn [this _] (:value this))
                    :toString (fn [this] (str (:value this)))
                    :toStringPostfix (fn [this] (str (:value this)))
                    })
(def Constant (constructor Cnst CnstPrototype))

;; Operations
(def ExprPrototype
  {
   :evaluate (fn [this vars] (apply
                                 (:operation this)
                                 (map #(evaluate % vars) (:args this))))

   :toString (fn [this] (format "(%s %s)"
                       (:stringOp this)
                       (clojure.string/join " " (mapv #(toString %) (:args this)))))
   :toStringPostfix (fn [this] (format "(%s %s)"
                       (clojure.string/join " " (mapv #(toStringPostfix %) (:args this)))
                       (:stringOp this)))
   })

(defn createOp [operation stringOp]
  (defn Op [this & args]
    (assoc (assoc (assoc this :operation operation) :args args) :stringOp stringOp))
  (constructor Op ExprPrototype)
  )

(def Add (createOp + "+"))
(def Subtract (createOp - "-"))
(def Multiply (createOp * "*"))
(def Divide (createOp (fn [x y] (/ (double x) (double y))) "/"))
(def Negate (createOp - "negate"))
(def Sin (createOp (fn [a] (Math/sin a)) "sin"))
(def Cos (createOp (fn [a] (Math/cos a)) "cos"))
(def Inc (createOp (fn [a] (+ a 1)) "++"))
(def Dec (createOp (fn [a] (- a 1)) "--"))
;; ------------------------------------------------------------------------------------------------- ;;

;; --------------------------------------------- PARSERS ------------------------------------------- ;;
(def exprMapFunctional {'var variable, 'const constant,
                        '+ add, '- subtract, '* multiply, '/ divide, 'negate negate, 'exp exp, 'ln ln})
(def exprMapObject {'var Variable, 'const Constant,
                    '+ Add, '- Subtract, '* Multiply, '/ Divide, 'negate Negate, 'sin Sin, 'cos Cos '++ Inc, '-- Dec})

(defn recurParser [elements exprMap]
  (cond
    (list? elements) (apply (exprMap (first elements)) (map #(recurParser % exprMap) (rest elements)))
    (symbol? elements) ((exprMap 'var) (str elements))
    (number? elements) ((exprMap 'const) elements)
    )
  )

(defn recurParserPostfix [elements exprMap]
  (cond
    (list? elements) (apply (exprMap (last elements)) (map #(recurParserPostfix % exprMap) (butlast elements)))
    (symbol? elements) ((exprMap 'var) (str elements))
    (number? elements) ((exprMap 'const) elements)
    )
  )

(defn parseFunction [expr] (recurParser (read-string expr) exprMapFunctional))
(defn parseObject [expr] (recurParser (read-string expr) exprMapObject))
(defn parseObjectPostfix [expr] (recurParserPostfix (*expr expr) exprMapObject))
;; ------------------------------------------------------------------------------------------------- ;;