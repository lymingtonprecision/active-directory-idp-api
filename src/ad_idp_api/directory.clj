(ns ad-idp-api.directory
  (:require [com.stuartsierra.component :as component]
            [clj-ldap.client :as ldap]
            [ad-idp-api.directory.dns :refer :all]
            [ad-idp-api.user :as user]
            [ad-idp-api.client-app :as client]))

(defprotocol OAuthDirectory
  (lookup-user [this id]
   "Returns the user record or nil")
  (authenticate-user [this id pw]
   "Returns the authenticated user or nil on failure")

  (create-client [this name] [this name url]
   "Creates and returns a new client")
  (lookup-client [this id]
   "Returns the client record or nil")
  (list-clients [this]
   "Returns a seq of all clients defined in the directory")
  (authenticate-client [this id secret]
   "Returns the authenticated client or nil on failure")
  (replace-client-secret [this id]
   "Replaces the specified clients secret with a new one, returns the
   updated client record")
  (rename-client [this id new-name]
   "Renames the specified client, returns the updated record or nil if
   there is no such client. Throws an `LDAPException` if there is a
   naming conflict.")
  (delete-client [this id]
   "Removes the specified client from the directory"))

(defn- if-client
  "If client `id` can be found in directory `d` call the given fn `f`
  with the directory connection and client record"
  [d id f]
  (if-let [c (:connection-pool d)]
    (if-let [cl (lookup-client d id)]
      (f c cl))))

(defrecord ActiveDirectory [config password-encryptor]
  component/Lifecycle
  (start [this]
    (if (:connection-pool this)
      this
      (-> this
          (set-base-dns config)
          (assoc :connection-pool (ldap/connect config)))))
  (stop [this]
    (if-let [p (:connection-pool this)]
      (do (.close p)
          (dissoc this :connection-pool))
      this))

  OAuthDirectory
  (lookup-user [this id]
    (user/lookup-user (:connection-pool this) (:search-base-dn this) id))
  (authenticate-user [this id pw]
    (let [c (:connection-pool this)
          u (user/lookup-user c (:search-base-dn this) id)]
      (if (user/authenticate-user c (:password-encryptor this) u pw)
        u)))

  (create-client [this name] (create-client this name nil))
  (create-client [this name url]
    (client/create-client (:connection-pool this)
                          (:client-base-dn this)
                          name url))
  (list-clients [this]
    (client/list-clients (:connection-pool this) (:client-base-dn this)))
  (lookup-client [this id]
    (client/lookup-client (:connection-pool this) (:search-base-dn this) id))
  (authenticate-client [this id secret]
    (if-client this id (fn [c cl]
                         (if (client/authenticate-client c cl secret)
                           cl))))
  (replace-client-secret [this id]
    (if-client this id client/replace-secret))
  (rename-client [this id new-name]
    (if-client this id #(client/rename-client %1 %2 new-name)))
  (delete-client [this id]
    (if-client this id client/delete-client)))

(defn active-directory []
  (component/using
    (map->ActiveDirectory {})
    [:config :password-encryptor]))
