package models.db

/**
 * @author Emi Yoshihara
 */


import play.api.db.slick.Config.driver.simple._
import java.sql.Timestamp

import common.utils._

/**
 * DTOの定義
 */
case class User(user_id: Option[Long], user_name: String,  mail: String, password: String, status: Short, reg_date: Timestamp)

/**
 *  テーブルスキーマの定義
 */
class UserTable(tag: Tag) extends Table[User](tag, "User") {
  def user_id = column[Long]("user_id", O.PrimaryKey, O.AutoInc)
  def user_name = column[String]("user_name")
  def mail = column[String]("mail")
  def password = column[String]("password")
  def status = column[Short]("status")
  def reg_date = column[Timestamp]("reg_date")

  def * = (user_id.?, user_name, mail, password, status, reg_date) <> (User.tupled, User.unapply)
  
}

/**
 * DAOの定義
 */
object UserDAO {
  lazy val user = TableQuery[UserTable]
  
  /**
   * ID検索
   * @param id
   */
  def searchByID(id: Long)(implicit s: Session): User = {
      user.filter(_.user_id === id ).first
  }
  
  /**
   * メールアドレス検索
   * @param mail
   */
  def searchByAddress(mail: String)(implicit s: Session): List[User] = {
      user.filter(_.mail === mail ).list
  }
  

  /**
   * 登録 - Auto Increment
   * @param user
   * @return user_id
   */
  def register(user_data : User)(implicit s: Session): Long = {
    // current_date
    val current_date = new Timestamp(System.currentTimeMillis())
    val new_user = User(None, user_data.user_name, user_data.mail, user_data.password, user_data.status, current_date)
     
    val new_id = (user returning user.map(_.user_id)) += new_user

    new_id
  }
  
  /**
   * パスワード登録
   */
  def setPassword(user_id: Long, password: String)(implicit s: Session): Long = {
    user.filter(_.user_id === user_id).map(_.password).update(TextConv.getSetPassword(password, searchByID(user_id)))
  }

}
