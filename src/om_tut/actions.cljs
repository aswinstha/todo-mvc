(ns om-tut.actions)

;Individual functions
(defn swap-value-of-todo [todo]
  (todo :done not)
)

(defn update-text [text todo]
  (assoc todo :todo text))

;List functions
(defn add-todo [text todos]
  (conj todos {:todo text :done false}))

(defn remove-finished-todos [todos]
  (vec(remove :done todos)))

(defn get-visible [visibility todos]
  (case visibility
    "all" todos
    "active" (vec (remove :done todos))
    "completed" (vec (filter :done todos)))
  )

(defn mark-all [todos]
  (vec(for [todo todos
            :let [newtodo (assoc todo :done true)]]
        newtodo)))

(defn unmark-all [todos]
  (vec(for [todo todos
            :let [newtodo (assoc todo :done false)]]
        newtodo)))
