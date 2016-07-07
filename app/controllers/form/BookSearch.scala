package controllers.form

/**
 * @author Emi Yoshihara
 */

import play.api.data._
import play.api.data.Forms._

// BookSearch (series_name, title, author, publish, genre, place, book_id, shelf_id, year, amazon_url, asin, product, isbn, ean, img_url)
case class BookSearch(series_name: String, title: String, author: String,
    publish: String, genre: String, place: String, book_id : String, shelf_id : Long,
    year: Int, amazon_url: String, asin: String, product: String, isbn: String, ean: String,
    img_url: String)

    
object BookSearchForm {
  val bookSearchForm = Form(
      mapping(
          "series_name" -> text,
          "title" ->  nonEmptyText,
          "author" ->  nonEmptyText(maxLength = 140),
          "publish" ->  nonEmptyText(maxLength = 140),
          "genre" -> text,
          "place" -> text,
          "book_id" -> text,
          "shelf_id" ->  longNumber,
          "year" ->  number,
          "amazon_url" -> text,
          "asin" -> text(maxLength = 10),
          "product" -> text,
          "isbn" -> text(maxLength = 13),
          "ean" -> text(maxLength = 13),
          "img_url" -> text
          )(BookSearch.apply)(BookSearch.unapply)
      )
}