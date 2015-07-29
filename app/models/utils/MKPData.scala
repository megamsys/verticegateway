package models.utils

import controllers.Constants
import models.{MarketPlaceInput, MarketPlacePlans, MarketPlacePlan, MarketPlaceCatalog}
/**
-- name: 1-Ubuntu
   catalog:
           category: 1-Torpedo
           image: ubuntu.png
           description: Ubuntu Server
   plan:
           price: 0 [0$ or charges per month ]
           description: "Ubuntu 12.04 LTS (Precise Pangolin) is the blah..blah.
           plantype:
                  sambar: sambar means megam leverages community stuff
                  idli  : idli   means megam's as service like scaling, dbaas etc.
                  dosai : dosai  means somebody else service that can be consumed. (eg: a dns, datadog, newrelic, loggly )
           version: 14.04
           source:
                  OTHERS : link to the
                  BYOC   : https://github.com/megamsys/nilavu.git
           os: ubuntu [runs on any operating system or container]
  cattype: TORPEDO
  predef : ubuntu
  status :
          ACTIVE
          SOON       - coming soon.
          TERMINATED - the client filters dead weight.
**/
object MKPData {

    private val ACTIVE     = "ACTIVE"
    private val TERMINATED = "TERMINATED"

    private val APP     = "APP"
    private val SERVICE = "SERVICE"
    private val TORPEDO     = "TORPEDO"
    private val MICROSERVICES = "MICROSERVICES"

    private val CATEGORY_TORPEDO    = "1-Torpedo"
    private val CATEGORY_BYOC       = "2-Bring Your Own Code"
    private val CATEGORY_APPBOILERS = "3-App Boilers"
    private val CATEGORY_PLATFORM   = "4-Platform"
    private val CATEGORY_ANALYTICS  = "5-Analytics"
    private val CATEGORY_UNIKERNEL  = "6-Unikernel"

    private val CATIMAGE_PREFIX     = "https://s3-ap-southeast-1.amazonaws.com/megampub/images/marketplaces/"

    private val SAMBAR = "sambar"
    private val IDLI   = "idli"
    private val DOSAI  = "dosai"

    private val CATOS_UBUNTU = "ubuntu"
    private val FIVE = "5"


   private def CatImage(image_file: String): String = CATIMAGE_PREFIX + image_file
   private def Cat(image: String, category: String, summary: String, port: String): MarketPlaceCatalog = new MarketPlaceCatalog(CatImage(image), category,summary, port)
   private def Torpedo(image: String,  summary: String, port: String): MarketPlaceCatalog = Cat(image, CATEGORY_TORPEDO, summary, port)
   private def BYOC(image: String, summary: String, port: String): MarketPlaceCatalog = Cat(image, CATEGORY_BYOC, summary, port)
   private def Appb(image: String, summary: String, port: String): MarketPlaceCatalog = Cat(image, CATEGORY_APPBOILERS, summary, port)
   private def Plat(image: String, summary: String, port: String): MarketPlaceCatalog = Cat(image, CATEGORY_PLATFORM, summary, port)
   private def BigD(image: String, summary: String, port: String): MarketPlaceCatalog = Cat(image, CATEGORY_ANALYTICS, summary, port)

   private def Plan(price: String, desc: String, plantype: String, version: String, source:  String, os: String): MarketPlacePlan  = MarketPlacePlan(price, desc, plantype, version, source, os)
   //This can go to an autoupdatable yaml file. So when the server starts it will sync the new marketplace items.
   private val C1 =  ("1-Ubuntu", Torpedo("ubuntu.png", "Ubuntu", ""),
    List(Plan(FIVE, "Scale out with Ubuntu Server. The leading platform for scale-out computing, Ubuntu Server helps you make the most of your infrastructure.",
    SAMBAR, "14.04", "http://ubuntu.com", CATOS_UBUNTU)))

   private val C2 =  ("2-CoreOS", Torpedo("coreos.png", "CoreOS", ""),
   List(Plan(FIVE, "CoreOS provides no package manager as a way for the distribution of applications, requiring instead all applications to run inside their containers.",
     SAMBAR, "633.1.0", "http://coreos.com", "KVM")))

   private val C3 =  ("3-Debian", Torpedo("debian.png", "Debian", ""),
   List(Plan(FIVE, "Debian is a free operating system (OS) for your computer. Wheezy is the codename for 7.0 Debian.",
   SAMBAR, "7", "http://debian.com", "Debian wheezy"),
   Plan(FIVE, "Debian is a free operating system (OS) for your computer. Jessie is the codename for 8.0 Debian.",
   SAMBAR, "8", "http://debian.com", "Debian jessie")))

   private val C4 =  ("4-CentOS", Torpedo("centos.png", "CentOS", ""),
   List(Plan(FIVE, "CentOS is an Enterprise-class Linux Distribution derived from sources freely provided to the public by Red Hat.",
   SAMBAR, "7", "http://centos.org", "CentOS 7")))


   private val C5 =  ("5-Java",   BYOC("java.png", "Java Web starter", ""),
   List(Plan(FIVE, "Quickly get started with J2EE Spring framework app.",
   SAMBAR, Constants.VERSION, "https://github.com/megamsys/java-spring-petclinic.git", CATOS_UBUNTU)))


   private val C6 =  ("6-Rails",  BYOC("rails.png", "Rails App", ""),
   List(Plan(FIVE, "Quickly get started with rails 4.x app.",
   SAMBAR, Constants.VERSION, "https://github.com/megamsys/aryabhata.git", CATOS_UBUNTU)))

   private val C7 =  ("7-Play",   BYOC("play.png", "Play App", ""),
   List(Plan(FIVE, "Build robust RESTful API server using Scala.",
   SAMBAR, Constants.VERSION, "https://github.com/megamsys/modern-web-template.git", CATOS_UBUNTU)))

   private val C8 =  ("8-Nodejs", BYOC("nodejs.png", "Realtime App", ""),
   List(Plan(FIVE, "Build fast, scalable, and incredibly efficient realtime app in second.",
   SAMBAR, Constants.VERSION, "https://github.com/megamsys/etherpad-lite.git", CATOS_UBUNTU)))

   private val C9 =  ("9-Docker", Plat("docker.png", "Container", ""),
   List(Plan(FIVE, "Docker that automates the deployment of applications inside software containers.",
   SAMBAR, Constants.VERSION, "https://www.docker.com", "baremetal")))

   private val C10 = ("10-DockerBox", Plat("docker.png", "Launch a vm with docker", ""),
   List(Plan(FIVE, "Launch a vm with docker in detached model.",
   SAMBAR, "0.9.0", "https://www.docker.com", "vm")))

   private val C11 = ("11-PostgreSQL", Appb("postgres.png", "Object Relational DBMS", "5432"),
   List(Plan(FIVE, "PostgreSQL is a powerful, open source object-relational database system.",
   SAMBAR, "9.3", "https://postgresql.org", CATOS_UBUNTU)))

   private val C12 = ("12-Riak", Appb("riak.png", "Scalable Distributed Database", ""),
   List(Plan(FIVE, "Riak is a distributed database designed to deliver maximum data availability by distributing data across multiple servers.",
   SAMBAR, "2.1.1", "http://s3.amazonaws.com/downloads.basho.com/riak/2.1/2.1.1/ubuntu/trusty/riak_2.1.1-1_amd64.deb", CATOS_UBUNTU)))

   private val C13 = ("13-Redis", Appb("redis.png", "Key Value Store", ""),
   List(Plan(FIVE, "Redis is a key-value store which acts as a data structure server with keys containing strings, hashes, lists, sets and sorted sets.",
   SAMBAR, "2.8.4", "https://redis.org", CATOS_UBUNTU)))

   private val C14 = ("14-RabbitMQ", Appb("rabbitmq.png", "Message Broker", ""),
   List(Plan(FIVE, "RabbitMQ is a message broker software  that implements the Advanced Message Queuing Protocol (AMQP).",
   SAMBAR, "3.3.5", "https://www.rabbitmq.com", CATOS_UBUNTU)))

   private val C15 = ("15-Hadoop", BigD("hadoop.png", "Plumbing your big data is easy", ""),
   List(Plan(FIVE, "Apache Hadoop is a set of algorithms (an open-source software framework) for distributed storage and distributed processing of very large data sets (Big Data) on computer clusters built from commodity hardware",
   SAMBAR, "2.6.0", "https://hadoop.apache.org/", CATOS_UBUNTU)))




  val mkMap = Map[String, MarketPlaceInput](
      C1._1  -> MarketPlaceInput(C1._1,  C1._2,  MarketPlacePlans(C1._3),  TORPEDO,     "ubuntu",  ACTIVE),
      C2._1  -> MarketPlaceInput(C2._1,  C2._2,  MarketPlacePlans(C2._3),  TORPEDO,     "coreos",  ACTIVE),
      C3._1  -> MarketPlaceInput(C3._1,  C3._2,  MarketPlacePlans(C3._3),  TORPEDO,     "debian",  ACTIVE),
      C4._1  -> MarketPlaceInput(C4._1,  C4._2,  MarketPlacePlans(C4._3),  TORPEDO,     "centos",  ACTIVE),
      C5._1  -> MarketPlaceInput(C5._1,  C5._2,  MarketPlacePlans(C5._3),  APP,     "java",    ACTIVE),
      C6._1  -> MarketPlaceInput(C6._1,  C6._2,  MarketPlacePlans(C6._3),  APP,     "rails",   ACTIVE),
      C7._1  -> MarketPlaceInput(C7._1,  C7._2,  MarketPlacePlans(C7._3),  APP,     "play",    ACTIVE),
      C8._1  -> MarketPlaceInput(C8._1,  C8._2,  MarketPlacePlans(C8._3),  APP,     "nodejs",  ACTIVE),
   //   C9._1  -> MarketPlaceInput(C9._1,  C9._2,  MarketPlacePlans(C9._3),  ADDON,   "docker",  ACTIVE),
      C10._1 -> MarketPlaceInput(C10._1, C10._2, MarketPlacePlans(C10._3), MICROSERVICES, "containers", ACTIVE),
      C11._1 -> MarketPlaceInput(C11._1, C11._2, MarketPlacePlans(C11._3), SERVICE, "postgresql", ACTIVE),
      C12._1 -> MarketPlaceInput(C12._1, C12._2, MarketPlacePlans(C12._3), SERVICE, "riak",    ACTIVE),
      C13._1 -> MarketPlaceInput(C13._1, C13._2, MarketPlacePlans(C13._3), SERVICE, "redis",   ACTIVE),
      C14._1 -> MarketPlaceInput(C14._1, C14._2, MarketPlacePlans(C14._3), SERVICE, "rabbitmq",ACTIVE),
      C15._1 -> MarketPlaceInput(C15._1, C15._2, MarketPlacePlans(C15._3), SERVICE, "hadoop",  ACTIVE)
    )


}
