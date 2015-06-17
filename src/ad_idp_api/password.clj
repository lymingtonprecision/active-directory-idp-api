(ns ad-idp-api.password
  (:require [clojurewerkz.scrypt.core :as sc]))

(defprotocol PasswordEncryptor
  (encrypt [e s] "Returns an encrpyted hash for the given string")
  (verify? [e s h] "Returns true/false if the string s matches the encrypted value h"))

(defrecord Plaintext []
  PasswordEncryptor
  (encrypt [this s] s)
  (verify? [this s h] (= s h)))

(defrecord Scrypt [salt o]
  PasswordEncryptor
  (encrypt [this s]
    (let [{:keys [cpu mem p] :or {cpu 16384 mem 8 p 1}} o]
      (sc/encrypt (str salt s salt) cpu mem p)))
  (verify? [this s h]
    (sc/verify (str salt s salt) h)))
