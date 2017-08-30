(defproject twiteth "0.1.0-SNAPSHOT"
  :dependencies [[akiroz.re-frame/storage "0.1.2"]
                [bk/ring-gzip "0.1.1"]
                [bidi "2.1.1"]
                [cljs-ajax "0.6.0"]
                [com.cemerick/url "0.1.1"]
                [cljs-react-material-ui "0.2.48"]
                [cljs-web3 "0.19.0-0-5"]
                [compojure "1.6.0"]
                [environ "1.0.3"]
                [http-kit "2.2.0"]
                [madvas.re-frame/web3-fx "0.1.12"]
                [madvas.re-frame/google-analytics-fx "0.1.0"]
                [cljsjs/bignumber "2.1.4-1"]
                [cljsjs/linkify "2.1.4-0" :exclusions [cljsjs/react]]
                [cljsjs/material-ui-chip-input "0.15.0-0"]
                [cljsjs/react-flexbox-grid "1.0.0-0" :exclusions [cljsjs/react cljsjs/react-dom]]
                [medley "1.0.0"]
                [com.andrewmcveigh/cljs-time "0.4.0"]
                [org.clojure/clojurescript "1.9.671"]
                [org.clojure/core.async "0.3.443"]
                [re-frame "0.10.1" :exclusions [reagent]]
                [day8.re-frame/async-flow-fx "0.0.8"]
                [day8.re-frame/http-fx "0.1.4"]
                [print-foo-cljs "2.0.3"]
                [reagent "0.7.0" :exclusions [cljsjs/react cljsjs/react-dom]]
                ;;[madvas/reagent-patched "0.6.1"]
                [ring.middleware.logger "0.5.0"]
                [ring/ring-core "1.6.1"]
                [ring/ring-defaults "0.3.0"]
                [ring/ring-devel "1.6.1"]]

  :plugins [[lein-auto "0.1.3"]
            [lein-cljsbuild "1.1.4"]
            [lein-shell "0.5.0"]
            [deraen/lein-less4j "0.6.2"]]

  :min-lein-version "2.5.3"
  :main twiteth.core
  :source-paths ["src/clj" "src/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :auto {"compile-solidity" {:file-pattern #"\.(sol)$"
                             :paths ["resources/public/contracts/src"]}}

  :less {:source-paths ["resources/public/less"]
         :target-path "resources/public/css"
         :target-dir "resources/public/css"
         :source-map true
         :compression true}

  :aliases {"compile-solidity" ["shell" "./compile-solidity.sh"
                                       "start-testrpc" ["shell" "./start-testrpc.sh"]]}

  :figwheel {:css-dirs ["resources/public/css"]}
  :profiles {:dev
             {:dependencies [[org.clojure/clojure "1.8.0"]
                             [binaryage/devtools "0.9.4"]
                             [com.cemerick/piggieback "0.2.1"]
                             [figwheel-sidecar "0.5.10"]
                             [org.clojure/tools.nrepl "0.2.13"]]
              :plugins [[lein-figwheel "0.5.10"]]
              :source-paths []
              :resource-paths ["resources"]
              :cljsbuild {:builds [{:id "dev"
                                    :source-paths ["src/cljs"]
                                    :figwheel {:on-jsload "twiteth.core/mount-root"}
                                    :compiler {:main "twiteth.core"
                                               :output-to "resources/public/js/compiled/app.js"
                                               :output-dir "resources/public/js/compiled/out"
                                               :asset-path "js/compiled/out"
                                               :source-map-timestamp true
                                               :preloads [print.foo.preloads.devtools]
                                               :closure-defines {goog.DEBUG true}
                                               :external-config {:devtools/config {:features-to-install :all}}}}
                                   {:id "min"
                                    :source-paths ["src/cljs"]
                                    :compiler {:main "twiteth.core"
                                               :output-to "resources/public/js/compiled/app.js"
                                               :optimizations :advanced
                                               :closure-defines {goog.DEBUG false}
                                               :pretty-print false
                                               :pseudo-names false}}]}}}

  )
