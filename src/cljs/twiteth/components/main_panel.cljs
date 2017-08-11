(ns twiteth.components.main-panel
  (:require
    [cljs-react-material-ui.core :refer [get-mui-theme color]]
    [cljs-react-material-ui.reagent :as ui]
    [cljsjs.material-ui-chip-input]
    [clojure.set :as set]
    [twiteth.styles :as styles]
    [twiteth.components.currency-select-field :refer [currency-select-field]]
    [twiteth.components.misc :as misc :refer [row-plain col a center-layout row paper centered-rows currency]]
    [twiteth.constants :as constants]
    [twiteth.utils :as u]
    [twiteth.components.icons :as icons]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [twiteth.pages.about-page :refer [about-page]]
    [twiteth.pages.how-it-works-page :refer [how-it-works-page]]
    [twiteth.pages.tweets-page :refer [tweets-page]]
    ))

(def route->component
      {:tweets tweets-page
       :about about-page
       :home tweets-page
       :how-it-works how-it-works-page
       })

(def search-nav-items
  [["Tweets" :tweets (icons/magnify)]
   ["About us" :about (icons/magnify)]
   ["How it works" :how-it-works (icons/magnify)]
    ])

(defn create-menu-items [items]
  (for [[label handler icon query-string] items]
    [ui/list-item
     {:primary-text label
      :left-icon icon
      :value (u/ns+name handler)
      :href (str (u/path-for handler) query-string)
      :key handler}]))

(defn my-addresses-select-field []
  (let [my-addresses (subscribe [:db/my-addresses])
        active-address (subscribe [:db/active-address])]
    (fn []
      (when (< 1 (count @my-addresses))
        [ui/select-field
         {:value @active-address
          :on-change #(dispatch [:set-active-address %3])
          :style (merge styles/app-bar-user
                        {:width 170})
          :auto-width true
          :label-style styles/app-bar-select-field-label}
         (for [address @my-addresses]
           [ui/menu-item
            {:value address
             :primary-text (u/truncate address 25)
             :key address}])]))))

(defn app-bar-right-elements []
  (let [active-address-balance (subscribe [:db/active-address-balance])
        active-address-registered? (subscribe [:db/active-address-registered?])
        connection-error? (subscribe [:blockchain/connection-error?])
        my-addresses (subscribe [:db/my-addresses])
        selected-currency (subscribe [:db/selected-currency])]
    (fn []
      (if-not @connection-error?
        [row-plain
         {:middle "xs"
          :end "xs"}
         (when (and (seq @my-addresses)
                    @active-address-balance)
           [:h2.bolder {:style styles/app-bar-balance}
            [currency @active-address-balance]])
         (if (seq @my-addresses)
           [my-addresses-select-field]
           [misc/how-it-works-app-bar-link
            {:style {:margin-top 0}}
            [row-plain
             {:middle "xs"}
             [:span
              {:style {:margin-right 5}}
              "No accounts connected"]
             (icons/help-circle-outline {:color "#EEE"
                                         :style {:margin-right styles/desktop-gutter-less}})]])
         [currency-select-field
          {:value @selected-currency
           :label-style styles/app-bar-select-field-label
           :style styles/app-bar-user
           :on-change #(dispatch [:selected-currency/set %3])}]
         ]
        [row-plain
         {:middle "xs"
          :end "xs"}
         [misc/how-it-works-app-bar-link "Can't connect to a blockchain. How it works?"]]))))

(defn drawer-component [lg-width? drawer-open? handler]
    [ui/drawer
     {:docked lg-width?
      :open (or drawer-open? lg-width?)
      :on-request-change #(dispatch [:drawer/set %])}
     [:div
      {:style styles/navigation-drawer}
      [:div
       [ui/app-bar
        {:title (r/as-element [misc/logo])
         :show-menu-icon-button false
         :style styles/app-bar-left}]
       [ui/selectable-list
        {:value (u/ns+name handler)
         :style styles/nav-list
         :on-change (fn [])}
        (create-menu-items search-nav-items )]]]])

(defn main-panel []
  (let [current-page (subscribe [:db/current-page])
        drawer-open? (subscribe [:db/drawer-open?])
        active-address (subscribe [:db/active-address])
        lg-width? (subscribe [:window/lg-width?])
        xs-width? (subscribe [:window/xs-width?])]
    (fn []
      (let [{:keys [:handler]} @current-page]
       [ui/mui-theme-provider {:mui-theme styles/mui-theme}
         [:div
          (drawer-component @lg-width? @drawer-open? handler)
          [ui/app-bar
           {
            :title "TWITETH. A Little Less Simple Decentralized Twitter"
            :show-menu-icon-button (not @lg-width?)
            :icon-element-right (r/as-element [app-bar-right-elements])
            :on-left-icon-button-touch-tap #(dispatch [:drawer/set true])
            :style styles/app-bar-right}]
          (when-let [page (route->component handler)]
              [:div {:style (merge styles/content-wrap
                                   (when @lg-width?
                                     {:padding-left (+ 256 styles/desktop-gutter)})
                                   (when @xs-width?
                                     (styles/padding-all styles/desktop-gutter-mini)))}
                   [page]])]]))))
