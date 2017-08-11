(ns twiteth.components.misc
  (:require
    [cljs-react-material-ui.reagent :as ui]
    [cljs-web3.core :as web3]
    [clojure.string :as string]
    [twiteth.styles :as styles]
    [twiteth.utils :as u]
    [medley.core :as medley]
    [reagent.core :as r]
    [reagent.impl.template :as tmpl]
    [re-frame.core :refer [subscribe dispatch dispatch-sync]]
    ))

(def col (r/adapt-react-class js/ReactFlexboxGrid.Col))
(def row (r/adapt-react-class js/ReactFlexboxGrid.Row))
(def grid (r/adapt-react-class js/ReactFlexboxGrid.Grid))

(u/set-default-props! (aget js/MaterialUI "TableHeader") {:adjust-for-checkbox false
                                                          :display-select-all false})
(u/set-default-props! (aget js/MaterialUI "TableBody") {:display-row-checkbox false})
(u/set-default-props! (aget js/MaterialUI "TableRow") {:selectable false})
(u/set-default-props! (aget js/MaterialUI "Table") {:selectable false})
(u/set-default-props! (aget js/MaterialUI "TableFooter") {:adjust-for-checkbox false})


(defn logo [props]
  [:a
   (r/merge-props
     {:href (u/path-for :home)
      :style styles/twiteth-logo}
     props)
   "twiteth"])

(def row-plain (u/create-with-default-props row {:style styles/row-no-margin}))

(defn a [{:keys [route-params route underline-hover?] :as props} body]
  [:a
   (r/merge-props
     {:style {:color (:primary1-color styles/palette)}
      :class (when underline-hover? "hoverable")
      :href (when-not (some nil? (vals route-params))
              (medley/mapply u/path-for route route-params))
      :on-click #(.stopPropagation %)}
     (dissoc props :route-params :route)) body])

(defn line
  ([body]
   [:div {:style styles/line} body])
  ([label body]
   [:div {:style styles/line} [:span label ": "] [:b body]]))

   ;;(defn create-table-pagination [list-pagination-props]
  ;;   [ui/table-footer
  ;;    [ui/table-row
    ;;   [ui/table-row-column
    ;;    {:col-span 99
    ;;     :style styles/pagination-row-column
    ;;    [row-plain
    ;;     {:start "xs"
      ;;    :end "sm"
    ;;     [list-pagination list-pagination-props]]

 (defn create-no-items-row [text & [loading?]]
   [ui/table-row
    [ui/table-row-column
     {:col-span 99 :style styles/text-center}
     (if-not loading? text "Loading...")]])

 (defn center-layout [& children]
   [row {:center "xs"}
    (into [col {:xs 12 :md 10 :lg 9 :style styles/text-left}]
          children)])

(defn- link [href text]
  [:a {:href href
       :target :_blank}
   text])

(defn youtube [props]
  [:iframe
   (r/merge-props
     props
     {:width 560
      :height 315
      :frameBorder 0
      :allowFullScreen true})])

(defn paper []
  (let [xs-width (subscribe [:window/xs-width?])
        connection-error? (subscribe [:blockchain/connection-error?])]
    (fn [props & children]
      (let [[props children] (u/parse-props-children props children)]
        [ui/paper
         (dissoc props :loading? :inner-style)
         [ui/linear-progress {:mode :indeterminate
                              :style {:visibility (if (and (:loading? props)
                                                           (not @connection-error?))
                                                    :visible
                                                    :hidden)}
                              :color styles/accent1-color}]
         (into [] (concat [:div {:style (merge (if @xs-width styles/paper-secton-thin
                                                             styles/paper-secton)
                                               styles/word-wrap-break
                                               (:inner-style props))}]
                          children))]))))

(defn centered-rows [& children]
  [center-layout
   [paper
    [row
     {:middle "xs" :center "xs"}
     (for [[i child] (medley/indexed children)]
       [col {:xs 12 :key i}
        child])]]])

(defn currency [value opts]
  [:span {:title (str (u/big-num->num value) "Ξ")}
   @(subscribe [:selected-currency/converted-value value opts])])

(defn call-on-change [{:keys [:args :load-on-mount?]}]
  (let [prev-args (r/atom (when-not load-on-mount? args))]
    (fn [{:keys [:on-change :args]} & childen]
      (when-not (= @prev-args args)
        (reset! prev-args args)
        (when (fn? on-change)
          (on-change args)))
      (into [:div] childen))))

(defn how-it-works-app-bar-link [props & children]
  (let [[props children] (u/parse-props-children props children)]
    [:div
     (r/merge-props
       {:style {:margin-top 10 :text-align :right}}
       props)
     [:a
      {:href (u/path-for :how-it-works)}
      [:h3.bolder
       {:style styles/white-text}
       (or (first children) "How it works")]]]))
