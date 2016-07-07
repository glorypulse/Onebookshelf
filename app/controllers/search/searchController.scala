package controllers.search

/**
 * 本の検索機能
 * @author Emi Yoshihara
 */
import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

import play.api.cache.Cache
import play.Logger

import play.api.Play.current
import play.api.db.slick._

import controllers.form.{BookSearch, BookSearchForm, BookReg}
import models.db._
import models.provider._

import common._


object BookController extends Controller with controllers.Secured{
  
  val bookSearchForm = BookSearchForm.bookSearchForm
  private val pat_bookid = """(\d{16})""".r
  private val pat_ean = """(\d{13})""".r
  private val pat_isbn_10 = """(\d{10})""".r
  private val pat_alph_10 = """(\w{10})""".r
  private val pat_all = """(\d{16}|\d{13}|\d{10}|\w{10})""".r
  
  // Logよう
  private val SEARCH_WORD = "[SEARCH_WORD] "

  /**
   * 本検索 デフォルト本棚の検索
   */
    
  def search(word: String) = withAuth {user_id => implicit rs =>
    DB.withSession { implicit session =>

      // 最初はデフォルト本棚を検索
      val default_shelf_id =  MyBookshelfDAO.searchDef(user_id, OBSConstants.DefaultShelf).shelf_id
      Redirect(controllers.search.routes.BookController.searchMyBook(default_shelf_id, word))
    }
  }
  
  /**
   * 本棚を指定して検索
   */
  def searchMyBook(shelf_id: Long, word: String) = withShelfAuth(shelf_id)({ user_id =>
      // 閲覧権限のない本棚の指定があった場合は、自分のデフォルト本棚に変更
      OBSCache.setGlobalError(user_id, OBSMessage.NoAuthShelf)
      Redirect(controllers.search.routes.BookController.search(word))
    }, {
    auth => user_id => implicit rs =>
    DB.withSession { implicit session =>
      
      // 本棚取得
      val myshelf = MyBookshelfDAO.searchByID(user_id, shelf_id).get
      
      Logger.debug (SEARCH_WORD +
        (word match {
          case "" => "word nothing"
          case pat_bookid(id) => s"BOOKID -- ${word}"
          case pat_ean(ean) => s"EAN-13 -- ${word}"
          case pat_isbn_10(isbn) => s"ISBN-10 -- ${word}"
          case pat_alph_10(alph) => s"ALPH-10 -- ${word}"
          case _ => s"free word --${word}"
        })
      )
      
      // すべての登録済みの本
      val allbookls = word match {
        case "" => List[Book]()
        case pat_bookid(id) => BookDAO.searchByID(id)
        case pat_ean(ean) => BookDAO.searchByEAN(ean)
        case pat_isbn_10(isbn) => BookDAO.searchByISBN(isbn)
        case pat_alph_10(asin) => BookDAO.searchByASIN(asin)
        case _ => BookDAO.search(word)
      }
      
      // Amazonの本
      val all_amazonls = word match {
        // 検索ワードが空のとき空
        case "" => List[BookSearch]()
        
        // BOOKID, EAN, ISBN, ASINのとき、DB登録があればAmazonには問い合わせない
        case pat_all(pat) => if(!allbookls.isEmpty) {
          List[BookSearch]()
        } else {
          val ret_ls = getAmazonbookLs(shelf_id, user_id, word)
          ret_ls.size match {
            case 1 => {
               // 単一のものが返ってきた場合は、その時点でDB登録
              val b_data = ret_ls.head
              BookDAO.register(new BookReg(b_data.ean, b_data.isbn, b_data.title, b_data.author, b_data.year, b_data.publish,
                                            b_data.amazon_url, b_data.asin, b_data.product, b_data.img_url))
            }
            case _ => {} 
          }
          ret_ls
        }
        // その他
        case _ => getAmazonbookLs(shelf_id, user_id, word)
      }
      
      // Amazonからデータを正常に取得できた場合は、キャッシュに入れておく
      if (!all_amazonls.isEmpty && (OBSCache.getGlobalError(user_id)==None)) {
        Cache.set(OBSCache.strAmazonSearchWord(word), all_amazonls, 30 * 60)
      }
      
      val allbookls_id = for (book <- allbookls) yield book.asin
      val amazonls = all_amazonls.filter { amazon => !allbookls_id.contains(amazon.asin) }
      
          
      // 本棚からの取得
      val localsearchls = getLocalbookLs(shelf_id, word)
      Cache.set(OBSCache.strLocalBookLs(user_id, shelf_id),
          localsearchls.map { lb => (lb.book_id, lb)}.toMap , 30 * 60) //詳細情報のためにキャッシュへ保存
  
      // すべての本から本棚の本を削除
      val local_id = for (localbook <- localsearchls) yield localbook.book_id
      val bookls = allbookls.filter { book => !local_id.contains(book.book_id) }
      
  
      
      val searchls = for (b <- bookls) yield {
        // val series = BookSeriesDAO.searchByID(lb.series_id)
        new BookSearch("", b.title, b.author, b.publish, "", "", b.book_id, shelf_id, b.year, b.amazon_url, b.asin, b.product, b.isbn, b.ean, b.img_url)
      }
      
      // ジャンル・場所情報の取得
      val gpMap = GPProvider.getGPMap(shelf_id)
      
      // メッセージ・エラー情報の取得
      val bsForm = OBSCache.getGlobalErrorWithForm[controllers.form.BookSearch](user_id, bookSearchForm)
      OBSCache.removeGlobalError(user_id)
      
      Ok(views.html.search(user_id, word, localsearchls, (searchls ::: amazonls).take(30), gpMap, bsForm)).withSession(Security.username -> user_id.toString)
      
    }
  })
  
  
  private def getLocalbookLs(shelf_id: Long, word : String)(implicit s: play.api.db.slick.Session) : List[BookSearch] = {
    // 本棚から本を取得
    val localbookls = word match {
      case "" => LocalBookViewDAO.searchAll(shelf_id)
      case pat_ean(ean) => LocalBookViewDAO.searchByEAN(shelf_id, ean)
      case pat_isbn_10(isbn) => LocalBookViewDAO.searchByISBN(shelf_id, isbn)
      case pat_alph_10(asin) => LocalBookViewDAO.searchByASIN(shelf_id, asin)
      case _ => LocalBookViewDAO.search(shelf_id, word)
    }
    
    // Book -> BookSearch
    for (lb <- localbookls) yield {
      new BookSearch("", lb.title, lb.author, lb.publish, lb.genre_name, lb.place_name, lb.book_id, lb.shelf_id, lb.year, lb.amazon_url, lb.asin, lb.product, lb.isbn, lb.ean, lb.img_url)
    }
  }
  
  private def getAmazonbookLs(shelf_id: Long, user_id: Long, word : String) : List[BookSearch] = {
    // Amazonの本 キャッシュにあればそれを取得
    val all_amazonls = if (word == "") {
      List[BookSearch]()
    } else {
      Cache.getAs[List[BookSearch]](OBSCache.strAmazonSearchWord(word)) match {
        case Some(amazonLs) if !amazonLs.isEmpty => amazonLs
        case _ => AmazonProductAdvertising.getBookSearch(word, shelf_id, user_id)
      }
    }
    
    // セット商品の除外
    all_amazonls.filterNot { book => book.title.contains("セット") && book.product == "Book" && book.isbn.isEmpty() }
  }

 
}