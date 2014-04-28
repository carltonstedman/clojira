(ns clojira.core
  (:require [clj-http.client :as client]
            [clj-http.cookies :as cookies]
          ; [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [clojira.jql :as jql]
            [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))


;; ## Read in from config file
;;
(def config (edn/read-string (slurp "config.edn")))
(def auth [(config :username) (config :password)])
(def rest-url [(config :rest-url)])

(defn auth-url [route]
  (str rest-url "/auth/latest/" (name route)))

(defn api-url [route]
  (str rest-url "/api/2/" (name route)))

(defn get-session [auth cookie-jar]
  (client/get (auth-url :session) {:content-type :json
                                   :basic-auth auth
                                   :cookie-store cookie-jar}))

(defn get-issues [results]
  (-> results :body json/read-json :issues))

(defn jql-search [jql-query cookie-jar]
  (client/post (api-url :search) {:content-type :json
                                  :form-params {:jql jql-query}
                                  :cookie-store cookie-jar}))


(defn fetch-open-issues [assignee cookie-jar]
  ;(map #(list (-> % :key) %)
       (-> (jql-search (jql/and (jql/equals :assignee "cstedman")
                                (jql/not-equals :status "closed"))
                       cookie-jar)
           get-issues));)

(def issues
  (let [cookie-jar (cookies/cookie-store)]
    (get-session auth cookie-jar)
    (let [open-issues (fetch-open-issues "cstedman" cookie-jar)]
      open-issues)))
      ;(zipmap (map first open-issues) (mapcat rest open-issues)))))

;; :key

;; :fields
{:description :description
 :summary :summary
 :original-estimate :timeoriginalestimate
 :time-spent :timespent
 :subtasks :subtasks
 :resolution [:resolution :name]
 :project [:project :name]}

{[] [:key :id]
 :fields [:description :summary :timeestimate :timespent :subtasks
          :aggregateprogress :progress :aggregatetimespent]}

(def hsh
 {:description [:description]
  :summary [:summary]
  :progress [:progress]
  :resolution [:resolution :name]
  :project [:project :name]
  :status [:status :name]
  :reporter [:reporter :name]
  :priority [:priority :name]
  :assignee [:assignee :name]
  :squad [:customfield_11130 :value]})

(def fields
  [:description
   :summary
   :progress])
   ;;:subtasks
   ;;:aggregateprogress
   ;;:timeestimate
   ;;:timespent
   ;;:aggregatetimespent])

;; Take a mapping into fields and an issue and return a
;; map of the keys of mapping mapped to the deeply fetched
;; values.
;;
(defn parse-fields [mapping issue]
  (reduce #(assoc %1 (%2 0) (%2 1))
          {}
          (map #(let [k (% 0) v (% 1)]
                 (vector k (get-in issue (into [:fields] v))))
               (seq mapping))))

(defn parse-issues [mapping issues]
  (reduce #(assoc %1 (%2 0) (%2 1))
          {}
          (map #(let [fields (parse-fields mapping %)]
                 (vector (keyword (:key %))
                         (assoc fields :id (:id %))))
               issues)))

(pp/pprint (reduce #(assoc %1 (%2 0) (%2 1))
                   {}
                   (filter #(or (= (:resolution (% 1)) "Fixed")
                                (= (:status (% 1) "Resolved")))
                           (parse-issues hsh issues)))
           (io/writer "quux.edn"))

;; :fields :parent

;; worklogs?

(defn parse-issue [issue]
  (let [top (select-keys issue [:key :id :fields])
        fields-top (select-keys (top :fields) )])

(pp/pprint issues (io/writer "search.edn"))
(pp/pprint (nth issues 3) (io/writer "foo.edn"))

(get-in (nth issues 3) [:fields :parent :fields :summary])

(select-keys (get-in (nth issues 3) [:fields]) [:timespent])

(map #(select-keys (% :fields) [:description]) issues)

(select-keys (first issues) [:key :summary])
