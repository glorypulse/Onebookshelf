package models.db

/**
 * @author Emi Yoshihara
 */


import play.api.db.slick.Config.driver.simple._
import java.sql.Timestamp
import controllers.form.BookReg

/**
 * DTOの定義
 */
case class Book(book_id: String, ean: String, isbn: String,  title: String, author: String, year:Int, publish: String, series_id: Option[Long],
    series_no: Option[Int], amazon_url: String, asin: String, product: String, img_url: String, update_date: Timestamp)

/**
 *  テーブルスキーマの定義
 */
class BookTable(tag: Tag) extends Table[Book](tag, "Book") {
  def book_id = column[String]("book_id", O.PrimaryKey)
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
  def update_date = column[Timestamp]("update_date")

  
  def * = (book_id, ean, isbn, title, author, year, publish, series_id, series_no, amazon_url, asin, product, img_url, update_date) <> (Book.tupled, Book.unapply)
  
  // foreign key
  def fk_series_book = foreignKey("fk_series_book", series_id, BookSeriesDAO.bookseries)(_.series_id)
}

/**
 * DAOの定義
 */
object BookDAO {
  lazy val book = TableQuery[BookTable]
  
  
  
  
  /**
   * ID検索
   * @param id
   */
  def searchByID(book_id: String)(implicit s: Session): List[Book] = {
      book.filter(_.book_id === book_id ).list
  }
  
  /**
   * ID存在確認
   * @param book_id
   */
  def existsByID(book_id: String)(implicit s: Session): Boolean = {
    !searchByID(book_id).isEmpty
  }

  /**
   * EAN検索
   * @param isbn
   */
  def searchByEAN(ean: String)(implicit s: Session): List[Book] = {
      book.filter(_.ean === ean ).list
  }
  /**
   * ISBN検索
   * @param isbn
   */
  def searchByISBN(isbn: String)(implicit s: Session): List[Book] = {
      book.filter(_.isbn === isbn ).list
  }
  
  /**
   * ASIN検索
   * @param isbn
   */
  def searchByASIN(asin: String)(implicit s: Session): List[Book] = {
      book.filter(_.asin === asin ).list
  }


  /**
   * キーワード検索
   * @param word
   */
  def search(word: String)(implicit s: Session): List[Book] = {
    book.filter(row => (row.title like "%"+word+"%") || (row.author like "%"+word+"%") || 
        (row.publish like "%"+word+"%")).list
  }


  /**
   * 全件検索(idの昇順)
   */
  def searchAll()(implicit s: Session): List[Book] = {
    book.sortBy(_.book_id.asc).list
  }

  /**
   * 全件検索(idの降順)
   */
  def searchAllDesc(implicit s: Session): List[Book] = {
    book.sortBy(_.book_id.desc).list
  }
  
  /**
   * 登録
   * @param book_data
   */
  def register(book_data : BookReg)(implicit s: Session): Book = {
    // book_id 
    val isKindle = book_data.isbn match {
      case "" => 100000
      case _  => 0
    }
    val book_id = "%06d".format(isKindle) + book_data.asin

    // current_date
    //val current_date = new Date((new java.util.Date()).getTime)
    val current_date = new Timestamp(System.currentTimeMillis())
    
    // series_*
    val series_name = None
    val series_no = None
    
    val new_book = new Book(book_id, book_data.ean, book_data.isbn, book_data.title, book_data.author, book_data.year, book_data.publish, series_name,
                            series_no, book_data.amazon_url, book_data.asin, book_data.product, book_data.img_url, current_date)
    
    register(new_book)
    
    new_book
  }

  /**
   * 登録（外部からは使用しない）
   * @param book_data
   */
  private def register(book_data : Book)(implicit s: Session) {
    book.insert(book_data)
  }

  /**
   * 更新
   * @param book_data
   */
  def update(book_data: Book)(implicit s: Session) {
    book.filter(_.book_id === book_data.book_id).update(book_data)
  }

  /**
   * 削除
   * @param book_data
   */
  def remove(book_data : Book)(implicit s: Session) {
    book.filter(_.book_id === book_data.book_id).delete
  }
}
