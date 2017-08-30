(ns twiteth.events
  (:require
       [ajax.core :as ajax]
        [ajax.edn :as ajax-edn]
        [akiroz.re-frame.storage :as re-frame-storage]
        [cljs-web3.core :as web3]
        [cljs-web3.eth :as web3-eth]
        [cljs-web3.personal :as web3-personal]
        [cljs-web3.utils :as web3-utils]
        [day8.re-frame.async-flow-fx]
        [day8.re-frame.http-fx]
        [re-frame.core :as re-frame :refer [reg-event-db reg-event-fx inject-cofx path trim-v after debug reg-fx console dispatch dispatch-sync]]
        [cljs.spec.alpha :as s]
        [twiteth.interval-fx]
        [twiteth.utils :as u]
        [twiteth.components.confirm-dialog :as confirm-dialog]
        [twiteth.constants :as constants]
        [twiteth.window-fx]
        [madvas.re-frame.web3-fx]
        [madvas.re-frame.google-analytics-fx]
        [goog.string :as gstring]
        [goog.string.format]
        [print.foo :include-macros true]
        [medley.core :as medley]
        [twiteth.db :as db]
        ))

(def tweet-gas-limit 1000000)

(re-frame-storage/reg-co-fx! :twiteth {:fx :localstorage :cofx :localstorage})

(defn check-and-throw
     [a-spec db]
     (when goog.DEBUG
       (when-not (s/valid? a-spec db)
         (.error js/console (s/explain-str a-spec db))
         (throw "Spec check failed"))))


(def check-spec-interceptor (after (partial check-and-throw :twiteth.db/db)))

(def interceptors [check-spec-interceptor
                      #_(when ^boolean goog.DEBUG debug)
                      trim-v])

(defn contract-xhrio [contract-name code-type on-success on-failure]
  {:method :get
   :uri (gstring/format "./contracts/build/%s.%s?v=%s" contract-name (name code-type) constants/contracts-version)
   :timeout 6000
   :response-format (if (= code-type :abi) (ajax/json-response-format) (ajax/text-response-format))
   :on-success on-success
   :on-failure on-failure})

(defn get-contract [db key]
  (get-in db [:eth/contracts key]))

(defn get-instance [db key]
  (get-in db [:eth/contracts key :instance]))

(defn get-contract-class [db key]
  (get-in db [:eth/contracts key :class]))

(defn get-max-gas-limit [db]
  (get-in db [:eth/config :max-gas-limit]))


(defn migrate-localstorage [localstorage]
  (update localstorage :selected-currency #(if (keyword? %) (constants/currencies-backward-comp %)
                                                            (or % (:selected-currency db/default-db)))))

(defn all-contracts-loaded? [db]
  (every? #(and (:abi %) (if goog.DEBUG (:bin %) true)) (vals (:eth/contracts db))))

(defn all-contracts-deployed? [db]
  (every? #(and (:instance %) (:address %)) (vals (:eth/contracts db))))

(def log-used-gas
  (re-frame/->interceptor
    :id :log-used-gas
    :before (fn [{:keys [coeffects] :as context}]
              (let [event (:event coeffects)
                    {:keys [gas-used] :as receipt} (last event)
                    gas-limit (first event)]
                (let [gas-used-percent (* (/ gas-used gas-limit) 100)
                      gas-used-percent-str (gstring/format "%.2f%" gas-used-percent)]
                  (console :log "gas used:" gas-used-percent-str gas-used (second event))
                  (-> context
                    (update-in [:coeffects :event (dec (count event))]
                               merge
                               {:success? (< gas-used gas-limit)
                                :gas-used-percent gas-used-percent-str})
                    (update-in [:coeffects :event] #(-> % rest vec))
                    (assoc-in [:coeffects :db :last-transaction-gas-used] gas-used-percent)))))
    :after (fn [context]
             (let [event (:event (:coeffects context))]
               (update context :effects merge
                       {:ga/event ["log-used-gas"
                                   (name (:fn-key (first event)))
                                   (str (select-keys (last event) [:gas-used :gas-used-percent :transaction-hash
                                                                   :success?]))]})))))


  (defn filter-contract-setters [db]
    (medley/filter-vals :setter? (:eth/contracts db)))


(reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))


(reg-event-fx
 :initialize
 (inject-cofx :localstorage)
 (fn [{:keys [localstorage]} [deploy-contracts?]]
   (let [localstorage (migrate-localstorage localstorage)
         db (as-> db/default-db db
                  (merge-with #(if (map? %1) (merge-with merge %1 %2) %2) db localstorage)
                  (assoc db :on-load-seed (rand-int 99999))
                  (assoc db :drawer-open? (> (:window/width-size db) 2)))
                  ]
   (merge
     {:db db
       :async-flow {:first-dispatch [:load-eth-contracts]
                    :rules [{:when :seen?
                             :events [:eth-contracts-loaded :blockchain/my-addresses-loaded]
                             :halt? true}]}
        :dispatch-n [[:load-conversion-rates]]
        :dispatch-interval {:dispatch [:load-conversion-rates]
                            :ms 60000
                            :db-path [:load-all-conversion-rates-interval]}
        :window/on-resize {:dispatch [:window/on-resize]
                           :resize-interval 166}
        :ga/page-view [(u/current-location-hash)]}
     (when (:provides-web3? db/default-db)
       {:web3-fx.blockchain/fns
        {:web3 (:web3 db/default-db)
         :fns [[web3-eth/accounts :blockchain/my-addresses-loaded :log-error]]}})))))

 (reg-event-fx
   :load-eth-contracts
   interceptors
   (fn [{:keys [db]}]
     {:http-xhrio
      (flatten
        (for [[key {:keys [name]}] (:eth/contracts db)]
          (for [code-type (if goog.DEBUG [:abi :bin] [:abi])]
            (contract-xhrio name code-type [:contract/loaded key code-type] [:log-error :load-eth-contracts]))))}))

  (reg-event-fx
    :contract/loaded
    interceptors
    (fn [{:keys [db]} [contract-key code-type code]]
      (let [code (if (= code-type :abi) (clj->js code) (str "0x" code))
            contract (get-contract db contract-key)
            contract-address (:address contract)]
        (let [new-db (cond-> db
                       true
                       (assoc-in [:eth/contracts contract-key code-type] code)

                       (= code-type :abi)
                         (update-in [:eth/contracts contract-key] merge
                                  (when contract-address
                                    {:instance (web3-eth/contract-at (:web3 db) code contract-address)})))]

          ;;(console :log (str "contract/loaded" contract-key " " code-type))
          (merge
            {:db new-db
             :dispatch-n (remove nil?
                                 [(when (all-contracts-loaded? new-db)
                                    [:eth-contracts-loaded])
                                  (when (and (= code-type :abi) (= contract-key :simple-twitter))
                                         [:contract/simple-twitter-loaded contract-key])
                                   ])})))))

(reg-event-fx
      :contract/simple-twitter-loaded
      interceptors
      (fn [{:keys [db]} [contract-key]]
        (let [web3 (:web3 db)
              contract-instance (get-instance db contract-key)]
          (console :log "simple-twitter-loaded")

          {:db db

           :web3-fx.contract/events
           {:db-path [:contract :events]
            :events [[contract-instance :on-tweet-added {} {:from-block 0} :contract/on-tweet-loaded :log-error]]}

           :web3-fx.contract/constant-fns
           {:instance contract-instance
             :fns [[:get-settings :contract/settings-loaded :log-error]]}})))


(reg-event-fx
  :contract/abi-loaded
  interceptors
  (fn [{:keys [db]} [abi]]
    (let [web3 (:web3 db)
          contract-instance (web3-eth/contract-at web3 abi (:address (:contract db)))]

      {:db (assoc-in db [:contract :instance] contract-instance)

       :web3-fx.contract/events
       {:instance contract-instance
        :db db
        :db-path [:contract :events]
        :events [[:on-tweet-added {} {:from-block 0} :contract/on-tweet-loaded :log-error]]}

       :web3-fx.contract/constant-fns
       {:instance contract-instance
        :fns [[:get-settings :contract/settings-loaded :log-error]]}})))

  (reg-event-fx
    :eth-contracts-loaded
    interceptors
    (fn [{:keys [db]}]
      ))

  (reg-event-fx
   :set-active-page
   interceptors
   (fn [{:keys [db]} [{:keys [:handler] :as match}]]
       (merge
         {:db (-> db
            (assoc :active-page match)
            (assoc :drawer-open? false))
          :ga/page-view [(u/current-location-hash)]
         }
         (when-not (= handler (:handler (:active-page db)))
           {:window/scroll-to-top true}))))

    (reg-event-fx
               :window/scroll-to-top
               interceptors
               (fn []
                 {:window/scroll-to-top true}))

    (reg-event-fx
               :window/on-resize
               interceptors
               (fn [{:keys [db]} [width]]
                 ;;(console :log "resize " width)
                 {:db (assoc db :window/width-size (u/get-window-width-size width))}))

    (reg-event-db
               :drawer/set
               interceptors
               (fn [db [open?]]
                 ;;(console :log "open? " open?)
                 (assoc db :drawer-open? open?)))



   (reg-event-fx
     :set-active-address
     [interceptors (inject-cofx :localstorage)]
     (fn [{:keys [db localstorage]} [address]]
       {:db (-> db
              (assoc :active-address address))
        :localstorage (assoc localstorage :active-address address)}))

  (reg-event-fx
    :blockchain/my-addresses-loaded
    [interceptors (inject-cofx :localstorage)]
    (fn [{:keys [db localstorage]} [addresses]]
      (let [addresses (if (seq (:my-addresses-forced db)) (:my-addresses-forced db) addresses)
            active-address (if (contains? (set addresses) (:active-address localstorage))
                             (:active-address localstorage)
                             (first addresses))]
       {:db (-> db
             (assoc :my-addresses addresses)
             (assoc :active-address (first addresses))
             (assoc-in [:new-tweet :address] (first addresses)))
       :web3-fx.blockchain/balances
       {:web3 (:web3 db/default-db)
        :addresses addresses
        :watch? true
        :blockchain-filter-opts "latest"
        :dispatches [:blockchain/balance-loaded :log-error]}})))

   (reg-event-db
     :contract/on-tweet-loaded
     interceptors
     (fn [db [tweet]]
       ;;(console :log (str "tweet loaded " tweet))
       (update db :tweets conj (merge (select-keys tweet [:address :text :name])
                                      {:date (u/big-number->date-time (:date tweet))
                                       :tweet-key (.toNumber (:tweet-key tweet))}))))

   (reg-event-db
     :contract/settings-loaded
     interceptors
     (fn [db [[max-name-length max-tweet-length]]]
       (console :log "contract settings-loaded")
       (assoc db :settings {:max-name-length (.toNumber max-name-length)
                            :max-tweet-length (.toNumber max-tweet-length)})))

  (reg-event-fx
    :blockchain/unlock-account
    interceptors
    (fn [{:keys [db]} [address password]]
      {:web3-fx.blockchain/fns
       {:web3 (:web3 db)
        :fns [[web3-personal/unlock-account address password 999999
               :blockchain/account-unlocked
               :log-error]]}}))

  (reg-event-fx
    :blockchain/account-unlocked
    interceptors
    (fn [{:keys [db]}]
      (console :log "Account was unlocked.")
      {}))

  (reg-event-db
     :blockchain/balance-loaded
     interceptors
     (fn [db [balance address]]
       (assoc-in db [:accounts address :balance] (web3/from-wei balance :ether))))

   (reg-event-fx
     :blockchain/on-error
     interceptors
     (fn [{:keys [:db]} errors]
       (apply console :error "blockchain-error" errors)
       {:db (assoc db :blockchain/connection-error? true)
        :ga/event ["blockchain-error" (name (first errors)) (str (rest errors))]
        :dispatch [:snackbar/show-error "Oops, looks like we have trouble connecting into the Ethereum blockchain"]}))

   (reg-event-db
     :new-tweet/update
     interceptors
     (fn [db [key value]]
       (assoc-in db [:new-tweet key] value)))

   (reg-event-fx
     :new-tweet/send
     interceptors
     (fn [{:keys [db]} []]
       (let [{:keys [name text address]} (:new-tweet db)
              tx-opts {:gas tweet-gas-limit :from address}
              contract-instance (get-instance db :simple-twitter)]
          ;;(console :log (str name " " text " " address))
          {:web3-fx.contract/state-fns
            {:web3 (:web3 db)
             :db-path [:contract :send-tweet]
             :fns [[contract-instance :add-tweet name text tx-opts
                    :new-tweet/confirmed
                    :log-error
                    :new-tweet/transaction-receipt-loaded]]}})))

   (reg-event-db
     :new-tweet/confirmed
     interceptors
     (fn [db [transaction-hash]]
       (do
         ;;(console :log (str "transaction " transaction-hash))
         (assoc-in db [:new-tweet :sending?] true))))

   (reg-event-db
     :new-tweet/transaction-receipt-loaded
     interceptors
     (fn [db [{:keys [gas-used] :as transaction-receipt}]]
       ;;(console :log (str "receipt " transaction-receipt))
       (when (= gas-used tweet-gas-limit)
         (console :error "All gas used"))
       (assoc-in db [:new-tweet :sending?] false)))

   (reg-event-fx
     :contract/fetch-compiled-code
     interceptors
     (fn [{:keys [db]} [on-success]]
        {:http-xhrio
            (flatten
              (for [[key {:keys [name]}] (:eth/contracts db)]
                    {:method :get
                     :uri (gstring/format "/contracts/build/%s.json" name)
                     :timeout 6000
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success (conj on-success name)
                     :on-failure [:log-error]}))}))

   (reg-event-fx
     :contract/deploy-compiled-code
     interceptors
     (fn [{:keys [db]} [name contracts]]
       (let [{:keys [abi bin]} (get-in contracts [:contracts (keyword name)])]
         (console :log (str "deploying "  name))
         {:web3-fx.blockchain/fns
          {:web3 (:web3 db)
           :fns [[web3-eth/contract-new
                  (js/JSON.parse abi)
                  {:gas 4500000
                   :data (str "0x" bin)
                   :from (first (:my-addresses db))}
                  :contract/deployed
                  :log-error]]}})))


   (reg-event-fx
     :contract/deployed
     interceptors
     (fn [_ [contract-instance]]
       (when-let [address (aget contract-instance "address")]
         (console :log "Contract deployed at" address))))

   (reg-event-fx
     :log-error
     interceptors
     (fn [_ [err]]
       (console :error err)
       {}))

   (reg-event-fx
     :print-accounts
     interceptors
     (fn [{:keys [db]}]
       {:web3-fx.blockchain/fns
        {:web3 (:web3 db)
         :fns [[web3-eth/accounts :log [:blockchain/on-error :print-accounts]]]}}))

;;;;;;;;;;;;;;;;;;;;;;;; snackbar + dialog ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(reg-event-fx
  :snackbar/show-error
  interceptors
  (fn [{:keys [db]} [error-text]]
      {:db (update db :snackbar merge
                   {:open? true
                    :message (or error-text "Oops, we got an error while saving to blockchain")
                    :action nil
                    :on-action-touch-tap nil})}))

(reg-event-fx
  :snackbar/show-message
  interceptors
  (fn [{:keys [db]} [message]]
      {:db (update db :snackbar merge
                   {:open? true
                    :message message
                    :action nil
                    :on-action-touch-tap nil})}))

(reg-event-fx
  :snackbar/show-message-redirect-action
  interceptors
  (fn [{:keys [db]} [message route route-params]]
      {:db (update db :snackbar merge
                   {:open? true
                    :message message
                    :action "SHOW ME"
                    :on-action-touch-tap #(dispatch [:location/set-hash route route-params])})}))

(reg-event-db
  :snackbar/close
  interceptors
  (fn [db _]
    (assoc-in db [:snackbar :open?] false)))

(reg-event-fx
  :dialog/open-confirmation
  interceptors
  (fn [{:keys [db]} [{:keys [:on-confirm] :as dialog}]]
    {:db (update db :dialog merge
                 (merge {:open? true
                         :message dialog
                         :actions (confirm-dialog/create-confirm-dialog-action-buttons
                                    {:confirm-button-props {:on-confirm on-confirm}})}
                        (dissoc dialog :on-confirm)))}))

(reg-event-db
  :dialog/close
  interceptors
  (fn [db _]
    (assoc-in db [:dialog :open?] false)))

     ;;;;;;;;;;;;;;;;;;;;,, debug ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   (reg-event-db
     :print-db
     interceptors
     (fn [db]
       (print.foo/look db)
       db))

   (reg-event-fx
     :print-contract-addresses
     interceptors
     (fn [{:keys [db]}]
       (doseq [[key {:keys [:address]}] (:eth/contracts db)]
         (println key address))
       nil))


   (reg-event-fx
     :log
     interceptors
     (fn [db result]
       (apply console :log (u/big-nums->nums (if (and (not (string? result)) (some sequential? result))
                                               (map u/big-nums->nums result)
                                               result)))))

   (reg-event-fx
     :do-nothing
     interceptors
     (fn [db result]
       ))

   (reg-event-fx
     :log-error
     interceptors
     (fn [{:keys [:db]} errors]
       (apply console :error errors)
       {:db db
        :ga/event ["error" (first errors) (str (rest errors))]}))

  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; currency ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  (reg-event-fx
    :load-conversion-rates
    interceptors
    (fn [{:keys [db]}]
      {:http-xhrio {:method :get
                    :uri "https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=USD,EUR,RUB,GBP,CNY,JPY"
                    :timeout 20000
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success [:conversion-rates-loaded]
                    :on-failure [:log-error :load-conversion-rates]}}))

  (reg-event-fx
    :load-conversion-rates-historical
    interceptors
    (fn [{:keys [db]} [timestamp]]
      (when-not (get-in db [:conversion-rates-historical timestamp])
        {:http-xhrio {:method :get
                      :uri (str "https://min-api.cryptocompare.com/data/pricehistorical?fsym=ETH&tsyms=USD,EUR,RUB,GBP,CNY,JPY&ts="
                                (/ timestamp 1000))
                      :timeout 20000
                      :response-format (ajax/json-response-format {:keywords? true})
                      :on-success [:conversion-rates-loaded-historical timestamp]
                      :on-failure [:log-error :load-conversion-rates-historical]}})))

  (reg-event-db
    :conversion-rates-loaded-historical
    interceptors
    (fn [db [timestamp {:keys [:ETH]}]]
      (update-in db [:conversion-rates-historical timestamp] merge (medley/map-keys constants/currency-code->id ETH))))

  (reg-event-db
    :conversion-rates-loaded
    interceptors
    (fn [db [response]]
      (update db :conversion-rates merge (medley/map-keys constants/currency-code->id response))))

  (reg-event-fx
    :selected-currency/set
    [interceptors (inject-cofx :localstorage)]
    (fn [{:keys [db localstorage]} [currency]]
      {:db (assoc db :selected-currency currency)
       :localstorage (assoc localstorage :selected-currency currency)}))

  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
