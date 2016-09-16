package models

object Constants {

  val UTF8Charset = java.nio.charset.Charset.forName("UTF-8")

  val JSON_CLAZ = "json_claz"

  val STATE = "state"
  val CREATE = "create"
  val DELETE = "destroy"

  val CONTROL = "control"
  val START = "start"
  val STOP = "stop"
  val SNAPSHOT = "disksaveas"
  val DISK = "diskattach"
  val SNAPSTATE = "snapshot"
  val DISKSTATE = "disk"
  val BIND = "bind"
  val BUILD = "build"
  val REBOOT = "restart"
  val OPERTATIONS = "operations"
  val UPGRADE = "upgrade"

  val CATTYPE_DOCKER = "microservices"
  val CATTYPE_TORPEDO = "torpedo"
  val DOMAIN = "domain"

  //index used by accounts bucket
  val idxAccountsId = "AccountsId"
  //index used by marketplaces bucket
  val idxMarketplaceName = "MarketplaceName"
  //index used by domains bucket
  val idxDomainName = "DomainName"
  //index used by requests bucket
  val idxAssemblyId = "AssemblyId"
  //index used by assemblies bucket a set of (accountsid, orgid)
  val idxTeamId = "TeamId"

  val ACCOUNTCLAZ                     = "Megam::Account"
  val ASSEMBLIESCLAZ                  = "Megam::Assemblies"
  val ASSEMBLIESCOLLECTIONCLAZ        = "Megam::AssembliesCollection"
  val ASSEMBLYCLAZ                    = "Megam::Assembly"
  val ASSEMBLYCOLLECTIONCLAZ          = "Megam::AssemblyCollection"
  val ADDONSCLAZ                     = "Megam::Addons"
  val ADDONSCOLLECTIONCLAZ           = "Megam::AddonsCollection"
  val AUTHCLAZ                        = "Megam::Auth"
  val AVAILABLEUNITSCLAZ              = "Megam::Availableunits"
  val AVAILABLEUNITSCOLLECTIONCLAZ    = "Megam::AvailableunitsCollection"
  val BALANCESCLAZ                    = "Megam::Balances"
  val BALANCESCOLLECTIONCLAZ          = "Megam::BalancesCollection"
  val BILLEDHISTORIESCLAZ             = "Megam::Billedhistories"
  val BILLEDHISTORIESCOLLECTIONCLAZ   = "Megam::BilledhistoriesCollection"
  val BILLINGTRANSCATIONSCLAZ             = "Megam::Billingtranscations"
  val BILLINGTRANSCATIONSCOLLECTIONCLAZ   = "Megam::BillingtranscationsCollection"
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
  val EVENTSVMCLAZ                      = "Megam::EventsVm"
  val EVENTSVMCOLLECTIONCLAZ            = "Megam::EventsVmCollection"
  val EVENTSCONTAINERCLAZ               = "Megam::EventsContainer"
  val EVENTSCONTAINERCOLLECTIONCLAZ     = "Megam::EventsContainerCollection"
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
  val DISKSCLAZ                        = "Megam::Disks"
  val DISKSCOLLECTIONCLAZ              = "Megam::DisksCollection"
  val SUBSCRIPTIONSCLAZ               = "Megam::Subscriptions"
  val SUBSCRIPTIONSCOLLECTIONCLAZ     = "Megam::SubscriptionsCollection"
  val MEGAM_PROMOSCLAZ                = "Megam::Promos"


}
