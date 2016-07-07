package controllers.form

import play.api.data._
import play.api.data.Forms._

/**
 * 本情報登録のためのCase class, Form
 * @author glorypulse
 */

case class BookReg(ean: String, isbn: String,  title: String, author: String, year:Int, publish: String,
    amazon_url: String, asin: String, product: String, img_url: String)

object BookRegForm {
  val bookRegForm = Form(
    mapping(
      "ean"  -> text(maxLength = 15),
      "isbn" -> text(maxLength = 15),
      "title" ->  nonEmptyText,
      "author" ->  nonEmptyText(maxLength = 140),
      "year" ->  number,
      "publish" ->  nonEmptyText(maxLength = 140),
      //"series_name" ->  optional(text),
      //"series_no" -> optional(number)
      "amazon_url" -> text,
      "asin" -> text(maxLength = 10),
      "product" -> text,
      "img_url" -> text
    )(BookReg.apply)(BookReg.unapply)
  )
}