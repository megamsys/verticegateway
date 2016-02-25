CREATE KEYSPACE IF NOT EXISTS TestMegam
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 }
 AND DURABLE_WRITES = false;

use TestMegam;


-------------------------------Accounts-------------------------------------
CREATE TABLE Account( id int,  first_name text, last_name text,  phone int, email text,  api_key text, password text, authority text, password_reset_key text, status text, created_at timestamp, PRIMARY KEY (email));

-----------------------------------Assemblies-------------------------------------------

CREATE TABLE assemblies (id text, org_id text, name text, assemblies list<text>, inputs list<text>, json_claz text, created_at text, PRIMARY KEY ((org_id), id));

-----------------------------------Assembly----------------------------

CREATE TABLE assembly (id text, org_id text, name text, components list<text>, tosca_type text, policies list<text>, inputs list<text>, outputs list<text>, status text, json_claz text, created_at text, PRIMARY KEY ((org_id), id));

 ---------------------------------Components---------------------------------------------

CREATE TABLE components (id text, name text, tosca_type text, inputs list<text>, outputs list<text>, envs list<text>, artifacts text, related_components list<text>, operations list<text>, repo text, status text, json_claz text, created_at text, PRIMARY KEY (id));

-----------------------------------settings-----------------------------------------------


CREATE TABLE settings( settings_name text, megam_home text, team text, domain text, message_broker map <text, text>, nilavu map <text, text>, api map <text, text>, flavors map <text, text>, PRIMARY KEY(settings_name));

 ---------------------------------organization--------------------------------------------

CREATE  TABLE organizations(id text, accounts_id text, name text, json_claz text, created_at  text, PRIMARY KEY ((accounts_id), id)  );


-----------------------------------Domain-------------------------------------------------


CREATE TABLE domains (id text, org_id text,  name text, json_claz text, created_at text, PRIMARY KEY((org_id), name));


-----------------------------------sshkeys---------------------------------------------------

CREATE TABLE sshkeys (id text, org_id text, name text, privatekey text, publickey text, json_claz text, created_at text, PRIMARY KEY((org_id), name));

 -------------------------------- marketplaces----------------------------------------------

CREATE TABLE marketplaces ( settings_name text,  cattype text, flavor text,  image text, url text, json_claz text, envs map <text, text>,  plan map <text, text>,  PRIMARY KEY ((settings_name), flavor) );

insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'TORPEDO', 'Ubuntu', 'ubuntu.png', 'http://ubuntu.com', 'Megam::MarketPlace' , {}, {'14.04':'Ubuntu 14.04 LTS (Trusty) is delivers the best value scale-out performance available.'});


insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'TORPEDO', 'CoreOs', 'coreos.png', 'http://coreos.com', 'MEGAM::MarketPlace' , {}, {' 633.1.0': 'CoreOS provides no package manager as a way for the distribution of applications, requiring instead all applications to run inside their containers'});

insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'TORPEDO', 'Debian', 'debian.png', 'http://debian.com', 'MEGAM::MarketPlace' , {}, {'8.0':'Debian is a free operating system (OS) for your computer. Jessie is the codename for 8.0 Debian.'});

insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'TORPEDO', 'CentOs', 'centos.png', 'http://debian.com', 'MEGAM::MarketPlace' , {}, {'7.1':'CentOS is an Enterprise-class Linux Distribution derived from sources freely provided to the public by Red Hat.'});


insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'APP', 'Java', 'java.png', 'https://github.com/megamsys/java-spring-petclinic.git', 'MEGAM::MarketPlace' , {'port':'8080', 'tomcat_username':'megam', 'tomcat_password':'megam'}, {'8.x':'Quickly get started with J2EE Spring framework app.'});

insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'APP', 'Rails', 'rails.png', 'https://github.com/megamsys/aryabhata.git', 'MEGAM::MarketPlace' , {}, {'4.x':'Quickly get started with rails 4.x app.'});


insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'APP', 'Play', 'play.png', 'https://github.com/megamsys/modern-web-template.git', 'MEGAM::MarketPlace' , {'port':'9000'}, {'2.4.x':'Build robust RESTful API server using Scala.'});


insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'APP', 'Nodejs', 'nodejs.png', 'https://github.com/megamsys/etherpad-lite.git', 'MEGAM::MarketPlace' , {}, {'0.12.x':'Build fast, scalable, and incredibly efficient realtime app in second.'});

insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'APP', 'PHP', 'php.png', 'https://secure.php.net/', 'MEGAM::MarketPlace' , {'port':'8080'}, {'5.x':'PHP is a server-side scripting language designed for web development but also used as a general-purpose programming language.'});


insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'APP', 'CCPlus', 'ccplus.png', 'https://www.bell-labs.com/', 'MEGAM::MarketPlace' , {'port' : '80'}, {'4.9': 'C/C++ IDE for working with C/C++ programming.'});


insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'APP', 'Python', 'python.png', 'https://www.python.org/', 'MEGAM::MarketPlace' , {'port':'80'}, {'3.4.2':'Python is object-oriented, imperative and functional programming language.'});

insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'APP', 'Apache', 'apache.png', 'http://www.apache.org/', 'MEGAM::MarketPlace' , {'port':'80'}, {'2.4.18':'Apache Server is an open-source HTTP server for modern operating systems'});


insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'SERVICE', 'CouchDb', 'couchdb.png', 'http://couchdb.apache.org/', 'MEGAM::MarketPlace' , {'port' : '5984', 'username' : 'couchdb'}, {'1.6.1':'CouchDB is a powerful, open source document-oriented database system.'});

insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'SERVICE', 'PostgreSQL', 'postgres.png', 'https://postgresql.org', 'MEGAM::MarketPlace' , {'port':'5432', 'username':'postgres', 'password':'megam'}, {'9.3':'PostgreSQL is a powerful, open source object-relational database system.'});

insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'SERVICE', 'Riak', 'riak.png', 'http://s3.amazonaws.com/downloads.basho.com/riak/2.1/2.1.1/ubuntu/trusty/riak_2.1.1-1_amd64.deb', 'MEGAM::MarketPlace' , {'port':'8098'}, {'2.1.1.':'PostgreSQL is a powerful, open source object-relational database system.'});


insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'SERVICE', 'Redis', 'redis.png', 'https://redis.org', 'MEGAM::MarketPlace' , {'port':'6379'}, {'2.8.4':'Redis is a key-value store which acts as a data structure server with keys containing strings, hashes, lists, sets and sorted sets.'});

insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'SERVICE', 'RabbitMQ', 'rabbitmq.png', 'https://www.rabbitmq.com', 'MEGAM::MarketPlace' , {'port':'5672', 'username':'guest', 'password':'guest'}, {'3.3.x':'RabbitMQ is a message broker software  that implements the Advanced Message Queuing Protocol (AMQP).'});

insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'SERVICE', 'Mysql', 'mysql.png', 'https://dev.mysql.com/', 'MEGAM::MarketPlace' , {'port':'3306', 'username':'root', 'password':'megam'}, {'5.x':MySQL is a freely available open source Relational Database Management System (RDBMS) that uses Structured Query Language (SQL).'});


insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'MICROSERVICES', 'DockerBox', 'dockerbox.png', 'https://www.docker.com', 'MEGAM::MarketPlace' , {'port':'2375'}, {'0.9.0':'Launch a vm with docker in detached model.'});

insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'MICROSERVICES', 'DockerContainer',  'dockercontainer.png', 'https://www.docker.com', 'MEGAM::MarketPlace' , {}, {'1.8.x':'Docker that automates the deployment of applications inside software containers.'});

insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'ANALYTICS', 'Hadoop',  'hadoop.png', 'https://hadoop.apache.org', 'MEGAM::MarketPlace' , {'port':'50070'}, {'2.6.0':'Apache Hadoop is a set of algorithms (an open-source software framework) for distributed storage and distributed processing of very large data sets (Big Data) on computer clusters built from commodity hardware.'});

insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'ANALYTICS', 'Spark', 'spark.png', 'http://spark.apache.org/', 'MEGAM::MarketPlace' , {'port':'9004'}, {'1.5.0':'Apache Spark is a fast and general-purpose cluster computing system. It provides high-level APIs in Java, Scala, Python and R, and an optimized engine that supports general execution graphs.'});


insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'ANALYTICS', 'SparkNotebook', 'sparknotebook.png', 'http://spark-notebook.io', 'MEGAM::MarketPlace' , {'port':'9000'}, {'0.6.x':'Use Apache Spark straight from the Browser. '});

insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'COLLABORATION', 'Zarafa', 'zarafa.png', 'https://www.zarafa.com/', 'MEGAM::MarketPlace' , {'port':'9000'}, {'7.2':'Zarafa gives you the tools that help boost your day-to-day communication. File sharing, web meetings and business apps like email and calendar are integrated in one suite.'});


insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'COLLABORATION', 'Owncloud', 'owncloud.png', 'https://owncloud.org/', 'MEGAM::MarketPlace' , {'username':'admin', 'password':'admin'}, {'8.1':'Access your data from all your devices, on an open platform you can extend and modify.'});


insert into marketplaces (settings_name, cattype, flavor, image, url, json_claz, envs, plan) values ('vertice', 'COLLABORATION', ' VirtualOffice',  'virtual_office.png', 'www.megam.io', 'MEGAM::MarketPlace' , {'host':'', 'port':'8069'}, {'0.9.0':'A virtual office provides bundled applications for everyday office use.'});
