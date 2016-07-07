package controllers

/**
 *  認証
 * */

import play.api._;
import play.api.mvc._;
import controllers.form.LoginForm

import play.api.cache._
import play.api.db.slick.DB

import models.DBAccess
import models.db._
import play.api.Play.current

import common._


trait Secured{
  
  def userid(request: RequestHeader) = request.session.get(Security.username)
    
  def onUnauthorized(request : RequestHeader) = {
    Results.Ok(views.html.login(LoginForm.form.withGlobalError("info.timeout"))).withNewSession
    // 表示しようとしていたページを再度表示したい TODO
  }
  
  def withAuth(f: => Long => Request[AnyContent] => Result) = {
    Security.Authenticated(userid, onUnauthorized) { user =>
      val user_id = user.toLong
      val authUser = OBSCache.getUser(user_id)
      OBSCache.updateUser(user_id)
      Action(request => f(user_id)(request))
    }
  }
  
  def withShelfAuth(shelf_id: Long)(noauth: Long => Result, auth: Short => Long => Request[AnyContent] => Result) = withAuth { user_id => implicit request =>
    val auth_flag = isShelfAuth(user_id, shelf_id)
    auth_flag match {
      case Some(flag) => auth(flag)(user_id)(request)
      case None       => noauth(user_id)
    }
  }
  
  def withOwnerAuth(shelf_id: Long)(noauth: Long => Result, auth: Long => Request[AnyContent] => Result) = withAuth { user_id => implicit request =>
    val auth_flag = isShelfAuth(user_id, shelf_id)
    auth_flag match {
      case Some(flag) if flag == common.OBSConstants.Owner => auth(user_id)(request)
      case _  => noauth(user_id)
    }
  }

  /**
   * 本棚の権限確認
   */
  def isShelfAuth(user_id: Long, shelf_id: Long): Option[Short] = {
    DB.withSession{ implicit session =>
      BookshelfDAO.searchByID(shelf_id).privacy match {
        case OBSConstants.PrivateShelf     => {
          MyBookshelfDAO.searchByID(user_id, shelf_id) match {
            case Some(myshelf) if myshelf.auth_flag == OBSConstants.Owner => Some(myshelf.auth_flag)
            case _                                                        => None
          }
        }
        case OBSConstants.PublicShelf      => {
          MyBookshelfDAO.searchByID(user_id, shelf_id) match {
            case Some(myshelf) => Some(myshelf.auth_flag)
            case None          => Some(OBSConstants.Reader)
          }
        }
        case OBSConstants.SelectableShelf  => {
          MyBookshelfDAO.searchByID(user_id, shelf_id) match {
            case Some(myshelf) => Some(myshelf.auth_flag)
            case None          => None
          }
        }
        case _ => None
      }
    }
  }
  
  
  
}