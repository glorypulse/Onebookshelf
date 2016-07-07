package models.db

/**
 * @author Emi Yoshihara
 */


import play.api.db.slick.Config.driver.simple._

/**
 * DTOの定義
 */
case class Bookshelf(shelf_id: Option[Long], shelf_name: String,  privacy: Short)

/**
 *  テーブルスキーマの定義
 */
class BookshelfTable(tag: Tag) extends Table[Bookshelf](tag, "Bookshelf") {
  def shelf_id = column[Long]("shelf_id", O.PrimaryKey, O.AutoInc)
  def shelf_name = column[String]("shelf_name")
  def privacy = column[Short]("privacy")
  
  def * = (shelf_id.?, shelf_name, privacy) <> (Bookshelf.tupled, Bookshelf.unapply)
  
}

/**
 * DAOの定義
 */
object BookshelfDAO {
  lazy val bookshelf = TableQuery[BookshelfTable]
  
  /**
   * ID検索
   * @param id
   */
  def searchByID(id: Long)(implicit s: Session): Bookshelf = {
      bookshelf.filter(_.shelf_id === id ).first
  }
  

  /**
   * 登録 - Auto Increment
   * @param shelf
   * @return shelf_id
   * shelf_idはNoneである必要がある
   */
  def register(shelf : Bookshelf)(implicit s: Session): Long = {
     
    val new_id = (bookshelf returning bookshelf.map(_.shelf_id)) += shelf

    new_id
  }

  /**
   * 更新
   * @param shelf
   */
  def update(shelf: Bookshelf)(implicit s: Session) {
    bookshelf.filter(_.shelf_id === shelf.shelf_id).update(shelf)
  }
  
}
