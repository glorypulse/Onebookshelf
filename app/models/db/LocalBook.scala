package models.db

/**
 * @author Emi Yoshihara
 */


import play.api.db.slick.Config.driver.simple._
import java.sql.Timestamp

/**
 * DTOの定義
 */
case class LocalBook(shelf_id : Long, book_id : String, genre_id : String, place_id : String, abst : String, reg_date : Timestamp, reg_user : Long)

/**
 *  テーブルスキーマの定義
 */
class LocalBookTable(tag: Tag) extends Table[LocalBook](tag, "LocalBook") {
  def shelf_id = column[Long]("shelf_id", O.PrimaryKey)
  def book_id = column[String]("book_id", O.PrimaryKey)
  def genre_id = column[String]("genre_id")
  def place_id = column[String]("place_id")
  def abst = column[String]("abst")
  def reg_date = column[Timestamp]("reg_date")
  def reg_user = column[Long]("reg_user")
  
  def * = (shelf_id, book_id, genre_id, place_id, abst, reg_date, reg_user) <> (LocalBook.tupled, LocalBook.unapply)
  
  // foreign key
  def fk_shelf_localbook = foreignKey("fk_shelf_localbook", shelf_id, BookshelfDAO.bookshelf)(_.shelf_id)
  def fk_localbook_localbook = foreignKey("fk_book_localbook", book_id, BookDAO.book)(_.book_id)
  def fk_genre_locallocalbook = foreignKey("fk_genre_localbook", genre_id, LocalGenreDAO.localgenre)(_.genre_id)
  def fk_place_locallocalbook = foreignKey("fk_place_localbook", place_id, LocalPlaceDAO.localplace)(_.place_id)
}

/**
 * DAOの定義
 */
object LocalBookDAO {
  lazy val localbook = TableQuery[LocalBookTable]
  
  /**
   * IDで存在確認
   * @param shelf_id, book_id
   */
  def existsByID(shelf_id: Long, book_id: String)(implicit s: Session): Boolean = {
    !localbook.filter(row => (row.shelf_id === shelf_id) && (row.book_id === book_id)).list.isEmpty
  }
  
  /**
   * ID検索
   * @param shelf_id, book_id
   */
  def searchByID(shelf_id : Long, book_id: String)(implicit s: Session): LocalBook = {
    println(localbook.filter(row => (row.shelf_id === shelf_id) && (row.book_id === book_id)))
    localbook.filter(row => (row.shelf_id === shelf_id) && (row.book_id === book_id)).first
  }

  /**
   * ジャンル検索
   * @param genre_id
   */
  def searchByGenre(genre_id : String)(implicit s: Session): List[LocalBook] = {
      localbook.filter(_.genre_id === genre_id).list
  }
  
  /**
   * 場所検索
   * @param genre_id
   */
  def searchByPlace(place_id : String)(implicit s: Session): List[LocalBook] = {
      localbook.filter(_.place_id === place_id).list
  }

  
  /**
   *概要検索  
   * @param word
   */
  def searchByAbst(word: String)(implicit s: Session): List[LocalBook] = {
    localbook.filter(row => (row.abst like "%"+word+"%")).list
  }

  /**
   * 全件検索(idの昇順)
   */
  def searchAll()(implicit s: Session): List[LocalBook] = {
    localbook.sortBy(_.book_id.asc).list
  }

  /**
   * 登録
   * @param localbook_data
   */
  def register(lb_data : LocalBook)(implicit s: Session) {
    // current_date
    val current_date = new Timestamp(System.currentTimeMillis())
    
    localbook.insert(new LocalBook(lb_data.shelf_id, lb_data.book_id, lb_data.genre_id, lb_data.place_id, lb_data.abst, current_date, lb_data.reg_user))
  }

  /**
   * 更新
   * @param localbook_data
   */
  def update(localbook_data: LocalBook)(implicit s: Session) {
    localbook.filter(row => (row.shelf_id === localbook_data.shelf_id) && (row.book_id === localbook_data.book_id)).update(localbook_data)
  }
  
  def updateGenre(shelf_id: Long, book_id: String, genre_id: String)(implicit s: Session) {
    localbook.filter(row => row.shelf_id === shelf_id && row.book_id === book_id).map(_.genre_id).update(genre_id)
  }
  
   def updatePlace(shelf_id: Long, book_id: String, place_id: String)(implicit s: Session) {
    localbook.filter(row => row.shelf_id === shelf_id && row.book_id === book_id).map(_.place_id).update(place_id)
  }

  /**
   * 更新　- 場所一斉置換　（削除に伴う）
   * @param shelf_id, place_id, new_place_id
   */
  def updateAllPlace(shelf_id: Long, place_id: String, new_place_id: String)(implicit s: Session) {
    localbook.filter ( row => row.shelf_id === shelf_id && row.place_id === place_id ).map ( _.place_id ).update( new_place_id )
  }
  
  /**
   * 更新　- ジャンル一斉置換　（削除に伴う）
   * @param shelf_id, genre_id, new_genre_id
   */
  def updateAllGenre(shelf_id: Long, genre_id: String, new_genre_id: String)(implicit s: Session) {
    localbook.filter ( row => row.shelf_id === shelf_id && row.genre_id === genre_id ).map ( _.genre_id ).update( new_genre_id )
  }  
  /**
   * 削除
   * @param localbook_data
   */
  def remove(localbook_data : LocalBook)(implicit s: Session) {
    localbook.filter(row => (row.shelf_id === localbook_data.shelf_id) && (row.book_id === localbook_data.book_id)).delete
  }
}
