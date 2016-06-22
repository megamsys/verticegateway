
/*
** Copyright [2013-2016] [Megam Systems]
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
** http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
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
  val AUTHCLAZ                        = "Megam::Auth"
  val AVAILABLEUNITSCLAZ              = "Megam::Availableunits"
  val AVAILABLEUNITSCOLLECTIONCLAZ    = "Megam::AvailableunitsCollection"
  val BALANCESCLAZ                    = "Megam::Balances"
  val BALANCESCOLLECTIONCLAZ          = "Megam::BalancesCollection"
  val BILLEDHISTORIESCLAZ             = "Megam::Billedhistories"
  val BILLEDHISTORIESCOLLECTIONCLAZ   = "Megam::BilledhistoriesCollection"
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
  val EVENTSCLAZ                      = "Megam::Events"
  val EVENTSCOLLECTIONCLAZ            = "Megam::EventsCollection"
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
  val SUBSCRIPTIONSCLAZ               = "Megam::Subscriptions"
  val SUBSCRIPTIONSCOLLECTIONCLAZ     = "Megam::SubscriptionsCollection"
  val MEGAM_PROMOSCLAZ                = "Megam::Promos"


}
