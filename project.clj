(defproject ad-idp-api "0.1.0-SNAPSHOT"
  :description "An API to facilitate use of Active Directory as an OAuth IDP"
  :url "https://github.com/lymingtonprecision/active-directory-idp-api"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [environ "1.0.0"]
                 [com.stuartsierra/component "0.2.2"]
                 [clojurewerkz/scrypt "1.2.0"]
                 [org.clojars.ah45/clj-ldap "0.0.9"]]

  :plugins [[lein-environ "1.0.0"]]

  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.3"]
                                  [org.clojure/java.classpath "0.2.0"]
                                  [im.chit/vinyasa.pull "0.2.2"]]
                   :plugins [[lein-marginalia "0.8.0"]]}
             :repl {:source-paths ["dev"]}}

  :codox {:sources ["src"]
          :output-dir "doc/api"
          :src-dir-uri "https://github.com/lymingtonprecision/active-directory-idp-api/blob/master"
          :src-linenum-anchor-prefix "L"
          :defaults {:doc/format :markdown}}

  :aliases {"docs" ["do" "marg" "doc"]
            "marg" ["marg"
                    "-d" "doc"
                    ;; different ways to prefix documentation with a
                    ;; project "overview": include commented dev files
                    ;; showing usage or have an empty, bar documentation,
                    ;; project namespace file
                    ;"dev/user.clj"
                    ;"src/ad_idp_api.clj"
                    "src/ad_idp_api/directory.clj"
                    "src/ad_idp_api/directory/dns.clj"
                    "src/ad_idp_api/client_app.clj"
                    "src/ad_idp_api/user.clj"
                    "src/ad_idp_api/group.clj"
                    "src/ad_idp_api/user/flags.clj"
                    "src/ad_idp_api/password.clj"
                    "src/ad_idp_api/ldap_util.clj"
                    "src/ad_idp_api/base64.clj"]})
