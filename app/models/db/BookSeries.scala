package models.db

/**
 * @author Emi Yoshihara
 */

import play.api.db.slick.Config.driver.simple._

/**
 * DTOの定義
 */
case class BookSeries(series_id: Option[Long], series_name: String)

/**
 *  テーブルスキーマの定義
 */
class BookSeriesTable(tag: Tag) extends Table[BookSeries](tag, "BookSeries") {
//object BookSeriesTable extends Table[BookSeries]("BookSeries") {
  
  def series_id = column[Long]("series_id", O.PrimaryKey, O.AutoInc)
  def series_name = column[String]("series_name")
  
  def * = (series_id.? , series_name) <> (BookSeries.tupled, BookSeries.unapply)
  
  //def incInsert = (series_id.?, series_name) <> (BookSeries.tupled, BookSeries.unapply) returning series_id

}

/**
 * DAOの定義
 */
object BookSeriesDAO {
  lazy val bookseries = TableQuery[BookSeriesTable]

  /**
   * ID検索
   * @param id
   */
  def searchByID(id: Long)(implicit s: Session): BookSeries = {
    val s_id = id // TODO シリーズ
    bookseries.filter(_.series_id === s_id).first
  }
  
  /**
   * シリーズ名検索
   * @param name
   */
  def searchByName(name: String)(implicit s: Session): Option[BookSeries] = {
    bookseries.filter(_.series_name === name).firstOption
  }

  /**
   * 登録
   * @param series_name
   * @return series_id
   */
  def register(series_name: String)(implicit s: Session): Long = {
    // TODO AutoIncにしたい
    //val l = bookSeriesQuery.map(s => (s.series_name))
  //      .insert((series_name))
      
    val new_id = (bookseries returning bookseries.map(_.series_id)) += new BookSeries(None, series_name)

    new_id
  }
}
