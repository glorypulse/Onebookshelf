package controllers.shelf

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._
import play.api.cache.Cache
import play.api.i18n.Messages

import play.api.db.slick._
import play.api.Play.current

import common._
import models.db._

import controllers.form.{BookSearch, BookReg}

object CSVBookController extends Controller with controllers.Secured {
  
  val booksCSVForm = controllers.form.BooksCSVForm.form
  val booksCSVRegForm = controllers.form.BooksCSVRegForm.form

  private def getShelfGroup(user_id: Long) : List[(String, String)] =  {
    DB.withSession { implicit session =>
      for (shelf <- MyBookshelfDAO.search(user_id).filter(_.auth_flag == OBSConstants.Owner)) yield {
        (shelf.shelf_id.toString, BookshelfDAO.searchByID(shelf.shelf_id).shelf_name)
      }
    }
  }
  
  /* 本棚への本複数登録（CSV） 表示 */
  def showRegisterBooksWithCSVForm() = withAuth { user_id => implicit rs =>
    val shelf_group = getShelfGroup(user_id)
    val booksCSVForm_withError = OBSCache.getGlobalError(user_id) match {
      case Some(error_mes) => booksCSVForm.withGlobalError(Messages(error_mes))
      case None  => booksCSVForm
    }
    Ok(views.html.booksCSVRegForm(shelf_group, booksCSVForm_withError, List[BookSearch]()))
  }
  
  
  /* 本棚への本複数登録　… CSVを取得、Amazonへ問い合わせ、表示 */
  def uploadCSV() = withAuth { user_id => implicit request =>  
    var regLs = List[BookSearch]()
    var errormes = ""
    
    val shelf_group = getShelfGroup(user_id)
    
    request.body.asMultipartFormData.get.file("bookscsv").map { bookscsv =>
      val bufferedSource = io.Source.fromFile(bookscsv.ref.file)
      val (shelf_id, filename) = booksCSVForm.bindFromRequest.fold(
      errors => {
        // shelf_idがないときはデフォルトを入れておく
        DB.withSession { implicit session =>
          (MyBookshelfDAO.searchDef(user_id, OBSConstants.DefaultShelf).shelf_id, bookscsv.filename)
        }
      },
      bcform => {
        (bcform.shelf_id, bcform.bookscsv)
      })
      
  
      try {
        bufferedSource.getLines.zipWithIndex.foreach { line_i =>
            val cols = line_i._1.split(",").map(_.trim)
            val index = line_i._2 + 1
            
            val input_isbn = cols(0)
            controllers.search.AmazonProductAdvertising.getBookWithISBN(input_isbn, shelf_id, user_id) match {
              case Some(book) => {
                
                // if (input_isbn.takeRight(10).equals(book.isbn.takeRight(10))) { // 正しい本が検索できているか確認...EANで比較する必要あり
                  regLs = regLs :+ (cols.size match {
                    case n if n > 2 =>  BookSearch(book.series_name, book.title, book.author, book.publish,
                        cols(2), cols(1), book.book_id, book.shelf_id, book.year, book.amazon_url, book.asin, book.product, book.isbn, book.ean, book.img_url)
                    case 2 =>  BookSearch(book.series_name, book.title, book.author, book.publish,
                        book.genre, cols(1), book.book_id, book.shelf_id, book.year, book.amazon_url, book.asin, book.product, book.isbn, book.ean, book.img_url)
                    case _ =>  book
                  })
                // } else {
                //   errormes = errormes + s"${index}行目のデータで正しい本を取得できません。<br>"
                // }
              }
              case None       => {
                  errormes = errormes + s"${index}行目のデータ" + (OBSCache.getGlobalError(user_id) match {
                    case Some(amazon) if amazon=="error.amazon" => {
                        OBSCache.removeGlobalError(user_id)
                        "取得時にエラーが発生しました。時間を空けて再度試行してください。<br>"
                      }
                    case Some(error) => "にエラーが発生しました。<br>"
                    case None        => "にエラーが発生しました。<br>"
                  })
                }
            }
            
        }
      } catch {
        case e: Throwable => {
          Logger.error(s"CSV Input Error: User: ${user_id}, Filename ${filename}, ", e)
         errormes = errormes + "ファイル内容取得時にエラーが発生しました。<br>" 
        }
      }
        bufferedSource.close
      
      Cache.set(OBSCache.strCSVUpload(user_id, shelf_id), regLs, 30 * 60)
      Ok(views.html.booksCSVRegForm(shelf_group, booksCSVForm.bind(Map("bookscsv" -> filename, "shelf_id" -> shelf_id.toString)).withGlobalError(errormes), regLs))
    }.getOrElse {
      
      BadRequest(views.html.booksCSVRegForm(shelf_group, booksCSVForm.withGlobalError(Messages("error.normal")), List[BookSearch]()))
    }
  }
  
  def registerCSV() = withAuth { user_id => implicit request =>
    booksCSVRegForm.bindFromRequest.fold(
        errors   => {
          OBSCache.setGlobalError(user_id, "error.normal")
          Redirect(controllers.shelf.routes.CSVBookController.showRegisterBooksWithCSVForm())
        },
        booksReg => {
          val shelf_id = booksReg.shelf_id
          var regedBooks = List[Book]()
          
          Cache.getAs[List[BookSearch]](OBSCache.strCSVUpload(user_id, shelf_id)) match {
          case Some(regLs) => {
            regLs.filter(booksReg.books contains _.ean).foreach { rb => 
              DB.withSession { implicit session =>
                val genre_id = LocalGenreDAO.search(rb.genre, shelf_id) match {
                  case genre if !genre.isEmpty => genre.head.genre_id
                  case _ => { LocalGenreDAO.register(rb.genre, shelf_id).genre_id
                  }
                }
                val place_id = LocalPlaceDAO.search(rb.place, shelf_id) match {
                  case place if !place.isEmpty => place.head.place_id
                  case _ => { LocalPlaceDAO.register(rb.place, shelf_id).place_id
                  }
                }
                
                val book = BookDAO.searchByISBN(rb.isbn) match {
                  case dbBook if !dbBook.isEmpty => {
                    dbBook.head
                  }
                  case _ => {
                    BookDAO.register(BookReg(rb.ean, rb.isbn, rb.title, rb.author, rb.year, rb.publish,
                                          rb.amazon_url, rb.asin, rb.product, rb.img_url))
                  }
                }
                
                
                if(!LocalBookDAO.existsByID(shelf_id, book.book_id)) {
                  LocalBookDAO.register(LocalBook(shelf_id, book.book_id, genre_id, place_id, "", null, user_id))
                  regedBooks = regedBooks :+ book
                } // TODO エラーメッセージなし

              }
            }
            Cache.set(OBSCache.strCSVReged(user_id, shelf_id), regedBooks, 30* 60)
            Redirect(controllers.shelf.routes.CSVBookController.showResultRegCSV(shelf_id))            
          }
          case None => {
            OBSCache.setGlobalError(user_id, "error.onemore")
            Redirect(controllers.shelf.routes.CSVBookController.showRegisterBooksWithCSVForm())
          }
        }
          
          }
        )
    

  }
  
  def showResultRegCSV(shelf_id : Long) = withAuth { user_id => implicit request =>
    val shelf_name = DB.withSession { implicit session =>
      BookshelfDAO.searchByID(shelf_id).shelf_name
    }
    Cache.getAs[List[Book]](OBSCache.strCSVReged(user_id, shelf_id)) match {
      case Some(rbs) => {
        val regedBooks = rbs
        Ok(views.html.bookmanage.result(s"${shelf_name}に、", regedBooks))
      }
      case None => {
            OBSCache.setGlobalError(user_id, "error.noresult")
            Redirect(controllers.shelf.routes.CSVBookController.showRegisterBooksWithCSVForm())
      }
    }
    
  }

 
}