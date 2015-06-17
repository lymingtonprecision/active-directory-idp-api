(ns ad-idp-api.user
  (:require [clj-ldap.client :as ldap]
            [ad-idp-api.ldap-util :refer [escape-ldap-value ensure-collection]]
            [ad-idp-api.user.flags :as flags]
            [ad-idp-api.group :as group]))

(def user-attributes
  #{"dn"
    "objectClass"
    "mail"
    "name"
    "flags"
    "userAccountControl"
    "o"
    "memberOf"})

(defn ldap->user
  "Converts an LDAP attribute map to a user map
  Returns nil on nil or empty input"
  [r]
  (when-not (or (nil? r) (empty? r))
    (let [u? (or (some #(= % "user") (:objectClass r)) false)
          f (get r (if u? :userAccountControl :flags))]
      {:dn (:dn r)
       :login (:mail r)
       :name (:name r)
       :directory-user? u?
       :disabled? (flags/account-disabled? f)
       :password-expired? (flags/password-expired? f)
       :organizations (ensure-collection (:o r))
       :memberships (group/memberOf->groups (:memberOf r))})))

(def filter-disabled-users
  "LDAP filter string for excluding disabled user accounts"
  (str "(&"
       "(!(userAccountControl:1.2.840.113556.1.4.803:=2))"
       "(!(flags:1.2.840.113556.1.4.803:=2))"
       ")"))

(defn filter-username
  "Generates an LDAP filter string for a given user"
  [u]
  (str "(&"
       "(objectClass=User)"
       "(sAMAccountName=" (escape-ldap-value u) "))"))

(defn filter-email-address
  "Generates an LDAP filter string for a records primary email address"
  [addr]
  (str "(mail=" (escape-ldap-value addr) ")"))

(defn lookup-user
  "Returns the requested user record from the directory or nil

  * `d` must be an active LDAP connection
  * `b` must be a base DN string from which to perform the search
  * `u` should be either a username or email address associated with the user

  `o` is an optional list of options, currently only `:include-disabled`
  is supported, if truthy disabled accounts will be returned otherwise
  only enabled accounts will be looked up"
  [d b u & o]
  (let [uf (if (>= (.indexOf u "@") 0)
             (filter-email-address u)
             (filter-username u))
        f (if (:include-disabled (apply array-map o))
            uf
            (str "(&" uf filter-disabled-users ")"))
        r (ldap/search d b {:filter f :attributes user-attributes})]
    (ldap->user (first r))))

(defn fetch-password-hash
  "Returns the current password hash for the given DN
  `d` must be an active LDAP connection"
  [d dn]
  (:userPassword (ldap/get d dn #{"userPassword"})))

(defn authenticate-directory-user
  "Authenticates the user against the directory by performing a bind using
  the supplied credentials, returning a boolean result

  * `d` must be an active LDAP connection pool
  * `u` must be a user record with a `:dn` entry
  * `p` should be the plaintext password"
  [d u p]
  (ldap/bind? d (:dn u) p))

(defn authenticate-non-directory-user
  "Authenticates a user by cryptographic password comparison, returns a
  boolean result

  * `d` must be an active LDAP connection
  * `e` must be a `PasswordEncryptor` configured as per that used encrypt the
  * users stored password
  * `u` must be a user record with a `:dn` entry
  * `p` should be the users plaintext password"
  [d e u p]
  (let [h (fetch-password-hash d (:dn u))]
    (and e h (.verify? e p h))))

(defn authenticate-user
  "Authenticates a directory user, returning a boolean result

  * `d` must be an active LDAP connection
  * `e` must be a `PasswordEncryptor`, as used to encrypt the users stored password
  * `u` must be a user record, as returned by `lookup-user` or `ldap->user`
  * `p` should be the plaintext password to authenticate"
  [d e u p]
  (cond
    (:directory-user? u) (authenticate-directory-user d u p)
    (= (:directory-user? u) false) (authenticate-non-directory-user d e u p)
    :else false))
