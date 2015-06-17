(ns ad-idp-api.directory.dns
  (:require [clj-ldap.client :as ldap]
            [ad-idp-api.ldap-util :as u]))

(defn default-client-base-dn
  "The default organizational unit DN under which clients will be created"
  [base-dn]
  (str "ou=Apps," base-dn))

(defn default-user-base-dn
  "The default organizational unit DN under which users will be created"
  [base-dn]
  (str "ou=Users," base-dn))

(defn set-base-dns
  "`assoc`s the various `:*-base-dn` entries used by a directory into the
  directory component map `d`

  `config` should be a map containing a `:base-dn` entry, which defines the
  root base DN used by the directory, and the following optional DNs:

  * `:search-base-dn` the base DN to use when performing searches
  * `:client-base-dn` the base DN under which client records will be created
  * `:user-base-dn` the base DN under which user records will be created

  Throws an exception if the `:base-dn` entry is missing."
  [d config]
  (if-let [root-base-dn (:base-dn config)]
    (assoc d
           :search-base-dn
           (get config :search-base-dn root-base-dn)
           :client-base-dn
           (get config :client-base-dn
                (default-client-base-dn root-base-dn))
           :user-base-dn
           (get config :user-base-dn
                (default-user-base-dn root-base-dn)))
    (throw (Exception. "no base-dn defined in configuration map"))))

(defn extract-base-dns
  "Returns a collection of the distinct base DN values defined for
  the directory `d`"
  [d]
  (->> (keys d)
       (filter (fn [k] (re-find #"base-dn$" (str k))))
       (select-keys d)
       vals
       distinct))

(defn create-ou!
  "Creates a new organizational unit in the directory.
  `c` must be an LDAP connection and `dn` the desired DN of the new OU"
  [c dn]
  (try
    (ldap/add c (.toString dn) {:objectClass #{"top" "organizationalUnit"}})
    (catch Exception e
      (throw (Exception. (str "failed creating missing base dn path "
                              "'" (.toString dn) "' /" (.getMessage e)))))))

(defn create-ou-for-dn-if-missing!
  "If the specified DN does not exist (and is above the level of `dc`)
  creates it (and any missing lower level DNs) as an OU.
  `c` must be an LDAP connection"
  [c dn]
  (loop [dns (u/dn->dnseq dn)]
    (let [[fdn & odn] dns]
      (if (u/dn-is-missing? c fdn) (create-ou! c fdn))
      (if (empty? odn) true (recur odn)))))

(defn create-missing-base-dns!
  "Creates organizational units for any missing base DN entries in directory `d`"
  [d]
  (doseq [dn (extract-base-dns d)]
    (create-ou-for-dn-if-missing! (:connection-pool d) dn)))
