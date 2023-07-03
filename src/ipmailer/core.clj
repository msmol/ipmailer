(ns ipmailer.core
  (:require [clj-http.client :as client]
            [postal.core :as postal]
            [dotenv :refer [env]]
            [clojure.string :refer [blank?]]
            [taoensso.timbre :refer [debug info fatal]])
  (:gen-class))

(defn get-ip
  []
  (-> "https://api.ipify.org"
      (client/get)
      (get :body)))

(defn kill-app
  [reason]
  (do
    (fatal reason)
    (System/exit 1)))

(defn not-boolean
  [val]
  (and
   (not= val "true")
   (not= val "false")))

(defn invalid-port?
  [val]
  (not (re-matches #"\d+" val)))

(defn get-config
  []
  {:host (env "IPMAILER_SMTP_HOST")
   :user (env "IPMAILER_SMTP_USER")
   :pass (env "IPMAILER_SMTP_PASS")
   :port (env "IPMAILER_SMTP_PORT")
   :tls (env "IPMAILER_SMTP_TLS")
   :from (env "IPMAILER_SMTP_FROM")
   :to (env "IPMAILER_SMTP_TO")
   :poll-interval (or (env "IPMAILER_POLL_INTERVAL") "30000")})

(defn get-postal-settings
  [old-ip actual-ip]
  (let [config (get-config)]
    [{:host (config :host)
      :user (config :user)
      :pass (config :pass)
      :port (Integer/parseInt (config :port))
      :tls (boolean (Boolean/valueOf (config :tls)))}
     {:from (config :from)
      :to (config :to)
      :subject "Home IP Address change"
      :body (str "Your home IP address has changed from " old-ip " to " actual-ip
                 "\n\nThanks,\nYour friendly neighbourhood IP bot")}]))

(defn send-email
  [old-ip
   actual-ip]
  (let [[smtp-settings email-settings] (get-postal-settings old-ip actual-ip)]
    (do
      (info (str "Sending email, IP has changed from " old-ip " to " actual-ip))
      (postal/send-message smtp-settings email-settings))))

(defn handle-ip-change
  [old-ip
   actual-ip]
  (do
    (if (= old-ip "Unknown")
      (debug "Not sending email, app startup")
      (send-email old-ip actual-ip))))

(defn get-sleep-time
  []
  (Long/parseLong ((get-config) :poll-interval)))

(defn check-ip
  [old-ip]
  (let [actual-ip (get-ip)]
    (do
      (debug (str "IP was " old-ip " and now is " actual-ip))
      (if (not= actual-ip old-ip)
        (handle-ip-change old-ip actual-ip))
      (Thread/sleep (get-sleep-time))
      (recur actual-ip))))

(defn validate-config
  [config]
  (do
    (if (some blank? (vals config))
      (kill-app "Please provide all settings as environment variables (can use .env)"))
    (if (not-boolean (config :tls))
      (kill-app "Please provide valid value for IPMAILER_SMTP_TLS: <boolean>"))
    (if (invalid-port? (config :port))
      (kill-app "Please provide valid valuefor IPMAILER_SMTP_PORT: <number>"))
    (if-not (re-matches #"\d+" (config :poll-interval))
      (kill-app "Please provide a valid value for IPMAILER_POLL_INTERVAL: <int>"))))

(defn -main
  []
  (do
    (info "App started")
    (validate-config (get-config))
    (check-ip "Unknown")))
