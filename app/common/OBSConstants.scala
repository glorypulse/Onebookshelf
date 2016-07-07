package common

object OBSConstants {
  
  // status / User
  val NormalStatus = 0.toShort
  val AdminStatus  = 9.toShort
  
  // privacy / Bookshelf
  val PrivateShelf     = 0.toShort
  val SelectableShelf  = 3.toShort
  val PublicShelf      = 8.toShort
  
  // auth_flag / MyBookshelf
  val Owner = 0.toShort
  val Editor = 3.toShort
  val Reader = 6.toShort
  
  // def_flag / MyBookshelf
  val NotDefaultShelf = 0.toShort
  val DefaultShelf    = 1.toShort
  
  // read_flag / MyBook
  val Unread = 0.toShort
  val Reading = 1.toShort
  val Read = 2.toShort
  
  /* ** その他の定数 ** */
  
  // Amazon API Max page
  val AmazonMaxPage = 4
  
  // Default Set / Bookshelf
  val DefaultSName = "の本棚"
  
  // Default Set / LocalPlace, LocalGenre
  val DefaultGPID = "0" * 32
  val DefaultGPName = "未分類"
  val DefaultGPNum = 0

}