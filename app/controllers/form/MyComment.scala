package controllers.form

import play.api.data._
import play.api.data.Forms._

import java.util.Date

/**
 * 本のコメントフォーム Case class, Form
 * @author glorypulse
 */

    
case class MyComment(username: String, read_flag: Int, impression: String, memo: String,
    start_day: Option[Date], finish_day: Option[Date]) {
    //, understanding: Short, evaluation: Short)
  def this (username: String, mybook: models.db.MyBook) {
    this(username: String, mybook.read_flag, mybook.impression, mybook.memo,
        mybook.start_day match {
      case Some(sd) => Some(new Date(sd.getTime()))
      case None => None
        },
        mybook.finish_day match {
      case Some(fd) => Some(new Date(fd.getTime()))
      case None => None
    })
  }
  
  def this (username: String, comment: MyComment) {
    this(username, comment.read_flag, comment.impression, comment.memo, comment.start_day, comment.finish_day)
  }
}

object MyCommentForm {
  val form = Form(
    mapping(
      "username" -> text,
      "read_flag" -> number,
      "impression" -> text,
      "memo" -> text,
      "start_day" -> optional(date),
      "finish_day" -> optional(date)
    )(MyComment.apply)(MyComment.unapply)
  )
}