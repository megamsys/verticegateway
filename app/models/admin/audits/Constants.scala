package models.admin.audits

object Constants {

  val KIND                     = "kind"
  val ASSEMBLY_ID              = "assembly_id"
  val ASSEMBLIES_ID            = "assemblies_id"
  val ACCOUNT_ID               = "email"

  val LOG_USER_DELETE          = "user.deleted"
  val LOG_ASSEMBLY_DELETE      = "assembly.deleted"

  val DESTROYED_ALL            = "deployed,team,telemetry,identity"
  val DESTROYED_ASSEMBLY       = "deployed,telemetry"

  val STATUS                   = "status"
  val SUCCESS                  = "success"
}
