package controllers.shelf

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._
import play.api.cache.Cache

import play.api.db.slick._
import play.api.Play.current

import common._
import models.db._

import controllers.form.{BookSearch, BookSearchForm, BookReg}

object BookShelfController extends Controller with controllers.Secured {
  
	val bookShelfForm = BookSearchForm.bookSearchForm
  

  /* 本棚への本の登録 */
	def registerBook (shelf_id : Long) = withShelfAuth(shelf_id) ({ user_id =>
      // 閲覧権限のない本棚の指定があった場合は、自分のデフォルト本棚に変更
      OBSCache.setGlobalError(user_id, OBSMessage.NoAuthShelf)
      Redirect(controllers.search.routes.BookController.search(""))
  }, { auth => user_id => implicit rs =>

			bookShelfForm.bindFromRequest.fold(
					errors => {
            OBSCache.setGlobalError(user_id, "error.normal")
            Redirect(controllers.search.routes.BookController.search(""))
          },
					b_data => {
            
						b_data.book_id match {
						case "" => { //登録なし
              DB.withSession { implicit session =>
							val book = BookDAO.register(BookReg(b_data.ean, b_data.isbn, b_data.title, b_data.author, b_data.year, b_data.publish,
                                          b_data.amazon_url, b_data.asin, b_data.product, b_data.img_url))
                                          
	            Redirect(controllers.shelf.routes.BookShelfController.showRegisterBook(shelf_id)).flashing(
                  "shelf_id" -> b_data.shelf_id.toString, "book_id" -> book.book_id, "genre" -> b_data.genre, "place" -> b_data.place)
              }
            }

						case _ => { Redirect(controllers.shelf.routes.BookShelfController.showRegisterBook(shelf_id)).flashing(
                  "shelf_id" -> b_data.shelf_id.toString, "book_id" -> b_data.book_id, "genre" -> b_data.genre, "place" -> b_data.place)  }
						}  
					}
			)

	})


  /* 本棚への本の登録結果表示 */
	def showRegisterBook (shelf_id: Long) = withAuth { user_id => implicit rs =>
    val shelf_id = rs.flash.get("shelf_id").getOrElse("0L").toLong
    val book_id = rs.flash.get("book_id").getOrElse("0")
    val genre_id = rs.flash.get("genre").getOrElse("nothing")
    val place_id = rs.flash.get("place").getOrElse("nothing")
    
    if (shelf_id == 0L || book_id == "0" || genre_id == "nothing" || place_id == "nothing") {
      Redirect("/logout")
    }
    
    DB.withSession { implicit session =>
      OBSCache.removeLocalBookLs(user_id, shelf_id)
      LocalBookDAO.register(LocalBook(shelf_id, book_id, genre_id, place_id, "", null, user_id))
      Ok(views.html.bookmanage.result("", BookDAO.searchByID(book_id)))
    }
	}

 
}