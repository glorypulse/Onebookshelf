package controllers.bookmanage

/**
 * @author Emi Yoshihara
 */

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._
import controllers.form.{BookReg, BookRegForm}

import play.api.db.slick._
import models.db._
import play.api.Play.current

import common._


object BookRegController extends Controller with controllers.Secured{

  val bookRegForm = BookRegForm.bookRegForm
  
  /**
   *  本情報登録フォーム表示アクションメソッドの定義
   */
  def showRegisterForm() = withAuth { user_id => implicit request =>
    user_id match {
      case 1 => Ok(views.html.bookRegForm(bookRegForm))
      case _ => {
        OBSCache.setGlobalError(user_id, "error.noauth")
        Redirect(controllers.search.routes.BookController.search(""))
      }
    }
    
  }

  /**
   *  本情報登録アクションメソッドの定義
   */
  def register() = withAuth { user_id => implicit request =>
    user_id match {
      case 1 => bookRegForm.bindFromRequest.fold(
        errors => BadRequest(views.html.bookRegForm(errors)),
        bookReg => {
          DB.withSession{ implicit session =>        
   // TODO シリーズ        
   //        val series_id = BookSeriesDAO.searchByName(series_name) match {
   //          case Some(bs : BookSeries) => bs.series_id.get
   //         case _                     => 
   //            BookSeriesDAO.register(series_name)
   //        }
          val book = new BookReg(bookReg.ean, bookReg.isbn, bookReg.title, bookReg.author, bookReg.year, bookReg.publish,
                            "URL", "ASIN", "PRODUCT", "IMG_URL")
          val new_book = BookDAO.register(book)
          Redirect(controllers.bookmanage.routes.BookRegController.searchByID(new_book.book_id))
          }
        }
      )
      case _ => {
        OBSCache.setGlobalError(user_id, "error.noauth")
        Redirect(controllers.search.routes.BookController.search(""))
      }
    }
      

  }

  /**
   *  本情報検索アクションメソッドの定義（ID検索）　…結果表示
   *  @param id
   */
  def searchByID(id: String) = withAuth { user_id => implicit rs => 
    DB.withSession{ implicit session =>
      Ok(views.html.bookmanage.result(id, BookDAO.searchByID(id)))
  }}
 
}