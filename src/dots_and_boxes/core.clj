(ns dots-and-boxes.core
  (:require [org.httpkit.server :as server]
            [compojure.core :as cc]
            [compojure.route :as route]
            [ring.middleware.defaults :as rmd]
            [ring.middleware.json :as rmjs]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [dots-and-boxes.game :as game])
  (:gen-class))

(defn getparameter [req pname] (get (:params req) pname))


(defn simple-body-page [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Hello World"})


(defn next-move
  [req]
  (println (get-in req [:params :squere]))
  {
   :status 200
   :headers {"Content-Type" "application/json"}
   :body (str (json/write-str (game/next-move (get-in req [:params :squere]))))
   })

(defn close-move?
  [req]
  (println (get-in req [:params :squere]))
  (println (get-in req [:params :moves]))
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (str (json/write-str (game/close-box-position (get-in req [:params :squere])
                                                       (if (= (get-in req [:params :moves 0 0]) 0) true false)
                                                       (get-in req [:params :moves 0 1]))))})

(defn all-free-moves
  [req]
  (println (get-in req [:params :squere]))
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (str (json/write-str (game/all-free-moves (get-in req [:params :squere]))))})

(defn get-move-boxes
  [req]
  (println (get-in req [:params :squere]))
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (str (json/write-str (game/get-move-boxes (get-in req [:params :squere])
                                                       (if (= (get-in req [:params :moves 0 0]) 0) true false)
                                                       (get-in req [:params :moves 0 1]))))})

(cc/defroutes app-routes
  (cc/GET "/hello-world" [] simple-body-page)
  (cc/POST "/dots-and-boxes/next-move" [] next-move)
  (cc/POST "/dots-and-boxes/is-close-move" [] close-move?)
  (cc/POST "/dots-and-boxes/all-free-moves" [] all-free-moves)
  (cc/POST "/dots-and-boxes/get-move-boxes" [] get-move-boxes)
  (route/not-found "error"))

(defn -main
  "This is our main entry point"
  [& args]
  (let [port 3000]
    (server/run-server (rmjs/wrap-json-params (rmjs/wrap-json-response (rmd/wrap-defaults #'app-routes (assoc-in rmd/site-defaults [:security :anti-forgery] false)))) {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))