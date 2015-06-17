(ns ad-idp-api.ldap-util
  (:require [clj-ldap.client :as ldap])
  (:import [com.unboundid.ldap.sdk DN Filter ModifyDNRequest]))

(defn escape-ldap-value [s] (Filter/encodeValue (str s)))

(defn ensure-collection [v] (if (or (nil? v) (coll? v)) v [v]))

(defn make-dn-instance
  "Returns an instance of the Unboundid SDK `DN` class for the dn"
  [dn]
  (if (instance? DN dn) dn (DN. dn)))

(defn dn->cn
  "Returns the value of the left most RDN from dn"
  [dn]
  (let [dno (make-dn-instance dn)]
    (-> (.getRDNs dno) first .getAttributeValues first)))

(defn parent-dn
  "Returns the parent dn of `dn`, as an instance of the Unboundid SDK `DN` class"
  [dn]
  (if (instance? DN dn)
    (.getParent dn)
    (DN/getParent dn)))

(defn domain-dn?
  "Returns true if `dn` is a domain dn (i.e. is defined by a `dc` attribute)"
  [dn]
  (re-find #"(?i)^dc=" (.toString dn)))

(defn dn->dnseq
  "Returns a sequence of the sub-DNs of dn

      (dn->dnseq \"dc=example,dc=co,dc=uk\")
      ;=> [\"dc=uk\" \"dc=co,dc=uk\" \"dc=example,dc=co,dc=uk\"]

  Each DN in the sequence is an instance of the Unboundid SDK `DN` class"
  [dn]
  (reverse (loop [c [] ldn (make-dn-instance dn)]
             (if (nil? ldn) c (recur (conj c ldn) (.getParent ldn))))))

(defn dn-is-missing?
  "Returns try if a `get` for the specified DN succeeds, false otherwise.
  Note that domain DNs are always assumed to exist, no `gets` are performed
  for them."
  [c dn]
  (and (not (domain-dn? dn))
       (nil? (ldap/get c (.toString dn) #{"dn"}))))

(defn rename-dn
  "Renames `dn` to `new-dn` in the directory. `new-dn` can either be a partial
  or full DN to optionally move/re-base the object within the directory as
  well as renaming it.

  Throws an `LDAPException` on error"
  [c dn new-dn]
  (let [rdn (make-dn-instance new-dn)
        mr (ModifyDNRequest. (make-dn-instance dn)
                             (.getRDN rdn)
                             true
                             (.getParent rdn))]
    (.modifyDN c mr)))
