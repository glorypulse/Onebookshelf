package models.db

/**
 * @author Emi Yoshihara
 */


import play.api.db.slick.Config.driver.simple._

/**
 * DTOの定義
 */
case class LocalTag(tag_id: String, tag_name: String,  shelf_id: Long)

/**
 *  テーブルスキーマの定義
 */
class LocalTagTable(tag: Tag) extends Table[LocalTag](tag, "LocalTag") {
  def tag_id = column[String]("tag_id", O.PrimaryKey)
  def tag_name = column[String]("tag_name")
  def shelf_id = column[Long]("shelf_id")
  
  def * = (tag_id, tag_name, shelf_id) <> (LocalTag.tupled, LocalTag.unapply)
  
  // foreign key
  //val tagseries = TableQuery[LocalTagSeriesTable]
  def fk_shelf_tag = foreignKey("fk_shelf_tag", shelf_id, BookshelfDAO.bookshelf)(_.shelf_id)
}

/**
 * DAOの定義
 */
object LocalTagDAO {
  lazy val localtag = TableQuery[LocalTagTable]
  
  /**
   * ID検索
   * @param id
   */
  def searchByID(id: String)(implicit s: Session): LocalTag = {
      localtag.filter(_.tag_id === id ).first
  }


  /**
   * ジャンル名検索
   * @param word
   */
  def search(word: String)(implicit s: Session): List[LocalTag] = {
    localtag.filter(row => (row.tag_name like "%"+word+"%")).list
  }


  /**
   * 全件検索(idの昇順)
   */
  def searchAll()(implicit s: Session): List[LocalTag] = {
    localtag.sortBy(_.tag_id.asc).list
  }

  /**
   * 全件検索(idの降順)
   */
  def searchAllDesc(implicit s: Session): List[LocalTag] = {
    localtag.sortBy(_.tag_id.desc).list
  }

  /**
   * 登録
   * @param tag
   */
  def register(tag : LocalTag)(implicit s: Session): LocalTag = {
    // tag_id inc 
    val tagcount = localtag.filter { _.shelf_id === tag.shelf_id } .length .run
    println("Debug-TagCount:", tagcount)
    val tag_id = tagcount + 1
    
    val new_tag = new LocalTag(tag_id.toString, tag.tag_name, tag.shelf_id)
    localtag.insert(new_tag)
    
    new_tag
  }

  /**
   * 更新
   * @param tag
   */
  def update(tag: LocalTag)(implicit s: Session) {
    localtag.filter(_.tag_id === tag.tag_id).update(tag)
  }

  /**
   * 削除
   * @param tag
   */
  def remove(tag: LocalTag)(implicit s: Session) {
    localtag.filter(_.tag_id === tag.tag_id).delete
  }
}
