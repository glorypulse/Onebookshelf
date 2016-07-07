package models

/**
 * @author Emi Yoshihara
 */

import play.api.db.DB
import play.api.Play.current
import play.api.db.slick.Config.driver.simple.Database

object DBAccess {
  /** DBコネクション */
  val dbconn = Database.forDataSource(DB.getDataSource())
}