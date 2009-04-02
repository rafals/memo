(comment
	;; konstrukcja modelu
	;; memo nie nadaje si« do importowania istniejˆcych baz danych!
	
	;; kaýda tabela ma id
	;; walidacji nie ma
	
	;; ? - szukanie
	;; ! - zapisywanie
	(>> :user {:name "Rav"})
	(>> (user :name "Rav"))
	(<< (user :name "Rav"))
	(>> :user {:name "Rav"})
	(user? {:name "Rav"})
	(user? {:name "Rav"} :id)
	(user? {:name "Rav"} :id :asc)
	(user? {:name "Rav"} :id <)
	(create :user {:name "Rav"})
	(save :user {:name "Rav"})
	(save (create :user {:name "Rav"}))
	(user 4)
	(user? {:id 4})
	
	(new-user {:name "Rav", :email "ravsobota@gmail.com"})
	(user/find)
	(find-user {:name "Rav"})
	
	(-> (find-user 3) (change :name "Ravkie") save)
	
	(? :user {:name "Rav"})
	(? :user {:name #"R"})
	(user {:name "Rav"}) ;; new
	(user? {:name "Rav"}) ;; find first
	(def user? (partial find :user))
	(! (user {:name "Rav"})) ;; create (save)
	
	(defmodel user
		:id :name :text [:kukua :int]
		(belongs-to :group {:model :group2, :key :group_id2})
		(has-many :comments)
		(has-many :comments {:model :comment, :key :owner_id})
		(has-and-belongs-to-many :friends {:model :user, :table :friends_users}))
		
	{	:name :user
		:table :users
		:schema [[][][][]]
		:fields [:id :name]
		:lazy-fields [:description]
		:belongs-to {:team {}, :category {:model :subculture, :key :category_identifier}} ;; wed¸ug slota
		:has-many {:friends {:model :user, :key :user_id}} ;; wed¸ug slota
		:has-and-belongs-to-many {:friends {:model :user, :table :friends-users, :self-key :owner_id, :target-key :iha_id}
		
		;; relacje powinno da si« skompilowa do funkcji pobierajˆcych id rekordu i zwracajˆcych sql zapytania
		
		:compiled-lazy-fields {:description f, :team f, :subculture f, :friends f}
		
		:before {:save {"usuni«cie z dýungli 34652345423" #() }}
		:before {:save [[f :tag1 :tag2 :tag3 ...]
										[f2 :tag1]
										[f3]
										[f4 :tagi :tagu :tage]]}
		; ale po co usuwa?
		:before {:save [f1, f2, f3], :update [], :create [], :find []}
		:after { }
		}
	
	(defmodel user
		:id
		:login
		:password ;; tutaj tworzy si« password-salt + password-hash + filtry
		[:name :int "NOT NULL"]
	)
	;; tworzy funkcje:
	User
	User!
	User?
	Users?
	
	;; dodaj jakieæ itemki
	(->
		(User? :name "Rav")
		(change :name "Rav2")
		(add (Book :title "Pilot i ja")) ;; to books
		(add-to :items (Book :title "jea")) ;; to items
		(add-to :items (Books? :title #"^P"))
		save)
		
	;; usuÄ ksiˆýk«
	(->
		(User? :name "Rav")
		(unplug (Book? :title "Pilot i ja"))
		(umplug-from :items {:title #"^P"})
		(umplug-from :items :title #"^P")
		save)
	
	(add (user :friends) friend)
	(->
		(find :user {:name "Rav"})
		(update {:name "Ravalarz"})
		(add-to :friends friend)
		(add-new :friend {:name "Niga"})
		save)
	(-> (find :user {:name "rav"}) (update {:name "Yo"}) save)
	;; ONE TO MANY
	(-> (User {:name "Rav"})
			(puts-to :Books (Book? {:title "Pilot i ja"}))
			(puts-new :Book {:title "hejka"})
			save)
	;; MANY TO MANY
	(-> (User {:name "Rav"})
			(connect-as :friend ))
		
	(save-as :user {:name "rav" :email "ravsobota@gmail.com"})
	(add user :friends friend) ;; dodaje do hasha i do mety, w mecie przechowuje si« info o zmianach
	(commit user {:name "Rav2" :friends [{:name "Jogi"}]})
	
	;; 1. has-many
	
	(<< user :books {:title "Jajko", :author "Iha"})
	(<< user :books [{:title "Jajko", :author "Iha"} {:title "Jab¸ecznik", :author "Hejka"}])
	(update user {:name "Rav"} (add-to :books book1 book2 {:title "new book"}))
	(update user (<< :books book1))
	((find :user 1) :books)
	(update user (<< :friends user))
	
	;; a moýe macro -> gen-class ??
)