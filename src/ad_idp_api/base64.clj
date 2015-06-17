(ns ad-idp-api.base64
  (:require [clojure.string :as str]
            [clojure.data.codec.base64 :as b64]))

(defn remove-padding [s]
  (str/replace s #"=*$" ""))

(defn pad-string [s]
  (let [b (rem (count s) 3)
        p (case b
            1 "=="
            2 "="
            nil)]
    (str s p)))

(defn b64string->escaped-hex-string [s]
  (->> (.getBytes s)
       b64/decode
       (map
         (fn [h]
           (let [i (.intValue (bit-and h 0xFF))
                 p (if (< i 0xf) "0" "")
                 s (java.lang.Integer/toHexString i)]
             (str "\\" p s))))
       (apply str)))
