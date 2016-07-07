package controllers.form

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._

import play.api.db.slick.DB
import models.db.UserDAO
import play.api.Play.current

case class Login (mail : String, password : String)

object LoginForm {
  val form = Form(
    mapping(
      "mail" -> email.verifying(OBSConstraint.nonEmpty, existsEmail),
      "password" -> nonEmptyText
    )(Login.apply)(Login.unapply)
  )
  
  /* メール存在チェック */
  def existsEmail : Constraint[String] = Constraint[String]("constraint.ex_email") { mail =>
    DB.withSession { implicit session =>
      UserDAO.searchByAddress(mail).size match {
        case n if n > 0 => Valid
        case _          => Invalid(ValidationError("error.ex_email"))
      }
    }
  }   
}

