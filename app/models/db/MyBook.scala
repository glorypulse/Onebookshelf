package models.db

/**
 * MyBook DTO
 * @author Emi Yoshihara
 */


import play.api.db.slick.Config.driver.simple._
import java.sql._

import controllers.form.MyComment
import common._

/**
 * DTO Case Class
 */
case class MyBook(user_id: Long, shelf_id: Long, book_id: String, read_flag: Short, impression: String, memo: String,
    start_day: Option[Date], finish_day: Option[Date], understanding: Short, evaluation: Short) {
  
  def this(user_id: Long, shelf_id: Long, book_id: String, comment : MyComment) {
    this (user_id, shelf_id, book_id, comment.read_flag.toShort, comment.impression, comment.memo,
        comment.start_day match {
          case Some(sd) => Some(new Date(sd.getTime()))
          case None => None
        },
        comment.finish_day match {
          case Some(fd) => Some(new Date(fd.getTime()))
          case None => None
        }, 0.toShort, 0.toShort) // TODO understandingとevaluationは未実装
  }
}

/**
 *  Table Schema
 */
class MyBookTable(tag: Tag) extends Table[MyBook](tag, "MyBook") {
  def user_id = column[Long]("user_id", O.PrimaryKey)
  def shelf_id = column[Long]("shelf_id", O.PrimaryKey)
  def book_id = column[String]("book_id", O.PrimaryKey)
  def read_flag = column[Short]("read_flag")
  def impression = column[String]("impression")
  def memo = column[String]("memo")
  def start_day = column[Option[Date]]("start_day", O.Nullable)
  def finish_day = column[Option[Date]]("finish_day", O.Nullable)
  def understanding = column[Short]("understanding")
  def evaluation = column[Short]("evaluation")


  
  def * = (user_id, shelf_id, book_id, read_flag, impression, memo, start_day, finish_day, understanding, evaluation) <> (MyBook.tupled, MyBook.unapply)
  
  // foreign key
  def fk_user_mybook = foreignKey("fk_user_mybook", user_id, UserDAO.user)(_.user_id)
  def fk_localbook_mybook = foreignKey("fk_localbook_mybook", (shelf_id, book_id), LocalBookDAO.localbook)((lb => (lb.shelf_id, lb.book_id)))
}

/**
 * DAO
 */
object MyBookDAO {
  lazy val mybook = TableQuery[MyBookTable]
  
  /**
   * ID検索　（empty Listの可能性あり）
   * @param user_id, shelf_id, book_id
   */
  def searchByID(user_id : Long, shelf_id : Long, book_id: String)(implicit s: Session): List[MyBook] = {
      mybook.filter(row => (row.user_id === user_id) && (row.shelf_id === shelf_id) && (row.book_id === book_id)).list
  }


  /**
   * ユーザ・本棚ごと本一覧取得
   * @param word
   */
  def search(user_id : Long, shelf_id: Long)(implicit s: Session): List[MyBook] = {
    mybook.filter(row => (row.user_id === user_id) && (row.shelf_id === shelf_id)).list
  }
  
  /**
   * 本棚・本ごと　一覧取得
   * @param word
   */
  def searchByLocalbook(shelf_id: Long, book_id : String)(implicit s: Session): List[MyBook] = {
    mybook.filter(row => (row.book_id === book_id) && (row.shelf_id === shelf_id)).list
  }


  /**
   * 登録
   * @param user_id, shelf_id, book_id
   */
  def register(user_id : Long, shelf_id : Long, book_id: String)(implicit s: Session) {
    val newMybook =
      MyBook(user_id, shelf_id, book_id, OBSConstants.Unread, "", "", None, None, 0.toShort, 0.toShort) // TODO understanding, evaluation 未実装
    mybook.insert(newMybook)
  }

  /**
   * 更新
   * @param mybook_data
   */
  def update(mybook_data : MyBook)(implicit s: Session) {
    mybook.filter(row => (row.user_id === mybook_data.user_id) &&
        (row.shelf_id === mybook_data.shelf_id) && (row.book_id === mybook_data.book_id)).update(mybook_data)
  }

  /**
   * 削除
   * @param user_id, shelf_id
   */
  def remove(user_id : Long, shelf_id : Long)(implicit s: Session) {
    mybook.filter(row => (row.user_id === user_id) && (row.shelf_id === shelf_id)).delete
  }
}
