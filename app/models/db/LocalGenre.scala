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
case class LocalGenre(genre_id: String, genre_name: String,  shelf_id: Long, sort_num: Int)

/**
 *  テーブルスキーマの定義
 */
class LocalGenreTable(tag: Tag) extends Table[LocalGenre](tag, "LocalGenre") {
  def genre_id = column[String]("genre_id", O.PrimaryKey)
  def genre_name = column[String]("genre_name")
  def shelf_id = column[Long]("shelf_id")
  def sort_num = column[Int]("sort_num")
  
  def * = (genre_id, genre_name, shelf_id, sort_num) <> (LocalGenre.tupled, LocalGenre.unapply)
  
  // foreign key
  //val genreseries = TableQuery[LocalGenreSeriesTable]
  def fk_shelf_genre = foreignKey("fk_shelf_genre", shelf_id, BookshelfDAO.bookshelf)(_.shelf_id)
}

/**
 * DAOの定義
 */
object LocalGenreDAO {
  lazy val localgenre = TableQuery[LocalGenreTable]
  
  /**
   * 本棚に登録されているジャンル一覧
   * @param shelf_id
   */
  def searchByShelf(shelf_id: Long)(implicit s: Session): Seq[(String, String)] = {
      localgenre.filter(_.shelf_id === shelf_id ).sortBy(_.sort_num.asc).list.map { lg =>
        (lg.genre_id.toString, lg.genre_name)
      }.toSeq
  }


  /**
   * ジャンル名検索
   * @param word
   */
  def search(name: String, shelf_id:Long)(implicit s: Session): List[LocalGenre] = {
    localgenre.filter(row => (row.shelf_id === shelf_id) && (row.genre_name like "%"+name+"%")).list
  }
  
  
  def exists(genre_id:String)(implicit s: Session): Boolean = {
    !localgenre.filter(_.genre_id === genre_id).list.isEmpty
  }


  /**
   * 全件検索(idの昇順)
   */
  def searchAll()(implicit s: Session): List[LocalGenre] = {
    localgenre.sortBy(_.genre_id.asc).list
  }

  /**
   * 全件検索(idの降順)
   */
  def searchAllDesc(implicit s: Session): List[LocalGenre] = {
    localgenre.sortBy(_.genre_id.desc).list
  }

  /**
   * 登録
   * @param genre_name, shelf_id
   */
  def register(genre_name : String, shelf_id : Long)(implicit s: Session): LocalGenre = {
    // genre_id is digest of Timestamp
    val genre_id = TextConv.getDigest("%tF %<tT.%<tL" format new java.util.Date, "MD5")
    val sort_num = localgenre.filter(_.shelf_id === shelf_id).sortBy(_.sort_num.desc).first.sort_num + 1
    val new_genre = new LocalGenre(genre_id, genre_name, shelf_id, sort_num)
    localgenre.insert(new_genre)
    
    new_genre
  }
  
  /**
   * 登録
   * @param place
   */
  def registerFirst(shelf_id : Long)(implicit s: Session): LocalGenre = {
    val genre_id = OBSConstants.DefaultGPID
    val genre_name = OBSConstants.DefaultGPName
    val sort_num = OBSConstants.DefaultGPNum
    val new_genre = new LocalGenre(genre_id, genre_name, shelf_id, sort_num)
    localgenre.insert(new_genre)
    
    // サブセット提供
    val genreSet = List("genre.entertainment","genre.live","genre.comic","genre.novel","genre.essay","genre.license","genre.language",
        "genre.computer","genre.business","genre.law","genre.social","genre.philosophy","genre.history","genre.medical",
        "genre.art","genre.kids","genre.edu","genre.nature")
    for (genre <- genreSet) {
      import play.api.i18n.Messages
      register(Messages(genre), shelf_id)
    }        
    
    new_genre
  }

  /**
   * 更新
   * @param genre
   */
  def update(genre: LocalGenre)(implicit s: Session) {
    localgenre.filter(_.genre_id === genre.genre_id).update(genre)
  }
  
  /**
   * ジャンル名更新
   * @param genre_id, genre_name, shelf_id, 
   */
  def updateName(genre_id: String, genre_name: String, shelf_id: Long)(implicit s: Session) = {
    localgenre.filter(row => row.genre_id === genre_id && row.shelf_id === shelf_id).map(_.genre_name).update(genre_name)
  }

  /**
   * 削除
   * @param genre_id, shelf_id
   */
  def remove(genre_id: String, shelf_id: Long)(implicit s: Session) {
    LocalBookDAO.updateAllGenre(shelf_id, genre_id, OBSConstants.DefaultGPID)
    localgenre.filter(row => row.genre_id === genre_id && row.shelf_id === shelf_id).delete
  }
}
