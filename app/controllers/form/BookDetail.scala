package controllers.form

import play.api.data._
import play.api.data.Forms._

import models.db.MyBook

case class BookInfo (book_id: String, shelf_id: Long)

object BookInfoForm {
  val form = Form(
    mapping(
      "book_id" -> nonEmptyText,
      "shelf_id" -> longNumber
    )(BookInfo.apply)(BookInfo.unapply)
  )
}

case class BookDetail(series_name: String, title: String, author: String,
    publish: String, genre: String, place: String, book_id : String, shelf_id : Long,
    year: Int, amazon_url: String, asin: String, product: String, isbn: String, ean: String, img_url: String,
    friendcomment : List[MyComment])


case class BookGP (genre_id: String, place_id: String)

object BookGPForm {
  val form = Form(
    mapping(
      "genre_id" -> nonEmptyText,
      "place_id" -> nonEmptyText
    )(BookGP.apply)(BookGP.unapply)
  )
}