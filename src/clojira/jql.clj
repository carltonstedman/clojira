(ns clojira.jql)

(defn- binary-op [op & xs]
  (reduce #(str "(" %1 " " op " " %2 ")")
          (map name xs)))

(def and (partial binary-op "AND"))
(def or (partial binary-op "OR"))
(def equals (partial binary-op "="))
(def not-equals (partial binary-op "!="))
(def greater-than (partial binary-op ">"))
(def greater-than-equals (partial binary-op ">="))
(def less-than (partial binary-op "<"))
(def less-than-equals (partial binary-op "<="))
(def contains (partial binary-op "~"))
(def not-contains (partial binary-op "!~"))
