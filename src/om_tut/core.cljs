(ns ^:figwheel-always om-tut.core
  (:require[om.core :as om :include-macros true]
           [sablono.core :refer-macros [html]]
           [alandipert.storage-atom :refer [local-storage]]
           [om-tut.actions :as func]
           [om-tools.core :refer-macros [defcomponent]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload
(def todo-list
  [{:todo "Get Clojurescript set up" :done true}
   {:todo "Make first om component" :done true}
   {:todo "Become an om ninja" :done false }
   {:todo "Actually do our assignments" :done false }]
  )

(defonce app-state
  (local-storage
   (atom
   {:todos todo-list
    :visible-todos todo-list
    :header "todos"
    :visibility "all"})
   :todos-app-state))

(defcomponent todo-checkbox
  [todo owner]
  (render [_]
          (let [toggle (fn [todo] (update todo :done not))]
            (html
             [:input {:type "checkbox"
                      :checked (:done todo)
                      :on-change #(om/transact! todo toggle)}]))))

(defcomponent todo-item
  [todo owner]
  (init-state[_] {:editing? false})
  (render-state [_ state]
                (html
                 [:li {:class (when (:done todo) "done")}
                  (om/build todo-checkbox todo)
                  (if (:editing? state)
                    [:input {:default-value (:todo todo)
                             :auto-focus true
                             :on-key-up (fn[event]
                                          (let [input (.-target event)]
                                            (case (.-keyCode event)
                                              13 ((om/set-state! owner :editing? false) (om/transact! todo (partial func/update-text (.-value input))))
                                              27 (om/set-state! owner :editing? false)
                                              )))
                             :on-blur (fn[event] (om/set-state! owner :editing? false)
                                        (om/transact! todo (partial func/update-text (.-value (.-target event)))))}]
                    [:span {:on-double-click #(om/set-state! owner :editing? true)}
                     (:todo todo)])])))

(defn todo-adder
  [todos owner]
  (om/component
   (html
    [:input
     {:type "text"
      :placeholder "What needs doing?"
      :on-key-up (fn[event]
                   (let [input (.-target event)]
                     (when (= 13 (.-keyCode event))
                       (om/transact! todos
                                     (partial func/add-todo (.-value input) ))
                       (set! (.-value input) ""))))}])))


(defn bottom-bar [data owner]
  (om/component
   (html
    [:div
     [:span (count (remove :done (:todos data) )) " items left"]
     [:input
      {:type "button"
       :value "All"
       :on-click (fn[event]
                   (om/update! data :visibility "all")
                   )}]
     [:input
      {:type "button"
       :value "Active"
       :on-click (fn[event]
                   (om/update! data :visibility "active")
                   )}]
     [:input
      {:type "button"
       :value "Completed"
       :on-click (fn[event]
                   (om/update! data :visibility "completed")
                   )}]
     [:input
      {:type "button"
       :value "Clear completed"
       :class (when
                (= 0 (count (remove #(not (:done %)) (:todos data) )))
                "hide")
       :on-click (fn[event]
                   (om/transact! data :todos func/remove-finished-todos))}]
     ])))


(defn top-bar [todos owner]
  (om/component
   (html
    (let [not-marked (not= 0 (count (remove :done todos )))]
      [:div
       [:input
        {:type "button"
         :value (if not-marked "Mark all as done" "Mark all as not done")
         :on-click (fn[event]
                     (om/transact! todos (if not-marked func/mark-all func/unmark-all)))}]
       ]))))


(defn todos-component
  [data owner]
  (om/component
   (html
    [:div {:class "aligned"}
     [:h1 (:header data)]
     (om/build top-bar (:todos data))
     [:ul
      (let [todos (func/get-visible (:visibility data) (:todos data))]
        (for [todo todos]
          (om/build todo-item todo)))]
     (om/build todo-adder (:todos data))
     (om/build bottom-bar data)
     ])))

(om/root todos-component
         app-state
         {:target (. js/document (getElementById "app"))})
