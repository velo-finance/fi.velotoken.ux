(defproject fi.velotoken.ux "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.773"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [thheller/shadow-cljs "2.11.7"]
                 [reagent "0.10.0"]
                 [re-frame "1.1.2"]
                 [akiroz.re-frame/storage "0.1.4"]
                 [cljs-http "0.1.46"]
                 [camel-snake-kebab "0.4.2"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [binaryage/oops "0.7.0"]
                 [reagent-forms "0.5.44"]
                 [org.clojars.akiel/async-error "0.3"]]


  :plugins [[lein-shadow "0.3.1"]
            [lein-shell "0.5.0"]
            [lein-less "1.7.5"]
            [lein-cljfmt "0.7.0"]]

  :less {:source-paths ["less"]
         :target-path  "resources/public/css"}

  :min-lein-version "2.9.0"

  :source-paths ["src/clj" "src/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :shadow-cljs {:nrepl {:port 8777}
                
                :builds {:app {:target :browser
                               :output-dir "resources/public/js/compiled"
                               :asset-path "/js/compiled"
                               :modules {:app {:init-fn fi.velotoken.ux.core/init
                                               :preloads [devtools.preload
                                                          re-frisk.preload]}}

                               :compiler-options {}
                               :devtools {:http-root "resources/public"
                                          :http-port 8280
                                          }}}}
  
  :shell {:commands {"karma" {:windows         ["cmd" "/c" "karma"]
                              :default-command "karma"}
                     "open"  {:windows         ["cmd" "/c" "start"]
                              :macosx          "open"
                              :linux           "xdg-open"}}}

  :aliases {"dev"          ["do" 
                            ["shell" "echo" "\"DEPRECATED: Please use lein watch instead.\""]
                            ["watch"]]
            "watch"        ["with-profile" "dev" "do"
                            ["shadow" "watch" "app" "browser-test" "karma-test"]]

            "prod"         ["do"
                            ["shell" "echo" "\"DEPRECATED: Please use lein release instead.\""]
                            ["release"]]

            "release"      ["with-profile" "prod" "do"
                            ["shadow" "release" "app"]]

            "build-report" ["with-profile" "prod" "do"
                            ["shadow" "run" "shadow.cljs.build-report" "app" "target/build-report.html"]
                            ["shell" "open" "target/build-report.html"]]

            "karma"        ["do"
                            ["shell" "echo" "\"DEPRECATED: Please use lein ci instead.\""]
                            ["ci"]]
            "ci"           ["with-profile" "prod" "do"
                            ["shadow" "compile" "karma-test"]
                            ["shell" "karma" "start" "--single-run" "--reporters" "junit,dots"]]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "1.0.2"]
                   [cider/cider-nrepl "0.24.0"]
                   [re-frisk "1.3.4"]
                   ]

    :source-paths ["dev"]

    }

   :prod {}
   
}

  :prep-tasks [["less" "once"]])
