(ns weekly-meetups.core
  (:require [clojure.data.json :as json])
  (:use [clj-time.core]
        [clj-time.coerce]
        [clj-time.format]
        [clj-time.local]
        [clostache.parser])
  (:gen-class))

;;configuration
(def number-of-weeks 1)

(def meetups
  {:brisbane ["clj-bne" 
              "qldjvm"
              "AWS-Brisbane"
              "Brisbane-Net-User-Group"
              "Brisbane-Functional-Programming-Group"
              "Brisbane-Hacks-for-Humanity"
              "BrisRuby"
              "BrisJS"
              "Agile-Brisbane"
              "hackbne"]
   :sydney []
   :melbourne []
   :perth []})

;;should need to change this
(def meetup-url 
  "http://api.meetup.com/2/events?sign=true&key=%s&group_urlname=%s&time=01012014,%dw")

(def events-template
  "events.mustache")

(def output-file
  "output.html")

;;hacky code that does stuff
(defn- get-meetup-events [api-key meetup]
  (-> (format meetup-url api-key meetup number-of-weeks) 
      slurp
      (json/read-str :key-fn keyword)
      :results))

(defn- get-all-meetups [api-key city]
  (->> (map #(get-meetup-events api-key %) (city meetups))
       flatten))

(defn- format-time [time]
   (unparse
    (with-zone (formatters :rfc822) (default-time-zone))
    (from-long time)))

(defn- format-event [event]
  {:name (:name event)
   :group_name (:name (:group event))
   :time (format-time (:time event))
   :url (:event_url event)})

(defn events-to-html [events]
  (render-resource events-template {:events events}))

(defn get-events [api-key city]
  (->>(get-all-meetups api-key city)
      (map format-event)))

(defn -main 
  ([api-key] 
     (-main api-key "brisbane" output-file))
  ([api-key city]
     (-main api-key city output-file))
  ([api-key city output-file-name]
     (->> (get-events api-key (keyword city))
          events-to-html
          (spit output-file-name))))

