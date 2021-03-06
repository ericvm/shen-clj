(ns shen-port.backend-test
  (:require [shen-port.backend :as backend]
            [clojure.core.match :refer [match]]
            [midje.sweet :refer :all]))

(facts "Locals [type X _] -> (kl-to-lisp Locals X)"
       (fact (backend/kl->clj [] '(type x y)) => '(quote x)))

(facts "Locals [lambda X Y] -> (let ChX (ch-T X) (protect [FUNCTION [LAMBDA [ChX] (kl-to-lisp [ChX | Locals] (SUBST ChX X Y))]]))"
       (fact (backend/kl->clj [] '(lambda x x)) => '(clojure.core/fn [x] x))
       (fact (backend/kl->clj [] '((lambda x x) 4)) => '((shen.primitives/resolve-fn (clojure.core/fn [x] x)) 4)))

(facts "(let x y z)"
       (fact (backend/kl->clj [] '(let x 5 z)) => '(clojure.core/let [x 5] (quote z))))

(facts "(defun name vars body)"
       (fact (backend/kl->clj [] '(defun func (x) x)) => '(shen-port.primitives/with-ns
                                                            (quote shen.functions)
                                                            (clojure.core/defn func [x] x))))

(facts "forward declaration"
       (fact (backend/kl->clj [] '(lambda X true)) => '(clojure.core/fn [X] true))

       (fact (backend/kl->clj [] '(defun func (x) (will-declare-2 x))) => '(shen-port.primitives/with-ns (quote shen.functions)
                                                                             (do (clojure.core/declare will-declare-2)
                                                                                 (clojure.core/defn func [x] (will-declare-2 x))))))

(facts "empty list"
       (fact (backend/kl->clj [] '()) => '()))

(fact "innocent symbols"
      (fact (backend/kl->clj [] 'bla) => '(quote bla)))

(def code-sample '(V2856 (map (lambda Z (shen-dot-walk V2856 Z)) V2857)))

(fact (backend/kl->clj ['V2856 'V2857] code-sample) => '((shen.primitives/resolve-fn V2856) (map (do (clojure.core/declare shen-dot-walk) (clojure.core/fn [Z] (shen-dot-walk V2856 Z))) V2857)))
