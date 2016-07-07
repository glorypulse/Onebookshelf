package controllers.user

/**
 * @author Emi Yoshihara
 */

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._
import controllers.form.{UserRegForm}

import play.api.db.slick._
import models.db._
import play.api.Play.current

import common._
import common.utils.TextConv




object UserRegController extends Controller{

  /**
   * ユーザフォーム
   */
  val userRegForm = UserRegForm.form
  
  
  /**
   *  ユーザ登録フォーム表示アクションメソッドの定義
   */
  def showRegisterForm() = Action { implicit request =>
    
    Ok(views.html.userRegForm(userRegForm))
  }

  /**
   *  ユーザ登録アクションメソッドの定義
   */
  def register() = Action { implicit rs =>
    userRegForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.userRegForm(errors))
      },
      userReg => {
        
        // パスワード変換
        val first_password = TextConv.getFirstPassword(userReg.password)
        
        DB.withTransaction { implicit session =>
          val user_id = UserDAO.register(User(None, userReg.user_name, userReg.mail, first_password, OBSConstants.NormalStatus, null))
          UserDAO.setPassword(user_id, first_password)
          
          
          // 本棚登録
          val shelf_id = BookshelfDAO.register(new Bookshelf(None, s"${userReg.user_name}${OBSConstants.DefaultSName}", OBSConstants.PrivateShelf))
          MyBookshelfDAO.register(new MyBookshelf(user_id, shelf_id, OBSConstants.Owner, OBSConstants.DefaultShelf))
          
          // 場所・ジャンル登録
          LocalPlaceDAO.registerFirst(shelf_id)
          LocalGenreDAO.registerFirst(shelf_id)
          
          // ログイン処理
          LoginController.loginExec(userReg.mail, userReg.password)
          
          Redirect(controllers.user.routes.UserRegController.searchByID(user_id)).withSession(Security.username -> user_id.toString)
        }
                        
      }
    )
  }

  /**
   *  本情報検索アクションメソッドの定義（ID検索）　…結果表示
   *  @param id
   */
  def searchByID(id: Long) = DBAction { implicit rs =>
    Ok(views.html.userRegResult(id.toString(), UserDAO.searchByID(id)))
  }
 
}