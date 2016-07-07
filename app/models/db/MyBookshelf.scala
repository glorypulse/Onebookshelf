package models.db

/**
 * @author Emi Yoshihara
 */


import play.api.db.slick.Config.driver.simple._
import play.Logger

/**
 * DTO
 */
case class MyBookshelf(user_id: Long, shelf_id:Long, auth_flag: Short, def_flag: Short)

/**
 *  テーブルスキーマ
 */
class MyBookshelfTable(tag: Tag) extends Table[MyBookshelf](tag, "MyBookshelf") {
  def user_id = column[Long]("user_id", O.PrimaryKey)
  def shelf_id = column[Long]("shelf_id", O.PrimaryKey)
  def auth_flag = column[Short]("auth_flag")
  def def_flag = column[Short]("def_flag")


  
  def * = (user_id, shelf_id, auth_flag, def_flag) <> (MyBookshelf.tupled, MyBookshelf.unapply)
  
  // foreign key
  def fk_user_myshelf = foreignKey("fk_user_myshelf", user_id, UserDAO.user)(_.user_id)
  def fk_shelf_myshelf = foreignKey("fk_shelf_myshelf", shelf_id, LocalBookDAO.localbook)(_.shelf_id)
}

/**
 * DAO
 */
object MyBookshelfDAO {
  lazy val mybookshelf = TableQuery[MyBookshelfTable]
  
  /**
   * ID検索
   * @param shelf_id, book_id
   */
  def searchByID(user_id : Long, shelf_id : Long)(implicit s: Session): Option[MyBookshelf] = {
    try {
      Some(mybookshelf.filter(row => (row.user_id === user_id) && (row.shelf_id === shelf_id)).first)
    } catch {
      case e : Throwable => Logger.error(this.toString + " error", e); None // TODO 先に確認する実装に変更したい
    }
  }


  /**
   * ユーザごと本棚一覧取得
   * @param word
   */
  def search(user_id : Long)(implicit s: Session): List[MyBookshelf] = {
    mybookshelf.filter(row => (row.user_id === user_id)).list
  }
  
  /**
   * デフォルト本棚取得
   * @param word
   */
  def searchDef(user_id : Long, def_flag: Short)(implicit s: Session): MyBookshelf = {
    mybookshelf.filter(row => (row.user_id === user_id) && (row.def_flag === def_flag)).first
  }


  /**
   * 登録
   * @param localtagmap_data
   */
  def register(myshelf : MyBookshelf)(implicit s: Session) {
    mybookshelf.insert(myshelf)
  }

  /**
   * 更新
   * @param localtagmap_data
   */
  def update(myshelf : MyBookshelf)(implicit s: Session) {
    mybookshelf.filter(row => (row.user_id === myshelf.user_id) && (row.shelf_id === myshelf.shelf_id) ).update(myshelf)
  }

  /**
   * 削除
   * @param localtagmap_data
   */
  def remove(user_id : Long, shelf_id : Long)(implicit s: Session) {
    mybookshelf.filter(row => (row.user_id === user_id) && (row.shelf_id === shelf_id)).delete
  }
}
