(ns shen-port.primitives
  (:require [clojure.core :as c]
            [shen-port.backend :as backend])
  (:refer-clojure :exclude [set intern]))

(create-ns 'shen.globals)
(create-ns 'shen.functions)

(def intern symbol)

(defn set* [X Y ns]
  @(c/intern (the-ns ns)
             (with-meta X {:dynamic true :declared true})
             Y))

(defn set
  ([X] (partial set X))
  ([X Y] (set* X Y 'shen.globals)))

(defn ^:private value* [X ns]
  (c/let [v (c/and (symbol? X) (ns-resolve ns X))]
    (if (nil? v)
      (throw (IllegalArgumentException. (c/str "variable " X " has no value")))
      @v)))

(defn value [X] (value* X 'shen.globals))

;;function declarations
(set* 'set #'set 'shen.functions)
(set* 'set* #'set* 'shen.functions) ;TODO: This is a bit weird
(set* 'value #'value 'shen.functions)
(set* '+ #'c/+ 'shen.functions)

(defn eval-kl
  [X]
  (eval (doto (backend/kl->clj [] X) println)))
