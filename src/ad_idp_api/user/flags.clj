(ns ad-idp-api.user.flags)

(def flags
  {:account-disabled 0x0002
   :password-expired 0x8000000})

(defn flag-set?
  "Returns true if the bitwise flag `flag` is set in `ac`

  `flag` can either be a numeric value or a keyword from the `flags`
  map"
  [ac flag]
  (if ac
    (let [n (if (number? ac) ac (Integer/parseInt ac))]
      (pos? (bit-and (if (number? flag) flag (get flags flag)) n)))
    false))

(defmacro make-flag-fns
  "Create query fns for checking defined flags.
  Enables compile time validation of `flag?` calls as we can write:

      (flags/account-disabled? f)

  Instead of:

      (flags/flag-set? f :account-disabled)

  Making it much less likely that a typo will result in flags not
  being properly checked."
  []
  (cons `do
        (for [k (keys flags)]
          (let [mn (symbol (->> (str k "?") rest (apply str)))
                v (k flags)
                f (fn [ac] (flag-set? ac v))]
            `(def ~mn ~f)))))

(make-flag-fns)
