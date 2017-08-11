(ns twiteth.pages.about-page
  (:require
    [cljs-react-material-ui.reagent :as ui]
    [twiteth.components.misc :as misc :refer [col row row-plain  link a paper]]
    [twiteth.styles :as styles]
    [re-frame.core :refer [subscribe dispatch]]
    [twiteth.utils :as u]))

(defn about-page []
    (fn []
       [misc/center-layout
        [paper {:zDepth 3}
          [:h2 "About Us"]
          [:p "Twiteth is built on blockchain using cryptocurrency for payments."]
          [:p "It is running on a " [link "https://ethereum.org/" "Ethereum"] " blockchain with the front-end
               written in " [link "https://clojurescript.org/" "ClojureScript."]]
          [:p   "Twiteth is open-source and you can find its code on " [link "https://github.com/vefjun/twiteth" "github.com/vefjun/twiteth"]
                " If you find a bug: " [link  "https://github.com/vefjun/twiteth/issues/new" "open an issue."]]]]))
