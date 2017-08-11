(ns twiteth.pages.how-it-works-page
  (:require
    [cljs-react-material-ui.reagent :as ui]
    [twiteth.components.misc :as misc :refer [col row paper row-plain link a youtube]]
    [twiteth.styles :as styles]))

(defn video [{:keys [:title :src]}]
  [row-plain
   {:center "xs"}
   [:h2
    {:style (merge styles/margin-bottom-gutter
                   styles/margin-top-gutter-more)}
    title]
   [youtube {:src src}]])

(defn how-it-works-page []
  [misc/center-layout
   [paper {:zDepth 3}
    [:h2 "How does it work?"]
    [:p "Twiteth is running on the " [link "https://ethereum.org/" "Ethereum"] " public blockchain,
    therefore you'll need the " [link "https://metamask.io/" "MetaMask"] " browser extension to be able to make
    changes into the blockchain. See the video tutorial below:"]
    [video
     {:title "Installing MetaMask Chrome Extension"
      :src "https://www.youtube.com/embed/gUZ_XT0a9_U?list=PL4rQUoitSeEH8ybx-yM1ocuvF9OvSVkU4"}]

    [:h1
     {:style (merge styles/margin-top-gutter-more
                    styles/text-center
                    styles/margin-bottom-gutter)}
     "Frequently Asked Questions"]
    [:h3.bolder "How do I get Ether (Ξ) cryptocurrency?"]
    [:p "Obtaining Ether is very similar to obtaining " [link "https://en.wikipedia.org/wiki/Bitcoin" "Bitcoin"]
     ". The most common way is to register at one of the worldwide
    cryptocurrency exchanges that exchange fiat currency for cryptocurrency. Note that exchange from
    Bitcoin to Ether can be done directly in " [link "https://metamask.io/" "MetaMask"] " or "
     [link "https://github.com/ethereum/mist" "Mist browser"] "."]
    [:h3.bolder "Why do I have to pay Ethereum gas fees?"]
    [:p "Every time you'll want to change something in the twiteth database you'll be asked to pay a small fee (usually a couple of cents)
     called \"gas fees\". This fee is used to compensate for the electricity costs of the computers running the Ethereum blockchain.
     Thanks to this, twiteth doesn't need to rent servers and therefore keeps service fees as low as 0%! This fee is a
     great protection against spam. " [:u "Gas fees are by no means profit of twiteth"] "."]]])
