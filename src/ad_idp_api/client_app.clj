(ns ad-idp-api.client-app
  (:require [clojure.string :as str]
            [clj-ldap.client :as ldap]
            [ad-idp-api.base64 :as b64]
            [ad-idp-api.ldap-util :as u]
            [ad-idp-api.group :as group]))

(def client-attributes
  #{"dn" "name" "objectGUID" "userPassword" "url" "memberOf"})

(def ou-attribute-map {:objectClass #{"top" "organizationalUnit"}})

(defn ldap->client
  "Converts an LDAP attribute map to a client map
  Returns nil on nil or empty input"
  [r]
  (when-not (or (nil? r) (empty? r))
    {:dn (:dn r)
     :name (:name r)
     :client-id (b64/remove-padding (:objectGUID r))
     :client-secret (:userPassword r)
     :url (:url r)
     :memberships (group/memberOf->groups (:memberOf r))}))

(defn generate-secret
  "Generates a new random string for use as a client secret"
  []
  (let [sr (java.security.SecureRandom.)
        rb (java.math.BigInteger. 130 sr)]
    (.toString rb 32)))

(defn create-client
  "Creates a new client entry in the directory and returns the resulting
  client record.

  Only the clients `name` and `url` may be specified. The OAuth client ID
  and secret will be generated and included in the returned record."
  ([d parent-dn name] (create-client d parent-dn name nil))
  ([d parent-dn name url]
   (let [dn (str "ou=" (u/escape-ldap-value name) "," parent-dn)
         a (-> ou-attribute-map
               ((fn [a] (if url (assoc a :url (u/escape-ldap-value url)) a)))
               (assoc :userPassword (generate-secret)))
         get-client #(ldap->client (ldap/get d dn client-attributes))]
     (if-let [c (get-client)]
       c
       (do (ldap/add d dn a)
           (ldap/add d (str "ou=Groups," dn) ou-attribute-map)
           (get-client))))))

(defn list-clients
  "Returns a seq of all clients defined in the directory"
  [d base-dn]
  (let [f (str "(&(objectClass=organizationalUnit)(userPassword=*))")
        r (ldap/search d base-dn {:filter f :attribtues client-attributes})]
    (map ldap->client r)))

(defn lookup-client
  "Returns the client record for the specified client id, located under
  `base-dn` in the directory, or nil if no such client exists

  `d` must be a connection suitable for use in performing an LDAP search"
  [d base-dn id]
  (let [guid (-> (u/escape-ldap-value id)
                 b64/pad-string
                 b64/b64string->escaped-hex-string)
        f (str "(&(objectClass=organizationalUnit)"
               "(objectGUID=" guid "))")
        r (ldap/search d base-dn {:filter f :attributes client-attributes})]
    (ldap->client (first r))))

(defn authenticate-client
  "Verifies the provided secret against the client record, returning a boolean
  (`true` if the secret matches, `false` otherwise)

  `d` must be an LDAP connection
  `client` must be a client record as returned by `lookup-client`"
  [d client secret]
  (= (:client-secret client) secret))

(defn replace-secret
  "Replaces the clients secret with a newly generated one and returns
  the updated client record.
  `d` must be an LDAP connection and `c` the existing client record."
  [d c]
  (let [dn (:dn c)]
    (ldap/modify d dn {:replace {:userPassword (generate-secret)}})
    (ldap->client (ldap/get d dn client-attributes))))

(defn rename-client
  "Renames the client and returns it's updated client record.
  `d` must be an LDAP connection and `c` the existing client record."
  [d c new-name]
  (let [dn (:dn c)
        p (.toString (u/parent-dn dn))
        en (u/escape-ldap-value new-name)
        new-dn (str "ou=" en "," p)]
    (u/rename-dn d dn new-dn)
    (ldap->client (ldap/get d new-dn client-attributes))))

(defn delete-client
  "Delete the given client from the directory
  `d` must be an LDAP connection and `c` an existing client record."
  [d c]
  (ldap/delete d (:dn c) {:delete-subtree true}))
