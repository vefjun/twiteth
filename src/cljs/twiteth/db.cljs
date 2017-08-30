(ns twiteth.db
    (:require [cljs-web3.core :as web3]
              [cljs-time.core :as t]
              [cljs.spec.alpha :as s]
              [twiteth.constants :as constants]
              [twiteth.utils :as u]
              [re-frame.core :refer [dispatch]]
      ))

      (s/def ::load-node-addresses? boolean?)
      (s/def ::web3 (complement nil?))
      (s/def ::node-url string?)
      (s/def ::provides-web3? boolean?)
      (s/def ::contracts-not-found? boolean?)
      (s/def ::last-transaction-gas-used (s/nilable number?))
      (s/def ::drawer-open? boolean?)
      (s/def ::handler keyword?)
      (s/def ::route-params (s/map-of keyword? (some-fn number? string?)))
      (s/def :window/width-size int?)
      (s/def ::active-page (s/keys :req-un [::handler] :opt-un [::route-params]))
      (s/def ::selected-currency (partial contains? (set (keys constants/currencies))))
      (s/def ::open? boolean?)
      (s/def ::message string?)
      (s/def ::on-request-close fn?)
      (s/def ::auto-hide-duration int?)
      (s/def ::title string?)
      (s/def ::modal boolean?)
      (s/def ::snackbar (s/keys :req-un [::open? ::message ::on-request-close ::auto-hide-duration]))
      (s/def ::dialog (s/keys :req-un [::open? ::title ::modal]))
      (s/def :eth/config (s/map-of keyword? int?))
      (s/def ::name string?)
      (s/def ::address string?)
      (s/def ::bin string?)
      (s/def ::abi array?)
      (s/def :eth/contracts (s/map-of keyword? (s/keys :req-un [::name] :opt-un [::address ::bin ::abi])))
      (s/def ::my-addresses (s/coll-of string?))
      (s/def ::active-address (s/nilable string?))
      (s/def :blockchain/connection-error? boolean?)
      (s/def ::conversion-rates (s/map-of number? number?))
      (s/def ::conversion-rates-historical (s/map-of number? ::conversion-rates))
      (s/def ::load-all-conversion-rates-interval (s/nilable int?))

      (s/def ::db (s/keys :req-un [::load-node-addresses? ::node-url ::web3 ::active-page ::provides-web3? ::contracts-not-found?
                                   ::drawer-open?
                                   ::selected-currency ::snackbar ::my-addresses ::active-address
                                   ::conversion-rates ::conversion-rates-historical
                                   ::last-transaction-gas-used
                                   ::dialog]))


  (def default-db
    {
     :tweets []
     :settings {}
     :my-addresses []
     :accounts {}
     :new-tweet {:text ""
                 :name ""
                 :address nil
                 :sending? false}
     :web3 (or (aget js/window "web3")
               (if goog.DEBUG
                 (web3/create-web3 "http://localhost:8545/")
                 (web3/create-web3 "https://morden.infura.io/metamask")))
     :provides-web3? (or (aget js/window "web3") goog.DEBUG)
     :active-page (u/match-current-location)
     :load-node-addresses? true
     :node-url "http://localhost:8545" #_ "https://mainnet.infura.io/" #_ "http://localhost:8545" #_ "http://localhost:8549" #_"http://192.168.0.16:8545/" #_"http://localhost:8550"
     :contracts-not-found? false
     :window/width-size (u/get-window-width-size js/window.innerWidth)
     :drawer-open? false
     :selected-currency 0
     :last-transaction-gas-used nil
     :snackbar {:open? false
                :message ""
                :auto-hide-duration 5000
                :on-request-close #(dispatch [:snackbar/close])}
     :dialog {:open? false
              :modal false
              :title ""
              :actions []
              :body ""
              :on-request-close #(dispatch [:dialog/close])}
     :active-address nil
     :blockchain/connection-error? false
     :conversion-rates {}
     :conversion-rates-historical {}
     :load-all-conversion-rates-interval nil
     :eth/contracts {:simple-twitter {:name "SimpleTwitter" :address "0x54d7acf55a735faaba18f0ae04e43ec278cac869"}}})
