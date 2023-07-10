(defproject ipmailer "0.2.0"
  :description "Emails an address when IP has changed"
  :url "https://github.com/msmol/ipmailer"
  :license {:name "MIT"
            :url "https://opensource.org/license/mit/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [com.draines/postal "2.0.5"]
                 [clj-http "3.12.3"]
                 [lynxeyes/dotenv "1.1.0"]
                 [com.taoensso/timbre "6.2.1"]]
  :main ^:skip-aot ipmailer.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
