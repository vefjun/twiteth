(ns twiteth.utils
  (:require [cljs-time.coerce :refer [to-date-time to-long to-local-date-time]]
            [cljs-time.format :as time-format]
            [cljs-time.core :as t :refer [date-time to-default-time-zone]]
            [cljs-web3.core :as web3]
            [bidi.bidi :as bidi]
            [cemerick.url :as url]
            [clojure.string :as string]
            [medley.core :as medley]
            [twiteth.constants :as constants]
            [clojure.set :as set]
            [twiteth.routes :refer [routes]]
            [goog.crypt :as crypt]
            [goog.crypt.Md5 :as Md5]
            [goog.format.EmailAddress :as email-address]
            [goog.string :as gstring]
            [reagent.core :as r]
            [reagent.impl.component :refer [camelify-map-keys]]))           


(defn truncate
  "Truncate a string with suffix (ellipsis by default) if it is
   longer than specified length."
  ([string length]
   (truncate string length "..."))
  ([string length suffix]
   (let [string-len (count string)
         suffix-len (count suffix)]
     (if (<= string-len length)
       string
       (str (subs string 0 (- length suffix-len)) suffix)))))

(defn evt-val [e]
  (aget e "target" "value"))

(defn big-number->date-time [big-num]
  (to-date-time (* (.toNumber big-num) 1000)))

(defn eth [big-num]
  (str (web3/from-wei big-num :ether) " ETH"))

(defn format-date [date]
  (time-format/unparse-local (time-format/formatters :rfc822) (to-default-time-zone (to-date-time date))))

    (defn big-num->num [x]
      (if (and x (aget x "toNumber"))
        (.toNumber x)
        x))

    (defn big-num? [x]
      (and x (aget x "toNumber")))

    (defn big-num|num|str? [x]
      (and x (or (big-num? x)
                 (number? x)
                 (string? x))))

    (defn big-num-pos? [x]
      (when x
        (.greaterThan x 0)))

    (defn big-num-zero? [x]
      (when x
        (.equals x 0)))

    (defn big-num-greater-than-or-equal-to? [x y]
      (when (and x y)
        (.greaterThanOrEqualTo x y)))

    (defn big-num-less-than? [x y]
      (when (and x y)
        (.lessThan x y)))

    (defn big-num-neg? [x]
      (when x
        (.isNegative x)))

        (defn big-nums->nums [coll]
      (map big-num->num coll))

      (defn uint8? [x]
        (and x (not (neg? x))))

      (defn uint? [x]
        (and x (not (neg? x))))

      (defn uint-or-nil? [x]
        (or (nil? x) (not (neg? x))))

      (defn address? [x]
        (web3/address? x))

      (defn bytes32? [x]
        (string? x))

      (defn bytes32-or-nil? [x]
        (or (nil? x) (string? x)))

      (defn uint-coll? [x]
        (and x (every? uint? x)))

      (defn address-coll? [x]
        (and x (every? address? x)))

      (defn date? [x]
        (instance? goog.date.DateTime x))

      (defn string-or-nil? [x]
        (or (nil? x) (string? x)))

      (defn date-or-nil? [x]
        (or (nil? x) (date? x)))

      (defn rating? [x]
        (<= 0 x 100))

      (def max-gas-limit 4000000)

      (defn path-for [& args]
        (str "#" (apply bidi/path-for routes args)))

      (defn current-location-hash []
        (let [hash (-> js/document
                     .-location
                     .-hash
                     (string/split #"\?")
                     first
                     (string/replace "#" ""))]
          (if (empty? hash) "/" hash)))

      (defn set-location-hash! [s]
        (set! (.-hash js/location) s))

      (defn current-url []
        (url/url (string/replace (.-href js/location) "#" "")))

      (defn set-location-query! [query-params]
        (set-location-hash!
          (str "#" (current-location-hash)
               (when-let [query (url/map->query query-params)]
                 (str "?" query)))))

      (defn add-to-location-query! [query-params]
        (let [current-query (:query (current-url))
              new-query (merge current-query (->> query-params
                                               (medley/map-keys constants/keyword->query)
                                               (medley/remove-keys nil?)))]
          (set-location-query! new-query)))

      (defn current-url-query []
        (->> (:query (current-url))
          (medley/map-keys (set/map-invert twiteth.constants/keyword->query))
          (medley/remove-keys nil?)
          (map (fn [[k v]]
                 (if-let [f (constants/query-parsers k)]
                   {k (f v)}
                   {k v})))
          (into {})))

      (defn nav-to! [route route-params]
        (set-location-hash! (medley/mapply path-for route route-params)))

      (defn ns+name [x]
        (when x
          (str (when-let [n (namespace x)] (str n "/")) (name x))))

      (defn match-current-location []
        (bidi/match-route routes (current-location-hash)))

      (defn target-value [e]
        (aget e "target" "value"))


      (defn eth->to-wei [x]
        (if (and x (aget x "toNumber"))
          (web3/to-wei x :ether)
          x))

      (defn replace-comma [x]
        (string/replace x \, \.))

      (defn empty-string? [x]
        (and (string? x) (empty? x)))

      (defn parse-float [number]
        (if (string? number)
          (js/parseFloat (replace-comma number))
          number))

      (defn non-neg? [x]
        (not (neg? x)))

      (defn non-neg-ether-value? [x & [{:keys [:allow-empty?]}]]
        (try
          (when (and (not allow-empty?) (empty-string? x))
            (throw (js/Error.)))
          (let [value (web3/to-wei (if (string? x) (replace-comma x) x) :ether)]
            (and
              (or (and (string? value)
                       (not (= "-" (first value))))
                  (and (big-num? value)
                       (not (big-num-neg? value))))))
          (catch :default e
            false)))

      (defn pos-ether-value? [x & [props]]
        (and (non-neg-ether-value? x props)
             (or (and (string? x)
                      (pos? (parse-float x)))
                 (and (number? x)
                      (pos? x))
                 (and (big-num? x)
                      (big-num-pos? x)))))

      (def non-neg-or-empty-ether-value? #(non-neg-ether-value? % {:allow-empty? true}))

      (defn ensure-vec [x]
        (if (sequential? x) x [x]))

      (defn zero-address? [x]
        (= x "0x0000000000000000000000000000000000000000"))

      (defn ensure-number [x]
        (if (and (number? x)
                 (not (js/isNaN x))
                 (js/isFinite x)) x 0))




                   (defn md5-bytes [s]
                     (let [container (doto (goog.crypt.Md5.)
                                       (.update s))]
                       (.digest container)))

                   (defn md5 [s]
                     (crypt/byteArrayToHex (md5-bytes s)))

                   (defn map->data-source [m val-key]
                     (let [getter (if val-key #(get % val-key) identity)]
                       (map (fn [[k v]] {"text" (getter v) "value" k}) (into [] m))))

                   (defn coll->data-source [coll]
                     (mapv (fn [[k v]] {"text" v "value" (inc k)}) coll))

                   (defn results-coll->data-source [results all-items]
                     (mapv (fn [k] {"text" (nth all-items (dec k)) "value" k}) results))

                   (def data-source-config {"text" "text" "value" "value"})

                   (defn data-source-values [values]
                     (set (map :value (js->clj values :keywordize-keys true))))

                   (defn assoc-key-as-value [key-name m]
                     (into {} (map (fn [[k v]]
                                     {k (assoc v key-name k)}) m)))

                   (defn timestamp-js->sol [x]
                     (/ x 1000))

                   (defn timestamp-sol->js [x]
                     (* x 1000))

                   (defn big-num->date-time [big-num]
                     (when (big-num-pos? big-num)
                       (to-default-time-zone (to-date-time (timestamp-sol->js (.toNumber big-num))))))

                   (defn format-datetime [date]
                     (when date
                       (time-format/unparse-local (time-format/formatters :rfc822) date)))

                   (defn time-ago [time]
                     (when time
                       (let [units [{:name "second" :limit 60 :in-second 1}
                                    {:name "minute" :limit 3600 :in-second 60}
                                    {:name "hour" :limit 86400 :in-second 3600}
                                    {:name "day" :limit 604800 :in-second 86400}
                                    {:name "week" :limit 2629743 :in-second 604800}
                                    {:name "month" :limit 31556926 :in-second 2629743}
                                    {:name "year" :limit nil :in-second 31556926}]
                             diff (t/in-seconds (t/interval time (t/now)))]
                         (if (< diff 5)
                           "just now"
                           (let [unit (first (drop-while #(or (>= diff (:limit %))
                                                              (not (:limit %)))
                                                         units))]
                             (-> (/ diff (:in-second unit))
                               js/Math.floor
                               int
                               (#(str % " " (:name unit) (when (> % 1) "s") " ago"))))))))

                   (defn parse-props-children [props children]
                     (if (map? props)
                       [props children]
                       [nil (concat [props] children)]))

                   (defn round
                     ([d] (round d 2))
                     ([d precision]
                      (let [factor (js/Math.pow 10 precision)]
                        (/ (js/Math.round (* d factor)) factor))))

                   (defn pluralize [text count]
                     (str text (when (not= count 1) "s")))

                   (defn country-name [country-id]
                     (when (and (pos? country-id)
                                (<= country-id (count constants/countries)))
                       (nth constants/countries (dec country-id))))

                   (defn state-name [state-id]
                     (when (and (pos? state-id)
                                (<= state-id (count constants/united-states)))
                       (nth constants/united-states (dec state-id))))

                   (defn rating->star [rating]
                     (/ (or rating 0) 20))

                   (defn star->rating [star]
                     (* (or star 0) 20))

                   (defn rand-str [n & [{:keys [:lowercase-only?]}]]
                     (let [chars-between #(map char (range (.charCodeAt %1) (inc (.charCodeAt %2))))
                           chars (concat (when-not lowercase-only? (chars-between \0 \9))
                                         (chars-between \a \z)
                                         (when-not lowercase-only? (chars-between \A \Z))
                                         (when-not lowercase-only? [\_]))
                           password (take n (repeatedly #(rand-nth chars)))]
                       (reduce str password)))

                   (defn create-with-default-props [component default-props]
                     (fn [props & children]
                       (let [[props children] (parse-props-children props children)]
                         (into [] (concat
                                    [component (r/merge-props default-props props)]
                                    children)))))

                   (defn gravatar-url [hash & [user-id]]
                     (let [valid? (= (count hash) 32)]
                       (gstring/format "https://s.gravatar.com/avatar/%s?s=300&d=retro%s"
                                       (if valid? hash user-id)
                                       (if valid? "" "&f=y"))))

                   (defn list-filter-loaded [list non-empty-pred]
                     (-> list
                       (assoc :loading? (or (:loading? list) (some (complement non-empty-pred) (:items list))))
                       (update :items (partial filter non-empty-pred))))

                   (defn paginate [offset limit coll]
                     (->> coll
                       (drop offset)
                       (take limit)))


                   (defn set-default-props! [react-class default-props]
                     (let [current-defaults (-> (aget react-class "defaultProps")
                                              (js->clj :keywordize-keys true))
                           new-props (merge current-defaults (camelify-map-keys default-props))]
                       (aset react-class "defaultProps" (clj->js new-props))))

                   (defn table-cell-clicked? [e]
                     "Sometimes .stopPropagation doesn't work in material-ui tables"
                     (instance? js/HTMLTableCellElement (aget e "target")))

                   (defn table-row-nav-to-fn [& args]
                     (fn [e]
                       (when (table-cell-clicked? e)
                         (apply nav-to! args))))

                   (defn nav-to-fn [& args]
                     (fn [e]
                       (apply nav-to! args)))

                   (def first-word
                     (memoize (fn [x]
                                (first (string/split x #" ")))))

                   (def butlast-word
                     (memoize (fn [x]
                                (let [words (string/split x #" ")]
                                  (if (> (count words) 1)
                                    (string/join " " (butlast words))
                                    x)))))

                   (defn sort-by-desc [key-fn coll]
                     (sort-by key-fn #(compare %2 %1) coll))

                   (defn sort-desc [coll]
                     (sort #(compare %2 %1) coll))

                   (defn sort-in-dir [dir coll]
                     (case dir
                       :desc (sort-desc coll)
                       :asc (sort coll)
                       coll))

                   (defn sort-paginate-ids [{:keys [offset limit sort-dir]} ids]
                     (if (and offset limit)
                       (->> ids
                         (sort-in-dir sort-dir)
                         (paginate offset limit))
                       ids))

                   (defn pos-or-zero? [x]
                     (let [x (parse-float x)]
                       (or (pos? x) (zero? x))))

                   (defn get-time [x]
                     (.getTime x))

                   (def date->sol-timestamp (comp timestamp-js->sol get-time))

                   (defn week-ago []
                     (t/minus (t/today-at-midnight) (t/weeks 1)))

                   (defn remove-zero-chars [s]
                     (string/join (take-while #(< 0 (.charCodeAt % 0)) s)))

                   (defn alphanumeric? [x]
                     (re-matches #"[a-zA-Z0-9 ]*" x))

                   (defn etherscan-url [address]
                     (gstring/format "https://etherscan.io/address/%s" address))

                   (defn prepend-address-zeros [address]
                     (let [n (- 42 (count address))]
                       (if (pos? n)
                         (->> (subs address 2)
                           (str (string/join (take n (repeat "0"))))
                           (str "0x"))
                         address)))

                   (defn unzip-map [m]
                     [(keys m) (vals m)])

                   (defn conj-colls [colls coll]
                     (map (fn [[i c]]
                            (conj c (nth coll i))) (medley/indexed colls)))

                   (defn get-window-width-size [width]
                     (cond
                       (nil? width) 0
                       (>= width 1200) 3
                       (>= width 1024) 2
                       (>= width 768) 1
                       :else 0))

                   (defn to-locale-string [x max-fraction-digits]
                     (let [parsed-x (cond
                                      (string? x) (parse-float x)
                                      (nil? x) ""
                                      :else x)]
                       (if-not (js/isNaN parsed-x)
                         (.toLocaleString parsed-x js/undefined #js {:maximumFractionDigits max-fraction-digits})
                         x)))

                   (defn with-currency-symbol [value currency]
                     (case currency
                       1 (str (constants/currencies 1) value)
                       (str value (constants/currencies currency))))

                   (defn number-fraction-part [x]
                     (let [frac (second (string/split (str x) #"\."))]
                       (if frac
                         (str "." frac)
                         "")))

                   (defn format-currency [value currency & [{:keys [:full-length? :display-code?]}]]
                     (let [value (-> (or value 0)
                                   big-num->num)
                           value (if full-length?
                                   (str (to-locale-string (js/parseInt value) 0) (number-fraction-part value))
                                   (to-locale-string value (if (= currency 0) 3 2)))]
                       (if display-code?
                         (str value " " (name (constants/currency-id->code currency)))
                         (with-currency-symbol value currency))))

                   (defn united-states? [country-id]
                     (= 232 country-id))

                   (defn empty-or-valid-email? [s]
                     (or (empty? s)
                         (email-address/isValidAddress s)))

                   (defn split-include-empty [s re]
                     (butlast (string/split (str s " ") re)))


                   (defn uncapitalize [s]
                     (str (string/lower-case (first s)) (subs s 1)))

                   (defn filter-by-namespace [nmsp coll]
                     (let [nmsp (name nmsp)]
                       (filter #(= (namespace %) nmsp) coll)))

                   (defn filter-by-namespaces [nmsps coll]
                     (let [nmsps (set (map name nmsps))]
                       (filter #(contains? nmsps (namespace %)) coll)))

                   (defn remove-by-namespace [nmsp coll]
                     (let [nmsp (name nmsp)]
                       (remove #(= (namespace %) nmsp) coll)))

                   (defn remove-by-namespaces [nmsps coll]
                     (let [nmsps (set (map name nmsps))]
                       (remove #(contains? nmsps (namespace %)) coll)))

                   (defn distinct-namespaces [ks]
                     (distinct (map namespace ks)))

                   (defn ascii-char? [c]
                     (< (.charCodeAt c) 128))

                   (defn estimate-string-gas [s]
                     (if s
                       (let [ascii-freqs (frequencies (map ascii-char? s))]
                         (+ (* (get ascii-freqs true 0) 800)
                            (* (get ascii-freqs false 0) 1550)))
                       0))

                   (defn linkedin-url? [s]
                     (let [{:keys [:host :path]} (url/url s)]
                       (and (or (= host "www.linkedin.com") (= host "linkedin.com"))
                            (string/starts-with? path "/in/"))))

                   (defn parse-linkedin-url-id [s]
                     (let [{:keys [:path]} (url/url s)]
                       (string/replace path #"/in/" "")))

                   (defn get-my-linkedin-profile-url [on-success & [on-error]]
                     (js/IN.User.authorize (fn []
                                             (.. (js/IN.API.Profile "me")
                                                 (fields "public-profile-url")
                                                 (result (fn [res]
                                                           (-> (js->clj res :keywordize-keys true)
                                                             :values
                                                             first
                                                             :publicProfileUrl
                                                             (string/replace #"https://www.linkedin.com/in/" "")
                                                             on-success)))
                                                 (error on-error)))))

                   (defn get-my-github-username [on-success]
                     (.. js/OAuth
                         (popup "github")
                         (done (fn [res]
                                 (.. res
                                     (me)
                                     (done (fn [res]
                                             (on-success (aget res "alias"))))
                                     (fail (fn [err]
                                             (.error js/console err))))))
                         (fail (fn [err]
                                 (.error js/console err)))))


                   (defn currency->ether [value value-currency conversion-rates]
                     (if-not (= value-currency 0)
                       (/ value (conversion-rates value-currency))
                       value))

                   (defn ether->currency [value target-currency conversion-rates]
                     (if-not (= target-currency 0)
                       (* value (conversion-rates target-currency))
                       value))

                   (defn value-in-all-currencies [value value-currency conversion-rates]
                     (let [ether-value (currency->ether value value-currency conversion-rates)]
                       (cons
                         ether-value
                         (for [[target-currency rate] (sort-by key conversion-rates)]
                           (if (= value-currency target-currency)
                             value
                             (ether->currency ether-value target-currency conversion-rates))))))

                   (defn convert-currency [value value-currency target-currency conversion-rates & [format-opts]]
                     (let [value (big-num->num value)]
                       (if (and (not= target-currency 0)
                                (not (conversion-rates target-currency)))
                         (with-currency-symbol "" target-currency)
                         (if (= value-currency target-currency)
                           (format-currency value value-currency format-opts)
                           (if (or (conversion-rates value-currency)
                                   (= 0 value-currency))
                             (let [value (parse-float value)]
                               (-> (if (and value (not (js/isNaN value))) value 0)
                                 (currency->ether value-currency conversion-rates)
                                 (ether->currency target-currency conversion-rates)
                                 (format-currency target-currency format-opts)))
                             (with-currency-symbol "" target-currency))))))

                   (defn num->wei [value]
                     (web3/to-wei (if (string? value) (replace-comma value) value) :ether))

                   (defn hours-decimal [hours minutes]
                     (+ hours (/ minutes 60)))

                   (defn currency-full-name [currency-id]
                     (when currency-id
                       (str (constants/currencies currency-id)
                            " "
                            (name (constants/currency-id->code currency-id)))))

                   (def http-url-pattern #"(?i)^(?:(?:https?)://)(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,}))\.?)(?::\d{2,5})?(?:[/?#]\S*)?$")

                   (defn http-url? [x & [{:keys [:allow-empty?]}]]
                     (if (and allow-empty? (empty? x))
                       true
                       (when (string? x)
                         (boolean (re-matches http-url-pattern x)))))

                   (defn create-length-validator
                     ([max-length] (create-length-validator 0 max-length))
                     ([min-length max-length]
                      (if max-length
                        (fn [x]
                          (<= (or min-length 0)
                              (if (string? x) (count (string/trim x)) 0)
                              max-length))
                        (constantly true))))
