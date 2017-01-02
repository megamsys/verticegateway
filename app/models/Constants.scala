package models

object Constants {

  val UTF8Charset     = java.nio.charset.Charset.forName("UTF-8")

  val JSON_CLAZ       = "json_claz"

  val DOMAIN            = "domain"

  val STATE           = "state"
  val CREATE          = "create"
  val DELETE          = "destroy"

  val CONTROL         = "control"
  val START           = "start"
  val STOP            = "stop"
  val REBOOT          = "restart"

  val SNAPSHOT        = "snapshot"    //category
  val SNAPSHOT_CREATE = "snapcreate"  //action
  val SNAPSHOT_REMOVE = "snapremove"  //action

  val DISKS           = "disks"        //category
  val ATTACH_DISK     = "attachdisk"   //action
  val DETACH_DISK     = "detachdisk"   //action

  val BIND            = "bind"
  val BUILD           = "build"

  val CATTYPE_DOCKER    = "microservices"
  val CATTYPE_TORPEDO   = "torpedo"
  val CATTYPE_CUSTOMAPP = "app"
  val CATTYPE_SERVICE   = "service"
  val BITNAMI           = "bitnami"
  val ANALYTICS         = "analytics"
  val COLLABORATION     = "collaboration"

  val REPORT_SALES                        = "sales"
  val REPORT_USRDOT                       = "userdot"
  val REPORT_LANDOT                       = "launchdot"
  val REPORT_LAUNCHES                     = "launches"
  val REPORT_NOOP                         = "noop"
  val REPORT_FILTER_VM                    = List(CATTYPE_TORPEDO)
  val REPORT_DEAD                         = List("destroying", "destroyed", "posterror")
  val REPORT_NOTINITED                    = List("preerror", "initializing")
  val REPORT_FILTER_BITNAMI_PREPACKAGED   = List(BITNAMI)
  val REPORT_FILTER_VERTICE_PREPACKAGED   = List(COLLABORATION, ANALYTICS)
  val REPORT_FILTER_CUSTOMAPPS            = List(CATTYPE_CUSTOMAPP)
  val REPORT_FILTER_SERVICES              = List(CATTYPE_SERVICE)
  val REPORT_FILTER_CONTAINERS            = List(CATTYPE_DOCKER)


  val ACCOUNTCLAZ                     = "Megam::Account"
  val ADMINUSERSCLAZ                  = "Megam::Account"
  val ADMINUSERSCOLLECTIONCLAZ        = "Megam::AccountCollection"
  val ADMINLICENSECLAZ                = "Megam::License"

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
  val DISKSCLAZ                       = "Megam::Disks"
  val DISKSCOLLECTIONCLAZ             = "Megam::DisksCollection"
  val SUBSCRIPTIONSCLAZ               = "Megam::Subscriptions"
  val SUBSCRIPTIONSCOLLECTIONCLAZ     = "Megam::SubscriptionsCollection"
  val MEGAM_PROMOSCLAZ                = "Megam::Promos"
  val REPORTSCOLLECTIONCLAZ           = "Megam::ReportsCollection"
  val REPORTSCLAZ                     = "Megam::Reports"
  val QUOTASCOLLECTIONCLAZ            = "Megam::QuotasCollection"
  val QUOTASCLAZ                      = "Megam::Quotas"
}
