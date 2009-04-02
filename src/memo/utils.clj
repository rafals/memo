(ns memo.utils
	(:use clojure.contrib.str-utils))

(defn chain
  "(f3(f2(f1 x))) ... etc. for n fs in coll"
  [x coll]
  (if (empty? coll)
    x
    (recur ((first coll) x) (rest coll))))

(defn add-to-meta
  [object & options]
  (with-meta object (apply assoc (meta object) options)))

(defn tag-of?
	[r]
	((meta r) :tag))

(defn to-str
  [object]
  (cond
   (keyword? object) (name object)
   (symbol? object) (name object)
   (string? object) object
   true (str object)))

(defn to-sym
  [object]
  (symbol (to-str object)))

(defn to-k
  [object]
  (keyword (to-str object))) 
 
(defn- save-type [object f]
  (cond
   (keyword? object) (to-k (f (to-str object)))
   (string? object) (to-str (f object))
   (symbol? object) (to-sym (f (to-str object)))
   true (f (str object))))

(defmacro defs
  "S¸uýy do traktowania keyword—w i symboli jak stringi w ciele funkcji.
Tworzy funkcj«, kt—ra przed wykonaniem zamienia argument na string i zwracanˆ wartoæ sprowadza do poczˆtkowego typu."
  [name args-v & body]
  `(defn ~name
     ~args-v
     (save-type
      ~@args-v
      (fn [~@args-v] ~@body))))

(defs to-key [s] (str s "_id"))

(defs downcase [s] (.toLowerCase s))

(defs upcase [s] (.toUpperCase s)) 

(defn re-gsub-first-matching [pairs s]
	(let [[regex replacement] (first pairs)]
		(if (re-find regex s)
			(re-gsub regex replacement s)
			(recur (rest pairs) s))))

(let [uncountable ["equipment" "information" "rice" "money" "species" "series" "fish" "sheep"]
			irregular-singulars {"person" "people", "man" "men", "child" "children",
								 					 "sex" "sexes", "move" "moves", "cow" "kine"}
			irregular-plurals (clojure.set/map-invert irregular-singulars)
			plural [[#"(quiz)$" "$1zes"]
							[#"^(ox)$" "$1en"]
							[#"([m|l])ouse$" "$1ice"]
							[#"(matr|vert|ind)(?:ix|ex)$" "$1ices"]
							[#"(x|ch|ss|sh)$" "$1es"]
							[#"([^aeiouy]|qu)y$" "$1ies"]
							[#"(hive)$" "$1s"]
							[#"(?:([^f])fe|([lr])f)$" "$1$2ves"]
							[#"sis$" "ses"]
							[#"([ti])um$" "$1a"]
							[#"(buffal|tomat)o$" "$1oes"]
							[#"(bu)s$" "$1ses"]
							[#"(alias|status)$" "$1es"]
							[#"(octop|vir)us$" "$1i"]
							[#"(ax|test)is$" "$1es"]
							[#"s$" "s"]
							[#"$" "s"]]
			singular [[#"(database)s$" "$1"]
								[#"(quiz)zes$" "$1"]
								[#"(matr)ices$" "$1ix"]
								[#"(vert|ind)ices$" "$1ex"]
								[#"^(ox)en" "$1"]
								[#"(alias|status)es$" "$1"]
								[#"(octop|vir)i$" "$1us"]
								[#"(cris|ax|test)es$" "$1is"]
								[#"(shoe)s$" "$1"]
								[#"(bus)es$" "$1"]
								[#"([m|l])ice$" "$1ouse"]
								[#"(x|ch|ss|sh)es$" "$1ovie"]
								[#"(s)eries$" "$1eries"]
								[#"([^aeiouy]|qu)ies$" "$1y"]
								[#"([lr])ves$" "$1f"]
								[#"(tive)s$" "$1"]
								[#"(hive)s$" "$1"]
								[#"([^f])ves$" "$1fe"]
								[#"(^analy)ses$" "$1sis"]
								[#"((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$" "$1$2sis"]
								[#"([ti])a$" "$1um"]
								[#"(n)ews$" "$1ews"]
								[#"s$" ""]]]
	(defs pluralize
		[s]
		(let [s (downcase s)]
			(or
				(if (some (partial = s) uncountable) s)
				(irregular-singulars s)
				(re-gsub-first-matching plural s))))
	(defs singularize
		[s]
		(let [s (downcase s)]
			(or
				(if (some (partial = s) uncountable) s)
				(irregular-plurals s)
				(re-gsub-first-matching singular s)))))

(defn now [] (java.sql.Timestamp. (.getTime (java.util.Date.))))

(defn commit
  " ;; adding when no conflicts
    (commit {:a 1} {:b 2})
    > {:a 1 :b 2}
    ;; nesting with hash-maps
    (commit {:a {:b [1 2]}} {:a {:b [3 4]}})
    > {:a {:b [1 2 3 4]}}
    ;; subs on conflicts    v                v
    (commit {:a {:b {:c 1 :d 2}}} {:a {:b {:d 500 :e 4}}})
    > {:a {:b {:c 1 :d 500 :e 4}}}"
  [target source]
  (cond
   (nil? target)
   source,

   (and (coll? source) (empty? source))
   target,

   (and (map? target) (map? source))
   (let [node (first source)
	 			 key (first (first source))
	 			 val (second (first source))]
     (recur (assoc target key (commit (target key) val)) (merge {} (rest source)))),
   
   (and (vector? target) (vector? source))
   (apply vector (concat target source)),
   
   (and (list? target) (list? source))
   (apply list (concat target source)),
   
   true ; substitutions
   source))