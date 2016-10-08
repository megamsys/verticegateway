package controllers.stack

sealed trait Role

object Role {

  type Authority = Role

  case object Administrator extends Role
  case object RegularUser extends Role

  def valueOf(value: String): Role = value match {
    case "Administrator" => Administrator
    case "RegularUser"   => RegularUser
    case _ => throw new IllegalArgumentException()
  }

}
