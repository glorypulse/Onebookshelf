package controllers.search

/**
 * @author Emi Yoshihara
 */

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._
import play.api.cache.Cache

import play.api.db.slick._
import models.db._
import play.api.Play.current

import common._
import controllers.form.{BookSearch, BookInfo, BookDetail, MyComment}
import models.provider.GPProvider

object BookDetailController extends Controller with controllers.Secured{
  
  val bookForm = controllers.form.BookInfoForm.form
  val myCommentForm = controllers.form.MyCommentForm.form
  val gpForm = controllers.form.BookGPForm.form 
  
   /**
    *  本詳細情報表示, ユーザーコメント更新フォーム表示
    */
  
  def showBook() = withAuth { user_id => implicit request =>
       bookForm.bindFromRequest.fold(
         errors => {
          OBSCache.setGlobalError(user_id, "error.abnormal")
          Logger.error(errors.toString)
          Redirect(controllers.search.routes.BookController.search(""))
         },
         b_data => {
           Redirect(controllers.search.routes.BookDetailController.showBookDetail(b_data.shelf_id, b_data.book_id))
         })
  }
  
  def showBookDetail(shelf_id: Long, book_id: String) = withShelfAuth(shelf_id)({ user_id =>
      OBSCache.setGlobalError(user_id, "error.noauth.shelf")
      Redirect(controllers.search.routes.BookController.search(book_id))
    },{
      auth => user_id => implicit rs =>
       DB.withSession { implicit session => 
         (LocalBookDAO.existsByID(shelf_id, book_id) && BookDAO.existsByID(book_id)) match {
           case false => {
             OBSCache.setGlobalError(user_id, "error.nobook")
             Redirect(controllers.search.routes.BookController.searchMyBook(shelf_id, book_id))
           }
           case _     => {
             val username = OBSCache.getUser(user_id).user_name
             
             // コメントの取得
             val mybookLs = DB.withSession { implicit session =>
               if (MyBookDAO.searchByID(user_id, shelf_id, book_id).isEmpty) {
                 MyBookDAO.register(user_id, shelf_id, book_id)
               }
               MyBookDAO.searchByLocalbook(shelf_id, book_id)
             }
             
             val mycomment = new MyComment(username, mybookLs.filter( _.user_id == user_id ).head)
             
             val friendcomment = for (fbook <- mybookLs.filter(_.user_id != user_id)) yield {
                val fname = UserDAO.searchByID(fbook.user_id).user_name
                new MyComment(fname, fbook) 
             }
             
             // 本情報の取得
             val bookDetail = Cache.getAs[BookDetail](OBSCache.strBookDetail(user_id, book_id, shelf_id)) match {
               case Some(bd) => bd
               case None     => {
                 (Cache.getAs[Map[String, BookSearch]](OBSCache.strLocalBookLs(user_id, shelf_id)) match {
                   case Some(bsearchLs) => bsearchLs
                   case None            => Map[String, BookSearch]()
                 }).get(book_id) match {
                   case Some(book) => BookDetail(book.series_name, book.title, book.author, book.publish, book.genre, book.place,
                                             book.book_id, book.shelf_id, book.year, book.amazon_url, book.asin, book.product, book.isbn, book.ean, book.img_url, friendcomment)
                   case None     => DB.withSession{ implicit session =>
                     val book = LocalBookViewDAO.searchByID(shelf_id, book_id)
                     // TODO get series_name
                     BookDetail("", book.title, book.author, book.publish, book.genre_name, book.place_name,
                                             book.book_id, book.shelf_id, book.year, book.amazon_url, book.asin, book.product, book.isbn, book.ean, book.img_url, friendcomment)
                   }
                 }
               }
             }
             Cache.set(OBSCache.strBookDetail(user_id, book_id, shelf_id), bookDetail, 60*30)
             
             // ジャンル・場所情報の取得
             val gpMap = GPProvider.getGPMap(shelf_id)
             val gp_id = GPProvider.getGPIDMap((bookDetail.genre, bookDetail.place), shelf_id)
             
             
             // メッセージ・エラー情報の取得 及びbind
             val gpForm_bind = OBSCache.getGlobalErrorWithForm[controllers.form.BookGP](user_id, gpForm).bind(gp_id)
             OBSCache.removeGlobalError(user_id)
             
             Ok(views.html.bookDetail(shelf_id, bookDetail, mycomment, myCommentForm, gpForm_bind, gpMap))             
             
             
             
           }
         }
         

       }
  })
  
  /**
   * 本棚の本情報更新
   */
  def updateBookInfo(shelf_id: Long, book_id: String) = withAuth { user_id => implicit request =>
    gpForm.bindFromRequest.fold(
      errors => {
        OBSCache.setGlobalError(user_id, "error.abnormal")
        Logger.error(errors.toString)
        Redirect(controllers.search.routes.BookDetailController.showBookDetail(shelf_id, book_id))
      },
      gp_id => {
        DB.withSession { implicit session =>
          val genre_id = gp_id.genre_id
          val place_id = gp_id.place_id
          if(LocalGenreDAO.exists(genre_id)) {
            LocalBookDAO.updateGenre(shelf_id, book_id, gp_id.genre_id)
          } 
          if(LocalPlaceDAO.exists(place_id)) {
            LocalBookDAO.updatePlace(shelf_id, book_id, gp_id.place_id)
          }
          OBSCache.removeLocalBookLs(user_id, shelf_id)
          OBSCache.removeBookDetail(user_id, book_id, shelf_id)
          Redirect(controllers.search.routes.BookDetailController.showBookDetail(shelf_id, book_id))
        }
      })
  }
  
  /**
   *  ユーザーコメント更新
   */
  def updateComment(shelf_id : Long, book_id : String) = withAuth { user_id => implicit request =>
    myCommentForm.bindFromRequest.fold(
        errors  => {
          OBSCache.setGlobalError(user_id, "error.abnormal")
          Logger.error(errors.toString)
          Redirect(controllers.search.routes.BookDetailController.showBookDetail(shelf_id, book_id))
        },
        comment => {
          DB.withSession { implicit session =>
            MyBookDAO.update(new MyBook(user_id, shelf_id, book_id, comment))
            OBSCache.removeBookDetail(user_id, book_id, shelf_id)
            Redirect(controllers.search.routes.BookDetailController.showBookDetail(shelf_id, book_id))
          }       
        }
        )
  }
  
}