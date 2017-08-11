(ns twiteth.core
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [dispatch dispatch-sync clear-subscription-cache!]]
            [cljsjs.web3]
            [twiteth.events]
            [twiteth.subs]
            [twiteth.views :as views]
            [twiteth.config :as config]
            [twiteth.config :as config]
            [twiteth.utils :as u]
            [twiteth.routes :refer [routes]]
            [cljs-time.extend]
            [cljs.spec.alpha :as s]
            [cljsjs.bignumber]
            [cljsjs.material-ui]
            [cljsjs.react-flexbox-grid]
            [print.foo :include-macros true]
            [madvas.re-frame.google-analytics-fx :as google-analytics-fx]
            [twiteth.components.main-panel :refer [main-panel]]
            ))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))


(defn mount-root []
    (s/check-asserts goog.DEBUG)
    (google-analytics-fx/set-enabled! (not goog.DEBUG))
    (clear-subscription-cache!)
    (reagent/render [main-panel] (.getElementById js/document "app")))

(defn ^:export init []
    (s/check-asserts goog.DEBUG)
    (google-analytics-fx/set-enabled! (not goog.DEBUG))
    (dispatch-sync [:initialize])
    (dev-setup)
    (set! (.-onhashchange js/window) #(dispatch [:set-active-page (u/match-current-location)]))
    (mount-root))
