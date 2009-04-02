(ns memo
	(:use memo.utils clojure.contrib.sql))	
	
(defn base-model
	[k]
	{:name k, :table (pluralize k)})
	
(let [models (ref {})]
	(defn model?
		[k]
		(@models k))
	(defn get-model
		[k]
		(or
			(model? k)
			(and
				(dosync	(commute models #(assoc % k (ref (base-model k)))))
				(model? k)))))
				
;; MODEL HELPERS

(defn property [v]
	(let [fields-slot (if (some #{:text} (rest v)) :lazy-fields :fields)]
		{:schema [v], fields-slot [(first v)]}))

(defn belongs-to
	([k] (belongs-to k {}))
	([k options-map]
		(let [key (options-map :key (to-key k))
					model (options-map :model k)]
			(list
				{:belongs-to {k {:model model, :key key}}}
				key))))
  	
(defn has-many
	([k] (has-many k {}))
	([k options-map]
		#(let [key (options-map :key (to-key (% :name)))
					 model (options-map :model (singularize k))]
			{:has-many {k {:model model, :key key}}})))

;; MODEL OPTIONS INSTALLERS

(defn- match?
  [a b]
  (cond
    (coll? a) (some #(= b %) a)
    (keyword? a) (= b a)
    (= (class a) java.util.regex.Pattern) (re-find a (to-str b))))

(defn make-keyword-installer [option]
	(condp match? option
		:id [option :int "IDENTITY" "PRIMARY KEY"]
    #"(text|article|essey|post|content|body)" [option :text]
    #"_at$" [option :datetime]
    #"_id$" [option :int]
    #"^is_" [option :boolean]
    #"(number|quantity|int$)" [option :int]
		; default
		[option :varchar]))

(defn make-installer [option]
	(if (fn? option) option
		(recur (cond
			(keyword? option) (make-keyword-installer option)
			(vector? option) (property option)
			(list? option) (let [installers (map make-installer option)] #(chain % installers))
			(map? option) #(commit % option)
			true (throw (IllegalArgumentException.
		    (str "I don't understrand option: " option)))))))

;; CREATE MODEL

(defn create-model [k & options]
	(let [model-ref (get-model k)
				option-installers (map make-installer options)
				upgraded-model (chain @model-ref option-installers)]
		(dosync	(ref-set model-ref upgraded-model))
		model-ref))
		
;; MIGRATE

