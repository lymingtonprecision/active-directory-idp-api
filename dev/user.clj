(ns user
  (:require [clojure.string :as str]
            [clojure.pprint :refer (pprint)]
            [clojure.repl :refer :all]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)]
            [clojure.tools.logging :as log]
            ; 3rd party libraries
            [vinyasa.pull :refer [pull]]
            [com.stuartsierra.component :as component]
            [clj-ldap.client :as ldap]
            ; project namespaces
            [ad-idp-api.password :as password]
            [ad-idp-api.directory :as directory]
            [ad-idp-api.user :as idp-user]
            [ad-idp-api.client-app :as idp-client]))

(def ldap-config
  {:host ["dc.lymingtonprecision.co.uk:636",
          "actinium01.lymingtonprecision.co.uk:636"
          "actinium02.lymingtonprecision.co.uk:636"]
   :ssl? true
   :num-connections 3
   :connect-timeout (* 1000 5)
   :timeout (* 1000 30)
   :domain "lymingtonprecision.co.uk"
   :base-dn "ou=OAuth,ou=Development,ou=LPE,dc=lymingtonprecision,dc=co,dc=uk"
   :search-base-dn "dc=lymingtonprecision,dc=co,dc=uk"
   :bind-dn (str/join ","
                      ["cn=OAuth Service Account"
                       "ou=OAuth"
                       "ou=Development"
                       "ou=LPE"
                       "dc=lymingtonprecision,dc=co,dc=uk"])
   :password "vLJisyqXf3ZGDZZei7vw"})

(def system nil)

(defn init []
  (alter-var-root #'system
                  (constantly
                    (component/system-map
                      :config ldap-config
                      :password-encryptor (password/map->Scrypt
                                            {:salt "JE9Jg]m2cBFj"})
                      :directory (directory/active-directory)))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system component/stop))

(defn go []
  (init)
  (start)
  (str "Directory service connected"))

(defn reset []
  (if system (stop))
  (refresh :after 'user/go))
