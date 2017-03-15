package models

object Constants {

  val UTF8Charset     = java.nio.charset.Charset.forName("UTF-8")

  val JSON_CLAZ       = "json_claz"

  val DOMAIN            = "domain"

  val STATE           = "state"
  val CREATE          = "create"
  val RESETPW         = "resetpassword"
  val DELETE          = "destroy"

  val CONTROL         = "control"
  val START           = "start"
  val STOP            = "stop"
  val REBOOT          = "restart"

  val SNAPSHOT        = "snapshot"    //category
  val SNAPSHOT_CREATE = "snapcreate"  //action
  val SNAPSHOT_REMOVE = "snapremove"  //action

  val BACKUP        = "backup"        //category
  val BACKUP_CREATE = "backupcreate"  //action
  val BACKUP_REMOVE = "backupremove"  //action

  val DISKS           = "disks"        //category
  val ATTACH_DISK     = "attachdisk"   //action
  val DETACH_DISK     = "detachdisk"   //action

  val RAWIMAGES       = "marketplaces.rawimages"   //category
  val CREATE_RAWIMAGE = "rawimage.iso.create" //action
  val DELETE_RAWIMAGE = "rawimage.iso.delete"   //action

  val LOCALSITE_MARKETPLACES    = "localsite.marketplaces"   //category
  val INITIALIZE_MARKETPLACE    = "marketplaces.initialize"   //action
  val CREATE_MARKETPLACE        = "marketplace.create"   //action
  val DELETE_MARKETPLACE        = "marketplace.delete"   //action

  val BIND            = "bind"
  val BUILD           = "build"

  val CATTYPE_DOCKER    = "container"
  val CATTYPE_TORPEDO   = "torpedo"
  val CATTYPE_CUSTOMAPP = "app"

  val CATTYPE_MARKETPLACES      = "marketplaces"

  val CATTYPE_SERVICE   = "service"
  val BITNAMI           = "bitnami"
  val ANALYTICS         = "analytics"
  val COLLABORATION     = "collaboration"

 //I don't like this, but in 2.0 we can do better using level based state machine as opposed to edge based,
  val STATUS_DESTROYED                    = List("destroyed", "preerror","posterror", "parked", "initializing")

  val REPORT_SALES                        = "sales"
  val REPORT_LAUNCHES                     = "launches"
  val REPORT_BACKUPS                      = "backups"
  val REPORT_SNAPSHOTS                    = "snapshots"

  val REPORT_USRDOT                       = "userdot"
  val REPORT_LANDOT                       = "launchdot"
  val REPORT_NOOP                         = "noop"

  val REPORT_FILTER_VM                    = List(CATTYPE_TORPEDO)
  val REPORT_DEAD                         = List("destroying", "destroyed", "posterror")
  val REPORT_NOTINITED                    = List("preerror", "initializing")
  val REPORT_FILTER_BITNAMI_PREPACKAGED   = List(BITNAMI)
  val REPORT_FILTER_VERTICE_PREPACKAGED   = List(COLLABORATION, ANALYTICS)
  val REPORT_FILTER_CUSTOMAPPS            = List(CATTYPE_CUSTOMAPP)
  val REPORT_FILTER_SERVICES              = List(CATTYPE_SERVICE)
  val REPORT_FILTER_CONTAINERS            = List(CATTYPE_DOCKER)

  val REPORT_CATEGORYMAP                  = Map(
    "all"         -> (REPORT_FILTER_VM  ++ REPORT_FILTER_CUSTOMAPPS ++ REPORT_FILTER_BITNAMI_PREPACKAGED ++  REPORT_FILTER_VERTICE_PREPACKAGED
              ++ REPORT_FILTER_SERVICES ++ REPORT_FILTER_CONTAINERS),
    "service"     -> REPORT_FILTER_SERVICES,
    "vm"          -> REPORT_FILTER_VM,
    "application" -> (REPORT_FILTER_CUSTOMAPPS ++ REPORT_FILTER_BITNAMI_PREPACKAGED ++  REPORT_FILTER_VERTICE_PREPACKAGED),
    "container"   -> REPORT_FILTER_CONTAINERS)

  val ACCOUNTCLAZ                     = "Megam::Account"
  val ADMINUSERSCLAZ                  = "Megam::Account"
  val ADMINUSERSCOLLECTIONCLAZ        = "Megam::AccountCollection"
  val ADMINLICENSECLAZ                = "Megam::License"
  val ADMINAUDITLOGCLAZ               =  "Megam::AuditLog"

  val ASSEMBLIESCLAZ                  = "Megam::Assemblies"
  val ASSEMBLIESCOLLECTIONCLAZ        = "Megam::AssembliesCollection"
  val ASSEMBLYCLAZ                    = "Megam::Assembly"
  val ASSEMBLYCOLLECTIONCLAZ          = "Megam::AssemblyCollection"
  val ADDONSCLAZ                      = "Megam::Addons"
  val ADDONSCOLLECTIONCLAZ            = "Megam::AddonsCollection"
  val AUTHCLAZ                        = "Megam::Auth"
  val AVAILABLEUNITSCLAZ              = "Megam::Availableunits"
  val AVAILABLEUNITSCOLLECTIONCLAZ    = "Megam::AvailableunitsCollection"
  val BALANCESCLAZ                    = "Megam::Balances"
  val BALANCESCOLLECTIONCLAZ          = "Megam::BalancesCollection"
  val CREDITSCLAZ                      = "Megam::Credits"
  val CREDITSCOLLECTIONCLAZ            = "Megam::CreditsCollection"
  val BILLEDHISTORIESCLAZ             = "Megam::Billedhistories"
  val BILLEDHISTORIESCOLLECTIONCLAZ   = "Megam::BilledhistoriesCollection"
  val BILLINGTRANSCATIONSCLAZ         = "Megam::BillingTransactions"
  val BILLINGTRANSCATIONSCOLLECTIONCLAZ   = "Megam::BillingTransactionsCollection"
  val BILLINGSCLAZ                    = "Megam::Billings"
  val BILLINGSCOLLECTIONCLAZ          = "Megam::BillingsCollection"
  val COMPONENTSCLAZ                  = "Megam::Components"
  val COMPONENTSCOLLECTIONCLAZ        = "Megam::ComponentsCollection"
  val CREDITHISTORIESCLAZ             = "Megam::Credithistories"
  val CREDITHISTORIESCOLLECTIONCLAZ   = "Megam::CredithistoriesCollection"
  val CSARCLAZ                        = "Megam::CSAR"
  val CSARCOLLECTIONCLAZ              = "Megam::CSARCollection"
  val DOMAINCLAZ                      = "Megam::Domains"
  val DOMAINCOLLECTIONCLAZ            = "Megam::DomainsCollection"
  val DISCOUNTSCLAZ                   = "Megam::Discounts"
  val DISCOUNTSCOLLECTIONCLAZ         = "Megam::DiscountsCollection"
  val EVENTSVMCLAZ                    = "Megam::EventsVm"
  val EVENTSVMCOLLECTIONCLAZ          = "Megam::EventsVmCollection"
  val EVENTSCONTAINERCLAZ             = "Megam::EventsContainer"
  val EVENTSCONTAINERCOLLECTIONCLAZ   = "Megam::EventsContainerCollection"
  val EVENTSBILLINGCLAZ               = "Megam::EventsBilling"
  val EVENTSBILLINGCOLLECTIONCLAZ     = "Megam::EventsBillingCollection"
  val EVENTSSKEWSCLAZ                 = "Megam::Eventsskews"
  val EVENTSSKEWSCOLLECTIONCLAZ       = "Megam::EventsSkewsCollection"
  val EVENTSSTORAGECLAZ               = "Megam::EventsStorage"
  val EVENTSSTORAGECOLLECTIONCLAZ     = "Megam::EventsStorageCollection"
  val ERRORCLAZ                       = "Megam::Error"
  val INVOICESCOLLECTIONCLAZ          = "Megam::InvoicesCollection"
  val INVOICESCLAZ                    = "Megam::Invoices"
  val MARKETPLACECLAZ                 = "Megam::MarketPlace"
  val MARKETPLACECOLLECTIONCLAZ       = "Megam::MarketPlaceCollection"
  val ORGANIZATIONCLAZ                = "Megam::Organizations"
  val ORGANIZATIONSCOLLECTIONCLAZ     = "Megam::OrganizationsCollection"
  val REQUESTCLAZ                     = "Megam::Request"
  val REQUESTCOLLECTIONCLAZ           = "Megam::RequestCollection"
  val SENSORSCLAZ                     = "Megam::Sensors"
  val SENSORSCOLLECTIONCLAZ           = "Megam::SensorsCollection"
  val SSHKEYCLAZ                      = "Megam::SshKey"
  val SSHKEYCOLLECTIONCLAZ            = "Megam::SshKeyCollection"
  val SNAPSHOTSCLAZ                   = "Megam::Snapshots"
  val SNAPSHOTSCOLLECTIONCLAZ         = "Megam::SnapshotsCollection"
  val BACKUPSCLAZ                     = "Megam::Backups"
  val BACKUPSCOLLECTIONCLAZ           = "Megam::BackupsCollection"
  val DISKSCLAZ                       = "Megam::Disks"
  val DISKSCOLLECTIONCLAZ             = "Megam::DisksCollection"
  val RAWIMAGESCLAZ                  = "Megam::Rawimages"
  val RAWIMAGESCOLLECTIONCLAZ         = "Megam::RawimagesCollection"

  val SUBSCRIPTIONSCLAZ               = "Megam::Subscriptions"
  val SUBSCRIPTIONSCOLLECTIONCLAZ     = "Megam::SubscriptionsCollection"
  val MEGAM_PROMOSCLAZ                = "Megam::Promos"
  val REPORTSCOLLECTIONCLAZ           = "Megam::ReportsCollection"
  val REPORTSCLAZ                     = "Megam::Reports"
  val QUOTASCOLLECTIONCLAZ            = "Megam::QuotasCollection"
  val QUOTASCLAZ                      = "Megam::Quotas"

  val EXTERNALOBJECTSCOLLECTIONCLAZ   = "Megam::ExternalobjectsCollection"
  val EXTERNALOBJECTSCLAZ             = "Megam::Externalobjects"
}
