package controllers.form

import play.api.data.validation._

object OBSConstraint {
  
  def nonEmpty : Constraint[String] = Constraint[String]("constraint.required"){ str =>
    str match {
      case "" => Invalid(ValidationError("error.required"))
      case _  => Valid
    }
  }

}