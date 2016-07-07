package models.db

/**
 * @author Emi Yoshihara
 */


import play.api.db.slick.Config.driver.simple._
import java.sql.Date

/**
 * DTOの定義
 */
case class LocalBookV(shelf_id : Long, book_id : String, genre_id : String, place_id : String, abst : String, reg_date: Date, reg_user: Long,
    ean: String, isbn: String, title: String, author: String, year: Int, publish: String, series_id: Option[Long], series_no: Option[Int],
    amazon_url: String, asin: String, product: String, img_url: String,
    genre_name: String, place_name: String)


/**
 *  テーブルスキーマの定義
 */
class LocalBookView(tag: Tag) extends Table[LocalBookV](tag, "LocalBookV") {
  def shelf_id = column[Long]("shelf_id", O.PrimaryKey)
  def book_id = column[String]("book_id", O.PrimaryKey)
  def genre_id = column[String]("genre_id")
  def place_id = column[String]("place_id")
  def abst = column[String]("abst")
  def reg_date = column[Date]("reg_date")
  def reg_user = column[Long]("reg_user")
  
  def ean = column[String]("ean")
  def isbn = column[String]("isbn")
  def title = column[String]("title")
  def author = column[String]("author")
  def year = column[Int]("year")
  def publish = column[String]("publish")
  def series_id = column[Option[Long]]("series_id")
  def series_no = column[Option[Int]]("series_no")
  def amazon_url = column[String]("amazon_url")
  def asin = column[String]("asin")
  def product = column[String]("product")
  def img_url = column[String]("img_url")
  
  def genre_name = column[String]("genre_name")
  def place_name = column[String]("place_name")

  
  def * = (shelf_id, book_id, genre_id, place_id, abst, reg_date, reg_user,
          ean, isbn, title, author, year, publish, series_id, series_no,
          amazon_url, asin, product, img_url, genre_name, place_name) <> (LocalBookV.tupled, LocalBookV.unapply)
  
}

/**
 * DAOの定義
 */
object LocalBookViewDAO {
  lazy val localbookView = TableQuery[LocalBookView]
  
  /**
   * ID検索
   * @param shelf_id, book_id
   */
  def searchByID(shelf_id : Long, book_id: String)(implicit s: Session): LocalBookV = {
      localbookView.filter(row => (row.shelf_id === shelf_id) && (row.book_id === book_id)).first
  }

  /**
   * ジャンル検索
   * @param genre_id
   */
  def searchByGenre(genre_id : String)(implicit s: Session): List[LocalBookV] = {
      localbookView.filter(_.genre_id === genre_id).list
  }
  
  /**
   * 場所検索
   * @param genre_id
   */
  def searchByPlace(place_id : String)(implicit s: Session): List[LocalBookV] = {
      localbookView.filter(_.place_id === place_id).list
  }

  
  /**
   *キーワード検索
   * @param word
   */
  def search(shelf_id: Long, word: String)(implicit s: Session): List[LocalBookV] = {
    localbookView.filter(row => (row.shelf_id === shelf_id) &&
        ((row.abst like "%"+word+"%") || (row.title like "%"+word+"%") ||
        (row.author like "%"+word+"%") || (row.publish like "%"+word+"%"))).list
  }


  /**
   * 本棚のすべての本検索(idの昇順)
   */
  def searchAll(shelf_id: Long)(implicit s: Session): List[LocalBookV] = {
    localbookView.filter(_.shelf_id === shelf_id).sortBy(_.reg_date.desc).list
  }
  
  /**
   * 本棚のすべての本検索　最新登録5件
   */
  def search5Desc(shelf_id: Long)(implicit s: Session): List[LocalBookV] = {
    localbookView.filter(_.shelf_id === shelf_id).sortBy(_.reg_date.desc).list.take(5)
  }

  /**
   * EAN検索
   * @param shelf_id, book_id
   */
  def searchByEAN(shelf_id : Long, ean: String)(implicit s: Session): List[LocalBookV] = {
      localbookView.filter(row => (row.shelf_id === shelf_id) && (row.ean === ean)).list
  }
  
  /**
   * ISBN検索
   * @param shelf_id, book_id
   */
  def searchByISBN(shelf_id : Long, isbn: String)(implicit s: Session): List[LocalBookV] = {
      localbookView.filter(row => (row.shelf_id === shelf_id) && (row.isbn === isbn)).list
  }

  /**
   * ASIN検索
   * @param shelf_id, book_id
   */
  def searchByASIN(shelf_id : Long, asin: String)(implicit s: Session): List[LocalBookV] = {
      localbookView.filter(row => (row.shelf_id === shelf_id) && (row.asin === asin)).list
  }
}
