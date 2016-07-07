package controllers.form

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._

import play.api.db.slick.DB
import models.db.UserDAO
import play.api.Play.current

case class UserReg (user_name : String, mail : String, password : String, password_con : String)

object UserRegForm {
  val form = Form(
    mapping(
      "user_name" -> nonEmptyText(maxLength = 140),
      "mail" -> email.verifying(OBSConstraint.nonEmpty, duplEmail),
      "password" -> nonEmptyText,
      "password_con" -> nonEmptyText
    )(UserReg.apply)(UserReg.unapply).verifying(pwd_con_validate))


  /* メール重複チェック */
  def duplEmail : Constraint[String] = Constraint[String]("constraint.dupl_email") { mail =>
    DB.withSession { implicit session =>
      UserDAO.searchByAddress(mail).size match {
        case n if n > 0 => Invalid(ValidationError("error.dupl_email"))
        case _          => Valid
      }
    }
  }     
    
  /* パスワード確認 - Global Constraint */
  def pwd_con_validate : Constraint[UserReg] = Constraint[UserReg]("constraint.pwd_con") { user =>
    user.password match {
      case user.password_con => Valid
      case _                 => Invalid(ValidationError("error.pwd_con")) 
    }
  }
  
}


