package controllers.form

import play.api.data._
import play.api.data.Forms._


case class BooksCSV (bookscsv: String, shelf_id: Long)

object BooksCSVForm {
  val form = Form(
    mapping(
      "bookscsv" -> text,
      "shelf_id" -> longNumber
    )(BooksCSV.apply)(BooksCSV.unapply)
  )
}

case class BooksCSVReg (books: List[String], shelf_id: Long)

object BooksCSVRegForm {
  val form = Form(
      mapping(
        "books"    ->  list(text),
        "shelf_id" ->  longNumber
        )(BooksCSVReg.apply)(BooksCSVReg.unapply)
      )
}