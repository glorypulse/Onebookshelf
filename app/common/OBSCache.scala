package common

/**
 * @author Emi Yoshihara
 */

import play.api.cache.Cache
import play.api.Play.current
import play.api.data._

import play.api.db.slick._
import models.db._
import play.api.Play.current

import models.db.User

object OBSCache {
  /* ** キャッシュ一覧 ** */
  val DEFAULT_CACHETIME = 60*30
  
  /* Common */
  def strGlobalError(user_id: Long) : String = s"GlobalError.${user_id}"
  def strUser(user_id: Long): String = s"${user_id}.user"
  def strNumberOfBooks(shelf_id: Long): String = s"books.in.${shelf_id}"
  
  /* Search */
  def strLocalBookLs(user_id: Long, shelf_id: Long): String = s"LB.${user_id}.${shelf_id}"
  def strAmazonSearchWord(word : String): String = s"Amazon.${word}"
  def strBookDetail(user_id: Long, book_id: String, shelf_id: Long): String = s"${user_id}.see.${book_id}.in.${shelf_id}"
  
  /* Shelf */
  //def strShelfManageForm(user_id: Long, shelf_id: Long): String = s"ShelfManForm.${user_id}.${shelf_id}"
  def strCSVUpload(user_id: Long, shelf_id: Long): String = s"CSVUpload.${user_id}.${shelf_id}"
  def strCSVReged(user_id: Long, shelf_id: Long): String = s"CSVReged.${user_id}.${shelf_id}"
  
  
  /* Common */
  /**
   * グローバルエラー　エラーメッセージ
   * set: エラー発生時
   * get: viewを呼び出すコントローラ
   * remove: コントローラで呼び出し時
   */
  def getGlobalError(user_id: Long): Option[String] = {
    Cache.getAs[String](strGlobalError(user_id))
  }
  
  def getGlobalErrorWithForm[T](user_id: Long, form: Form[T]) : Form[T] = {
    getGlobalError(user_id) match {
      case Some(message) => form.withGlobalError(message)
      case None          => form 
    } 
  }
  def setGlobalError(user_id: Long, error_message: String) = {
    Cache.set(strGlobalError(user_id), error_message, 60)
  }
  def removeGlobalError(user_id: Long) {
    Cache.remove(strGlobalError(user_id))
  }
  
  /**
   * ユーザクラス
   * set: ログイン時
   * get: 本詳細表示
   * remove: ログアウト時
   */

  def getUser(user_id: Long): User = {
    Cache.getOrElse[User](strUser(user_id), DEFAULT_CACHETIME) {
      DB.withSession { implicit session =>
        val user = UserDAO.searchByID(user_id)
        User(user.user_id, user.user_name, user.mail, "", user.status, user.reg_date)
      }
    }
  }
  def setUser(user_id: Long, user: User): Unit = {
    Cache.set(strUser(user_id), user, DEFAULT_CACHETIME)    
  }
  // TODO updateはいらない？
  def updateUser(user_id: Long): Unit = {
    setUser(user_id, getUser(user_id))
  }
  def removeUser(user_id: Long): Unit = {
    Cache.remove(strUser(user_id))
  }
  
  /**
   * 本棚の冊数
   * set: 本棚を表示するとき
   * get: sidebar
   * remove: ログアウト時
   */
  
  def getNumberOfBooks(shelf_id: Long): String = {
    Cache.getOrElse[String](strNumberOfBooks(shelf_id), DEFAULT_CACHETIME) {
      DB.withSession { implicit session =>
        val numOfBooks = LocalBookDAO.countBooks(shelf_id)
        numOfBooks.toString
      }
    }
  }
  
  def setNumberOfBooks(shelf_id: Long, number_of_books: Long): Unit = {
    Cache.set(strNumberOfBooks(shelf_id), number_of_books.toString, DEFAULT_CACHETIME)    
  }
  
  def updateNumberOfBooks(shelf_id: Long): Unit = {
    val numOfBooks = DB.withSession { implicit session =>
        LocalBookDAO.countBooks(shelf_id)
    }
    setNumberOfBooks(shelf_id, numOfBooks)
  }
  
  def removeNumberOfBooks(shelf_id: Long): Unit = {
    Cache.remove(strNumberOfBooks(shelf_id))
  }
  
  
  
  
  
  /* Search */
  /**
   * Amazon検索の結果のキャッシュ
   * set: 本棚検索時
   */
  def removeAmazonSearchWord(word : String) {
    Cache.remove(strAmazonSearchWord(word))
  }
  
  /**
   * 本棚検索の結果のキャッシュ
   * set: 本棚検索時
   * get: 本詳細表示
   * remove: ジャンル・場所の更新、本の新規登録
   */
  def setLocalBookLs(user_id: Long, shelf_id: Long, localBookLs: List[controllers.form.BookSearch]) {
    Cache.set(OBSCache.strLocalBookLs(user_id, shelf_id),
      localBookLs.map { lb => (lb.book_id, lb) }.toMap, DEFAULT_CACHETIME)
  }
  def removeLocalBookLs(user_id: Long, shelf_id: Long) {
    Cache.remove(strLocalBookLs(user_id, shelf_id))
    
  }
  
  /**
   * 本詳細情報のキャッシュ
   * set: 本詳細表示
   * get: 本詳細表示
   * remove: ジャンル・場所の更新、コメントの更新
   */
  def removeBookDetail(user_id: Long, book_id: String, shelf_id: Long) {
    Cache.remove(strBookDetail(user_id, book_id, shelf_id))
  }
  
 // /**
 //  * 本棚管理フォーム（該当のフォームシンボル, Form）
 //  * set: エラー発生時
 //  * get: フォーム表示
 //  * remove: フォーム表示 
 //  */
//  def getShelfManageForm[T](user_id: Long, shelf_id: Long): Option[Form[T]] = {
//    Cache.getAs[Form[T]](strShelfManageForm(user_id, shelf_id))
//  }
//  
//  def setShelfManageForm[T](user_id: Long, shelf_id:Long, form: Form[T]) = {
//    Cache.set(strShelfManageForm(user_id, shelf_id), form, 60)
//  }
//  
//  def removeShelfManageForm(user_id: Long, shelf_id: Long) = {
//    Cache.remove(strShelfManageForm(user_id, shelf_id))
//  }
  
  /* Shelf */
  /**
   * 本複数登録時のキャッシュ
   * set: 
   */
}