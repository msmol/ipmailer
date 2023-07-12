(ns ipmailer.core
  (:import [org.apache.commons.validator.routines InetAddressValidator])
  (:require [clj-http.client :as client]
            [postal.core :as postal]
            [dotenv :refer [env]]
            [clojure.string :refer [blank?]]
            [taoensso.timbre :refer [debug info warn fatal]])
  (:gen-class))

(defn get-ip
  []
  (-> "https://api.ipify.org"
      (client/get {:throw-exceptions false})
      (get :body)))

(defn ipv4?
  [ip]
  (-> (InetAddressValidator.)
      (.isValid ip)))

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

(defn write-ip-to-fs
  [ip]
  (try
    (spit "/etc/public-ip" ip)
    (catch Exception _ (warn "Cannot write to /etc/public-ip; IP will not be saved on app restart."))))

(defn handle-ip-change
  [old-ip
   actual-ip]
  (do
    (write-ip-to-fs actual-ip)
    (if (= old-ip "Unknown")
      (debug "Not sending email, app startup")
      (do
        (send-email old-ip actual-ip)))))

(defn get-sleep-time
  []
  (Long/parseLong ((get-config) :poll-interval)))

(defn check-ip
  [old-ip]
  (let [new-ip (get-ip)
        actual-ip (if (ipv4? new-ip) new-ip old-ip)]
    (do
      (if (not (ipv4? new-ip))
        (warn "Got invalid IP from ipify, not updating"))
      (debug (str "IP was " old-ip " and now is " actual-ip))
      (if (not= actual-ip old-ip)
        (if (ipv4? actual-ip)
          (handle-ip-change old-ip actual-ip)
          (warn "Got invalid IP from ipify, not updating")))
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

(defn get-ip-from-fs
  []
  (try (slurp "/etc/public-ip")
       (catch Exception _ nil)))

(defn -main
  []
  (do
    (info "App started")
    (validate-config (get-config))
    (check-ip (or (get-ip-from-fs) "Unknown"))))

