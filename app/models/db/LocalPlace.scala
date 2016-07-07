package models.db

/**
 * @author Emi Yoshihara
 */


import play.api.db.slick.Config.driver.simple._

import common.utils.TextConv
import common.OBSConstants

/**
 * DTOの定義
 */
case class LocalPlace(place_id: String, place_name: String,  shelf_id: Long, sort_num: Int)

/**
 *  テーブルスキーマの定義
 */
class LocalPlaceTable(tag: Tag) extends Table[LocalPlace](tag, "LocalPlace") {
  def place_id = column[String]("place_id", O.PrimaryKey)
  def place_name = column[String]("place_name")
  def shelf_id = column[Long]("shelf_id")
  def sort_num = column[Int]("sort_num")
  
  def * = (place_id, place_name, shelf_id, sort_num) <> (LocalPlace.tupled, LocalPlace.unapply)
  
  // foreign key
  def fk_shelf_place = foreignKey("fk_shelf_place", shelf_id, BookshelfDAO.bookshelf)(_.shelf_id)
}

/**
 * DAOの定義
 */
object LocalPlaceDAO {
  lazy val localplace = TableQuery[LocalPlaceTable]
  
  /**
   * 本棚に登録されている場所一覧
   * @param shelf_id
   */
  def searchByShelf(shelf_id: Long)(implicit s: Session): Seq[(String, String)] = {
      localplace.filter(_.shelf_id === shelf_id ).sortBy(_.sort_num.asc).list.map { lg =>
        (lg.place_id.toString, lg.place_name)
      }.toSeq
  }


  /**
   * 場所名検索
   * @param word
   */
  def search(name: String, shelf_id: Long)(implicit s: Session): List[LocalPlace] = {
    localplace.filter(row => (row.shelf_id === shelf_id) && (row.place_name like "%"+name+"%")).list
  }
  
  
  /**
   * 該当の場所IDが存在するかどうかチェック
   */
  def exists(place_id:String)(implicit s: Session): Boolean = {
    !localplace.filter(_.place_id === place_id).list.isEmpty
  }


  /**
   * 全件検索(idの昇順)
   */
  def searchAll()(implicit s: Session): List[LocalPlace] = {
    localplace.sortBy(_.place_id.asc).list
  }

  /**
   * 全件検索(idの降順)
   */
  def searchAllDesc(implicit s: Session): List[LocalPlace] = {
    localplace.sortBy(_.place_id.desc).list
  }

  /**
   * 登録
   * @param place
   */
  def register(place_name : String, shelf_id : Long)(implicit s: Session): LocalPlace = {
        
    // place_id is digest of Timestamp
    val place_id = TextConv.getDigest("%tF %<tT.%<tL" format new java.util.Date, "MD5")
    
    val sort_num = localplace.filter(_.shelf_id === shelf_id).sortBy(_.sort_num.desc).first.sort_num + 1
    
    val new_place = new LocalPlace(place_id.toString, place_name, shelf_id, sort_num)
    localplace.insert(new_place)
    
    new_place
  }
  
  /**
   * 登録
   * @param place_name, shelf_id
   */
  def registerFirst(shelf_id : Long)(implicit s: Session): LocalPlace = {
    val place_id = OBSConstants.DefaultGPID
    val place_name = OBSConstants.DefaultGPName
    val sort_num = OBSConstants.DefaultGPNum
    val new_place = new LocalPlace(place_id, place_name, shelf_id, sort_num)
    localplace.insert(new_place)
    
    // サブセット提供
    val placeSet = List("place.first", "place.pdf", "place.kindle")
    for (place <- placeSet) {
      import play.api.i18n.Messages
      register(Messages(place), shelf_id)
    }
    
    new_place
  }

  /**
   * 更新
   * @param place
   */
  def update(place: LocalPlace)(implicit s: Session) {
    localplace.filter(_.place_id === place.place_id).update(place)
  }
  
  /**
   * 場所名更新
   * @param place_id, place_name, shelf_id, 
   */
  def updateName(place_id: String, place_name: String, shelf_id: Long)(implicit s: Session) = {
    localplace.filter(row => row.place_id === place_id && row.shelf_id === shelf_id).map(_.place_name).update(place_name)
  }

  /**
   * 削除
   * @param place_id, shelf_id
   */
  def remove(place_id: String, shelf_id: Long)(implicit s: Session) {
    LocalBookDAO.updateAllPlace(shelf_id, place_id, OBSConstants.DefaultGPID)
    localplace.filter(row => row.place_id === place_id && row.shelf_id === shelf_id).delete
  }
}
