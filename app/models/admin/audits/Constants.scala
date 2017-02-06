package models.admin.audits

object Constants {

  val KIND                     = "kind"
  val LOG_USER_DELETE          = "user.deleted"
  val LOG_ASSEMBLY_DELETE      = "assembly.deleted"

  val STATUS                   = "status"
  val DESTROYED_ALL            = "deployed,team,telemetry,identity"
  val DESTROYED_ASSEMBLY       = "deployed,telemetry"

  val SUCCESS                  = "success"
}
