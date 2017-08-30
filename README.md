# twiteth

A Little Less Simple Decentralised Twitter.

A [re-frame](https://github.com/Day8/re-frame) application based on:

* [How to create decentralised apps with Clojurescript re-frame and Ethereum](https://medium.com/@matus.lestan/how-to-create-decentralised-apps-with-clojurescript-re-frame-and-ethereum-81de24d72ff5#.nvfyq27lb)

Has been tested on Ubuntu 16.10 with a local blockchain running on geth. See above link for setting up geth.


## Prepare App

Start Solidity auto compiling:
```
lein auto compile-solidity
```
Start less compiling:
```
lein less4j auto
```
## Run application

lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Production Build


To compile clojurescript to javascript:

```
lein clean
lein cljsbuild once min
```
```clojure
(twiteth.core/-main)
(figwheel-sidecar.repl-api/start-figwheel! (figwheel-sidecar.config/fetch-config))
(figwheel-sidecar.repl-api/cljs-repl)
```
Open at http://localhost:6655/
