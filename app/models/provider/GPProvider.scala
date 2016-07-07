package models.provider

/**
 * @author Emi Yoshihara
 */

import play.api.db.slick._
import models.db._
import play.api.Play.current

object GPProvider {
  /*
   * ジャンル、場所の(id, name)のListを返す
   */
  def getGPMap(shelf_id: Long): (Seq[(String, String)], Seq[(String, String)])  = {
    DB.withSession{ implicit session =>
      val genre_map = LocalGenreDAO.searchByShelf(shelf_id)
      val place_map = LocalPlaceDAO.searchByShelf(shelf_id)
      (genre_map, place_map)
    }
  }
  
  /*
   * ジャンル、場所の名称から、genre_id -> 名称に該当するジャンルID, place_id -> 名称に該当する場所IDのマップを返す
   */
  def getGPIDMap(gp_name: (String, String), shelf_id: Long): Map[String, String] = {
    val genre_name = gp_name._1
    val place_name = gp_name._2
    
    DB.withSession { implicit session =>
      Map("genre_id" -> LocalGenreDAO.search(genre_name, shelf_id).head.genre_id,
          "place_id" -> LocalPlaceDAO.search(place_name, shelf_id).head.place_id)
    }
  } 
  
}