(ns twiteth.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require  [re-frame.core :refer [reg-sub]]
             [cemerick.url :as url]
             [clojure.data :as data]
             [twiteth.db :refer [default-db]]
             [medley.core :as medley]
             [twiteth.constants :as constants]
             [twiteth.utils :as u]
             ))

(reg-sub
 :name
 (fn [db]
   (:name db)))

   (reg-sub
    :db/current-page
    (fn [db _]
      (:active-page db)))

   (reg-sub
    :window/width-size
    (fn [db]
      (:window/width-size db)))

   (reg-sub
    :window/lg-width?
    (fn [db]
      (= (:window/width-size db) 3)))

   (reg-sub
    :window/xs-width?
    (fn [db]
      (= (:window/width-size db) 0)))

   (reg-sub
    :window/xs-sm-width?
    (fn [db]
      (<= (:window/width-size db) 1)))

   (reg-sub
     :db/drawer-open?
     (fn [db _]
       (:drawer-open? db)))

   (reg-sub
     :db/my-addresses
     (fn [db]
       (:my-addresses db)))

   (reg-sub
     :db/tweets
     (fn [db]
       (sort-by :date #(compare %2 %1) (:tweets db))))

   (reg-sub
     :db/new-tweet
     (fn [db]
       (:new-tweet db)))

   (reg-sub
     :db/settings
     (fn [db]
       (:settings db)))

   (reg-sub
     :new-tweet/selected-address-balance
     (fn [db]
       (get-in db [:accounts (:address (:new-tweet db)) :balance])))

       (reg-sub
         :db/active-address
         (fn [db _]
           (:active-address db)))

       (reg-sub
         :db/active-address-balance_old
         :<- [:db/active-user]
         (fn [active-user]
           (:user/balance active-user)))

       (reg-sub
         :db/accounts
         (fn [db]
           (:accounts db)))

       (reg-sub
         :db/active-address-balance
           :<- [:db/active-address]
           :<- [:db/accounts]
         (fn [[active-address accounts]]
           (:balance (get accounts active-address))))

       (reg-sub
         :db/my-addresses
         (fn [db _]
           (:my-addresses db)))

       (reg-sub
         :eth/config
         (fn [db _]
           (:eth/config db)))

       (reg-sub
         :db/active-user
         :<- [:db/active-address]
         :<- [:app/users]
         (fn [[active-user-id users]]
           (get users active-user-id)))

       (reg-sub
         :db/my-users-loading?
         :<- [:db/my-addresses]
         :<- [:app/users]
         (fn [[my-addresses users]]
           (some (comp nil? :user/status) (vals (select-keys users my-addresses)))))

       (reg-sub
         :db/active-address-registered?
         :<- [:db/active-user]
         (fn [active-user]
           (pos? (:user/status active-user))))

       (reg-sub
         :app/users
         (fn [db]
           (:app/users db)))

       (reg-sub
         :db/selected-currency
         (fn [db _]
           (:selected-currency db)))

       (reg-sub
         :db/snackbar
         (fn [db]
           (:snackbar db)))

       ;;;;;;;;;;;;;;;; currency ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

       (reg-sub
         :db/conversion-rates
         (fn [db [_ timestamp]]
           (if timestamp
             (get-in db [:conversion-rates-historical timestamp] {})
             (:conversion-rates db))))

       (reg-sub
         :db/conversion-rates-historical
         (fn [db _]
           (:conversion-rates-historical db)))

       (reg-sub
         :db/conversion-rate-tampered?
         :<- [:db/conversion-rates-historical]
         (fn [conversion-rates-historical [_ timestamp conversion-rate currency]]
           (when conversion-rate
             (when-let [rate-historical (get-in conversion-rates-historical [timestamp currency])]
               [(< 0.03 (js/Math.abs (- 1 (/ rate-historical conversion-rate)))) rate-historical]))))


       (reg-sub
         :selected-currency/converted-value
         :<- [:db/selected-currency]
         :<- [:db/conversion-rates]
         (fn [[selected-curency conversion-rates] [_ value {:keys [:value-currency] :as opts
                                                            :or {value-currency 0}}]]
           (u/convert-currency value value-currency selected-curency conversion-rates opts)))

       (reg-sub
         :currency/ether-value
         :<- [:db/conversion-rates]
         (fn [conversion-rates [_ value value-currency]]
           (u/currency->ether (u/parse-float value) value-currency conversion-rates)))

       ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

       (reg-sub
         :blockchain/connection-error?
         (fn [db]
           (:blockchain/connection-error? db)))
