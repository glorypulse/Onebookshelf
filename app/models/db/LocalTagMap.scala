package models.db

/**
 * @author Emi Yoshihara
 */


import play.api.db.slick.Config.driver.simple._

/**
 * DTOの定義
 */
case class LocalTagMap(shelf_id:Long, book_id:String, tag_id: String)

/**
 *  テーブルスキーマの定義
 */
class LocalTagMapTable(tag: Tag) extends Table[LocalTagMap](tag, "LocalTagMap") {
  def shelf_id = column[Long]("shelf_id", O.PrimaryKey)
  def book_id = column[String]("book_id", O.PrimaryKey)
  def tag_id = column[String]("tag_id", O.PrimaryKey)


  
  def * = (shelf_id, book_id, tag_id) <> (LocalTagMap.tupled, LocalTagMap.unapply)
  
  // foreign key
  //def fk_localbook_tagmap = foreignKey("fk_localbook_tagmap", shelf_id, LocalBookDAO.localbook)(_.shelf_id)
  def fk_localbook_tagmap = foreignKey("fk_localbook_tagmap", (shelf_id, book_id), LocalBookDAO.localbook)(lb => (lb.shelf_id, lb.book_id))
  def fk_tag_tagmap = foreignKey("fk_tag_tagmap", tag_id, LocalTagDAO.localtag)(_.tag_id)
}

/**
 * DAOの定義
 */
object LocalTagMapDAO {
  lazy val localtagmap = TableQuery[LocalTagMapTable]
  
  /**
   * ID検索
   * @param shelf_id, book_id, tag_id
   */
  def searchByID(shelf_id : Long, book_id: String, tag_id: String)(implicit s: Session): LocalTagMap = {
      localtagmap.filter(row => (row.shelf_id === shelf_id) && (row.book_id === book_id) && (row.tag_id === tag_id)).first
  }


  /**
   * タグ一覧取得
   * @param word
   */
  def search(shelf_id : Long, book_id: String)(implicit s: Session): List[LocalTagMap] = {
    localtagmap.filter(row => (row.shelf_id === shelf_id) && (row.book_id === book_id)).list
  }


  /**
   * 全件検索(idの昇順)
   */
  def searchAll()(implicit s: Session): List[LocalTagMap] = {
    localtagmap.sortBy(_.tag_id.asc).list
  }

  /**
   * 全件検索(idの降順)
   */
  def searchAllDesc(implicit s: Session): List[LocalTagMap] = {
    localtagmap.sortBy(_.tag_id.desc).list
  }

  /**
   * 登録
   * @param localtagmap_data
   */
  def register(localtagmap_data : LocalTagMap)(implicit s: Session) {
    localtagmap.insert(localtagmap_data)
  }

  /**
   * 更新
   * @param localtagmap_data
   */
  def update(localtagmap_data : LocalTagMap)(implicit s: Session) {
    localtagmap.filter(row => (row.shelf_id === localtagmap_data.shelf_id) && (row.book_id === localtagmap_data.book_id) && (row.tag_id === localtagmap_data.tag_id)).update(localtagmap_data)
  }

  /**
   * 削除
   * @param localtagmap_data
   */
  def remove(shelf_id : Long, book_id: String, tag_id: String)(implicit s: Session) {
    localtagmap.filter(row => (row.shelf_id === shelf_id) && (row.book_id === book_id) && (row.tag_id === tag_id)).delete
  }
}
