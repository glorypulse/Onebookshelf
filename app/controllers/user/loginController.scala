package controllers.user

/**
 * @author Emi Yoshihara
 */

import play.api._
import play.api.mvc._
//import play.api.mvc.RequestHeader // session?

import play.api.data._
import play.api.data.Forms._
import play.api.cache._


import play.api.db.slick._
import models.db._
import play.api.Play.current

import common._
import common.utils.TextConv

object LoginController extends Controller with controllers.Secured{
  
  val loginForm = controllers.form.LoginForm.form
  
  /**
   *  ログインフォーム表示
   */
  def showLoginForm() = Action { request =>
    request.session.get(Security.username) match {
      case Some(userid) => {
        //OBSCache.setGlobalError(userid.toLong, "info.already")
        Redirect(controllers.search.routes.BookController.searchAll())
      }
      case None         => Ok(views.html.login(loginForm))
    }
    
  }

  /**
   *  ログイン
   */
  def login() = DBAction { implicit request =>
    loginForm.bindFromRequest.fold(
      errors => BadRequest(views.html.login(errors)),
      loginData => {
        
        loginExec(loginData.mail, loginData.password) match {
          // ログイン成功
          case Some(user) => {
            OBSCache.setUser(user.user_id.get, user)
            Redirect(controllers.search.routes.BookController.search("")).withSession(Security.username -> user.user_id.get.toString())
          }
          // ログイン失敗
          case None       => {
            BadRequest(views.html.login(loginForm.bind(Map("mail" -> loginData.mail)).withGlobalError("error.login"))).withNewSession
          }
        }
        
       }
    )
  } 

  /**
   * ログアウト
   */
  def logout() = withAuth { user_id => implicit rs =>
    OBSCache.removeUser(user_id)
    Ok(views.html.login(loginForm.withGlobalError("info.logout"))).withNewSession
  }
  
  /**
   * ログイン実行
   * @param mail...存在するメールアドレス
   * @param passoword...対応するパスワード
   * @return Option[User] ... ログイン成功時Some(User), ログイン失敗時None
   */
  def loginExec(mail: String, password: String): Option[User] = {
    DB.withSession{ implicit session =>
      def mkUser(user: User): User = {
        User(user.user_id, user.user_name, user.mail, "", user.status, user.reg_date)
      }
      
      val userLs = UserDAO.searchByAddress(mail)
      userLs.size match {
        // 不正なユーザ
        case 0 => None
        // 通常のログイン
        case _ => {
          val user = userLs.head
          val pass = TextConv.getPassword(password, user)
          pass match {
            case user.password => Some(mkUser(user))
            case _ => None
          }          
        }
      }
    }
  }    

}