(ns ad-idp-api.group
  (:require [ad-idp-api.ldap-util :as u]))

(defn memberOf->groups
  "Converts a collection of group DNs, as returned for the LDAP `memberOf`
  attribute, into a collection of group records"
  [mof]
  (map
    (fn [dns]
      (let [dn (u/make-dn-instance dns)
            pdn (u/parent-dn dn)]
        {:dn (.toString dn)
         :name (u/dn->cn dn)
         :app {:dn (.toString pdn)
               :name (u/dn->cn pdn)}}))
    (u/ensure-collection mof)))
