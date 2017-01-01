package controllers.shelf

/**
 * @author Emi Yoshihara
 */

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

import play.api.db.slick._
import models.db._
import play.api.Play.current

import controllers.form.{ShelfReg, ShelfManage, ShelfManageForm, PlaceAdd, PlaceEdit, PlaceDel, GenreAdd, GenreEdit, GenreDel}
import models.provider.GPProvider
import common._


object ShelfController extends Controller with controllers.Secured{
  
  /**
   *  本棚登録結果表示
   *  @param id
   */
  def searchByID(id: Long) = withAuth { user_id => implicit rs =>
    DB.withSession { implicit session => 
      Ok(views.html.shelfRegResult(id.toString(), BookshelfDAO.searchByID(id)))
    }
  }
 
 
  /**
   *  本棚登録　…　フォームの定義
   */
  val shelfRegForm = Form(
    mapping(
      "shelf_name" -> text(maxLength = 140),
      "privacy" ->  number
    )(ShelfReg.apply)(ShelfReg.unapply)
  )
  
  /**
   *  本棚登録　…　フォーム表示
   */
  def showRegisterForm() = withAuth { user_id => request =>
    user_id match {
      case 1 => Ok(views.html.shelfRegForm(shelfRegForm))
      case _ => {
        OBSCache.setGlobalError(user_id, "error.noauth")
        Redirect(controllers.search.routes.BookController.search(""))
      }
    }
    
  }

  /**
   *  本棚登録　…　実行
   */
  def register() = withAuth { user_id => implicit rs =>
    DB.withSession { implicit session =>
      user_id match {
        case 1 => shelfRegForm.bindFromRequest.fold(
          errors => BadRequest(views.html.shelfRegForm(errors)),
          shelfReg => {
            val shelf = new Bookshelf(None, shelfReg.shelf_name, shelfReg.privacy.toShort)
            val new_id = BookshelfDAO.register(shelf)
                    
            Redirect(controllers.shelf.routes.ShelfController.searchByID(new_id))
          }
        )
        case _ => {
          OBSCache.setGlobalError(user_id, "error.noauth")
          Redirect(controllers.search.routes.BookController.search(""))
        }
      }
      
    }
  }
  
  
  /* ************ 本棚管理 ************ */
  /**
   *  本棚更新、場所追加・更新・削除、ジャンル追加・更新・削除　…　フォームの定義
   */
  val shelfForm = ShelfManageForm.shelfForm
  val (placeAddForm, placeEditForm, placeDelForm) = (ShelfManageForm.placeAddForm, ShelfManageForm.placeEditForm, ShelfManageForm.placeDelForm)
  val (genreAddForm, genreEditForm, genreDelForm) = (ShelfManageForm.genreAddForm, ShelfManageForm.genreEditForm, ShelfManageForm.genreDelForm)
  val placeForms = (placeAddForm, placeEditForm, placeDelForm)
  val genreForms = (genreAddForm, genreEditForm, genreDelForm)
  

  
  /**
   *  本棚更新　…　フォーム表示
   */
  def showUpdateForm() = withAuth { user_id => implicit request =>
    
    // デフォルト本棚取得
    DB.withSession { implicit session => 
      val myshelf = MyBookshelfDAO.searchDef(user_id, OBSConstants.DefaultShelf)
      myshelf.auth_flag match {
        case OBSConstants.Owner => Redirect(controllers.shelf.routes.ShelfController.showShelfManageForm(myshelf.shelf_id))
        case _ => {
          // デフォルト本棚に編集権限がない場合、オーナーである本棚を探す
          OBSCache.setGlobalError(user_id, OBSMessage.NoEditShelf)
          val ownershelf = MyBookshelfDAO.search(user_id).filter { x => x.auth_flag == OBSConstants.Owner }
          ownershelf.size match {
            case 0 => Redirect(controllers.search.routes.BookController.search(""))          
            case _ => Redirect(controllers.shelf.routes.ShelfController.showShelfManageForm(ownershelf.head.shelf_id))
          }
          
        }
      }
      
    }
  }
  
  def showShelfManageForm(shelf_id: Long) = withOwnerAuth(shelf_id) ({ user_id =>
      redirectNoauth(user_id) 
    }, { user_id => implicit request =>
    DB.withSession { implicit session =>
      
      val gp_map = GPProvider.getGPMap(shelf_id)
      
      val shelfManage = getShelfManageCase(user_id, shelf_id)
      
      
       // メッセージ・エラー情報の取得
      val shelfForm_withMessage = OBSCache.getGlobalErrorWithForm[controllers.form.ShelfManage](user_id, shelfForm).fill(shelfManage)
      OBSCache.removeGlobalError(user_id)
      
      Ok(views.html.shelfManage(shelfManage, gp_map, shelfForm_withMessage, placeForms, genreForms))
      }
    })

 
  /**
   *  本棚更新　…　実行
   */
  def update(shelf_id: Long) = withOwnerAuth(shelf_id)({ user_id =>
    redirectNoauth(user_id) 
  }, { user_id => implicit request =>
    
    shelfForm.bindFromRequest.fold(
      errors => {
        val gp_map = GPProvider.getGPMap(shelf_id)
        val shelfManage = getShelfManageCase(user_id, shelf_id)
        BadRequest(views.html.shelfManage(shelfManage, gp_map, errors.withGlobalError("error.normal"), placeForms, genreForms))
      },
      
      shelf => DB.withSession { implicit session =>
        val myshelf = MyBookshelfDAO.searchDef(user_id, OBSConstants.DefaultShelf)
        val new_shelf = new Bookshelf(Some(shelf.shelf_id), shelf.shelf_name, shelf.privacy.toShort)
        val new_myshelf = new MyBookshelf(user_id, shelf.shelf_id, myshelf.auth_flag, shelf.def_flag.toShort)
        
        BookshelfDAO.update(new_shelf)
        MyBookshelfDAO.update(new_myshelf)
        
        Redirect(controllers.shelf.routes.ShelfController.showShelfManageForm(shelf_id))
      })
        
  })
  
  /**
   * 場所追加　…　実行
   */
  def registerPlace(shelf_id: Long) = withOwnerAuth(shelf_id)({  user_id =>
    redirectNoauth(user_id)
  }, { user_id => implicit rs =>
    
    placeAddForm.bindFromRequest.fold(
      errors => {
        val gp_map = GPProvider.getGPMap(shelf_id)
        val shelfManage = getShelfManageCase(user_id, shelf_id)
        val placeForms = (errors.withGlobalError("error.normal"), placeEditForm, placeDelForm)
        BadRequest(views.html.shelfManage(shelfManage, gp_map, shelfForm, placeForms, genreForms))
      },
      place_data => DB.withSession { implicit session =>
          val place_name = place_data.place_name
          LocalPlaceDAO.register(place_name, shelf_id)
          Redirect(controllers.shelf.routes.ShelfController.showShelfManageForm(shelf_id))
      }) 
  })
  
  /**
   * 場所更新
   */
  def updatePlace(shelf_id: Long) = withOwnerAuth(shelf_id)({ user_id =>
    redirectNoauth(user_id) 
  }, { user_id => implicit rs =>
    placeEditForm.bindFromRequest.fold(
      errors => {
        val gp_map = GPProvider.getGPMap(shelf_id)
        val shelfManage = getShelfManageCase(user_id, shelf_id)
        val placeForms = (placeAddForm, errors.withGlobalError("error.normal"), placeDelForm)
        BadRequest(views.html.shelfManage(shelfManage, gp_map, shelfForm, placeForms, genreForms))
      },
      place_data => DB.withSession { implicit session =>
        val (place_id, place_name) = (place_data.place_id, place_data.place_name)
        LocalPlaceDAO.updateName(place_id, place_name, shelf_id)
        Redirect(controllers.shelf.routes.ShelfController.showShelfManageForm(shelf_id))
      })
  })
  
  /**
   * 場所削除　…　実行
   */
  def removePlace(shelf_id: Long) = withOwnerAuth(shelf_id)({ user_id =>
    redirectNoauth(user_id) 
  }, { user_id => implicit rs =>
    placeDelForm.bindFromRequest.fold(
      errors => {
        val gp_map = GPProvider.getGPMap(shelf_id)
        val shelfManage = getShelfManageCase(user_id, shelf_id)
        val placeForms = (placeAddForm, placeEditForm , errors.withGlobalError("error.normal"))
        BadRequest(views.html.shelfManage(shelfManage, gp_map, shelfForm, placeForms, genreForms))
      },
      place_data => DB.withSession { implicit session =>
        val place_id = place_data.place_id
        place_id match {
          case OBSConstants.DefaultGPID => { // 「未分類」は削除できない
            OBSCache.setGlobalError(user_id, "error.abnormal")
            Redirect(controllers.shelf.routes.ShelfController.showShelfManageForm(shelf_id))
          }
          case _                        => {
            LocalPlaceDAO.remove(place_id, shelf_id)
            Redirect(controllers.shelf.routes.ShelfController.showShelfManageForm(shelf_id)) 
          }
        }
      })
  })

   /**
   * ジャンル追加
   */
  def registerGenre(shelf_id: Long) = withOwnerAuth(shelf_id)({ user_id =>
    redirectNoauth(user_id) 
  }, { user_id => implicit rs =>
    genreAddForm.bindFromRequest.fold(
        errors => {
        val gp_map = GPProvider.getGPMap(shelf_id)
        val shelfManage = getShelfManageCase(user_id, shelf_id)
        val genreForms = (errors.withGlobalError("error.normal"), genreEditForm , genreDelForm)
        BadRequest(views.html.shelfManage(shelfManage, gp_map, shelfForm, placeForms, genreForms))
        },
        genre_data => DB.withSession { implicit session =>
          val genre_name = genre_data.genre_name
          LocalGenreDAO.register(genre_name, shelf_id)
          Redirect(controllers.shelf.routes.ShelfController.showShelfManageForm(shelf_id)) 
      })
  })
  
  /**
   * ジャンル更新
   */
  def updateGenre(shelf_id: Long) = withOwnerAuth(shelf_id)({ user_id =>
    redirectNoauth(user_id)
  }, { user_id => implicit rs =>
    genreEditForm.bindFromRequest.fold(
        errors => {
        val gp_map = GPProvider.getGPMap(shelf_id)
        val shelfManage = getShelfManageCase(user_id, shelf_id)
        val genreForms = (genreAddForm, errors.withGlobalError("error.normal") , genreDelForm)
        BadRequest(views.html.shelfManage(shelfManage, gp_map, shelfForm, placeForms, genreForms))
        }, {
          genre_data => DB.withSession { implicit session =>
            val (genre_id, genre_name) = (genre_data.genre_id, genre_data.genre_name)
            LocalGenreDAO.updateName(genre_id, genre_name, shelf_id)
            Redirect(controllers.shelf.routes.ShelfController.showShelfManageForm(shelf_id))
            }
        }
        )
    
  })
  
  /**
   * ジャンル削除
   */
  def removeGenre(shelf_id: Long) = withOwnerAuth(shelf_id)({ user_id =>
    redirectNoauth(user_id) 
  }, { user_id => implicit rs =>
    genreDelForm.bindFromRequest.fold(
        errors => {
          val gp_map = GPProvider.getGPMap(shelf_id)
          val shelfManage = getShelfManageCase(user_id, shelf_id)
          val genreForms = (genreAddForm, genreEditForm , errors.withGlobalError("error.normal"))
          BadRequest(views.html.shelfManage(shelfManage, gp_map, shelfForm, placeForms, genreForms))
        },
        genre_data => DB.withSession { implicit session =>
            val genre_id = genre_data.genre_id
            genre_id match {
              case OBSConstants.DefaultGPID => { // 「未分類」は削除できない
                OBSCache.setGlobalError(user_id, "error.abnormal")
                Redirect(controllers.shelf.routes.ShelfController.showShelfManageForm(shelf_id))
              }
              case _                        => LocalGenreDAO.remove(genre_id, shelf_id) 
            }
            Redirect(controllers.shelf.routes.ShelfController.showShelfManageForm(shelf_id)) 
      })
  })

  // 本棚が存在するuser_id, shelf_idの組み合わせであること
  private def getShelfManageCase(user_id: Long, shelf_id: Long): ShelfManage = DB.withSession{ implicit session =>
      val myshelf = MyBookshelfDAO.searchByID(user_id, shelf_id).get
      val shelf_info = BookshelfDAO.searchByID(shelf_id)
      ShelfManage(shelf_id, shelf_info.shelf_name, myshelf.def_flag, shelf_info.privacy)
  }
  
  private def redirectNoauth(user_id: Long): Result = {
    // 編集権限のない本棚の指定があった場合は、自分のデフォルト本棚に変更
    OBSCache.setGlobalError(user_id, OBSMessage.NoEditShelf)
    Redirect(controllers.shelf.routes.ShelfController.showUpdateForm())  
  }

}