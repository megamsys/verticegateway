//cqlsh 103.56.92.24 -f $MEGAM_HOME/verticegateway/vertice.cql

CREATE KEYSPACE IF NOT EXISTS vertice
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }
 AND DURABLE_WRITES = false;


use vertice;

CREATE TABLE accounts( id text,  first_name text, last_name text,  phone text, email text,  api_key text, password text, authority text, password_reset_key text, password_reset_sent_at text, status text, created_at text, PRIMARY KEY (email));


CREATE TABLE assemblies (id text, org_id text, name text, assemblies list<text>, inputs list<text>, json_claz text, created_at text, PRIMARY KEY ((org_id), id));


CREATE TABLE assembly (id text, org_id text, name text, components list<text>, tosca_type text, policies list<text>, inputs list<text>, outputs list<text>, status text, json_claz text, created_at text, PRIMARY KEY ((org_id), id));



CREATE TABLE components (id text, name text, tosca_type text, inputs list<text>, outputs list<text>, envs list<text>, artifacts text, related_components list<text>, operations list<text>, repo text, status text, json_claz text, created_at text, PRIMARY KEY (id));




CREATE TABLE settings( settings_name text, megam_home text, team text, domain text, message_broker map <text, text>, nilavu map <text, text>, api map <text, text>, flavors map <text, text>, PRIMARY KEY(settings_name));



CREATE  TABLE organizations(id text, accounts_id text, name text, json_claz text, created_at  text, PRIMARY KEY ((accounts_id), id)  );





CREATE TABLE domains (id text, org_id text,  name text, json_claz text, created_at text, PRIMARY KEY((org_id), name));




CREATE TABLE sshkeys (id text, org_id text, name text, privatekey text, publickey text, json_claz text, created_at text, PRIMARY KEY((org_id), name));

CREATE TABLE requests( id text, cat_id text, cat_type text,  name text,  action text, category text, created_at text, PRIMARY KEY (id));

CREATE TABLE marketplaces ( settings_name text,  cattype text, flavor text,  image text, catorder text, url text, json_claz text, envs map <text, text>,  plans map <text, text>,  PRIMARY KEY ((settings_name), flavor) );

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'TORPEDO', 'Ubuntu', 'ubuntu.png', '1', 'http://ubuntu.com', 'Megam::MarketPlace' , {}, {'14.04':'Ubuntu 14.04 LTS (Trusty) is delivers the best value scale-out performance available.'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'TORPEDO', 'CoreOs', 'coreos.png', '1', 'http://coreos.com', 'Megam::MarketPlace' , {}, {' 633.1.0': 'CoreOS provides no package manager as a way for the distribution of applications, requiring instead all applications to run inside their containers'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'TORPEDO', 'Debian', 'debian.png', '1', 'http://debian.com', 'Megam::MarketPlace' , {}, {'8.0':'Debian is a free operating system (OS) for your computer. Jessie is the codename for 8.0 Debian.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'TORPEDO', 'CentOs', 'centos.png', '1', 'http://debian.com', 'Megam::MarketPlace' , {}, {'7.1':'CentOS is an Enterprise-class Linux Distribution derived from sources freely provided to the public by Red Hat.'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'APP', 'Java', 'java.png', '2', 'https://github.com/megamsys/java-spring-petclinic.git', 'Megam::MarketPlace' , {'port':'8080', 'tomcat_username':'megam', 'tomcat_password':'megam'}, {'8.x':'Quickly get started with J2EE Spring framework app.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'APP', 'Rails', 'rails.png', '2', 'https://github.com/megamsys/aryabhata.git', 'Megam::MarketPlace' , {}, {'4.x':'Quickly get started with rails 4.x app.'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'APP', 'Play', 'play.png', '2', 'https://github.com/megamsys/modern-web-template.git', 'Megam::MarketPlace' , {'port':'9000'}, {'2.4.x':'Build robust RESTful API server using Scala.'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'APP', 'Nodejs', 'nodejs.png', '2', 'https://github.com/megamsys/etherpad-lite.git', 'Megam::MarketPlace' , {}, {'0.12.x':'Build fast, scalable, and incredibly efficient realtime app in second.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'APP', 'PHP', 'php.png', '2', 'https://secure.php.net/', 'Megam::MarketPlace' , {'port':'8080'}, {'5.x':'PHP is a server-side scripting language designed for web development but also used as a general-purpose programming language.'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'APP', 'CCPlus', 'ccplus.png', '2', 'https://www.bell-labs.com/', 'Megam::MarketPlace' , {'port' : '80'}, {'4.9': 'C/C++ IDE for working with C/C++ programming.'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'APP', 'Python', 'python.png', '2', 'https://www.python.org/', 'Megam::MarketPlace' , {'port':'80'}, {'3.4.2':'Python is object-oriented, imperative and functional programming language.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'SERVICE', 'Apache', 'apache.png', '3', 'http://www.apache.org/', 'Megam::MarketPlace' , {'port':'80'}, {'2.4.18':'Apache Server is an open-source HTTP server for modern operating systems'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'SERVICE', 'CouchDb', 'couchdb.png', '3', 'http://couchdb.apache.org/', 'Megam::MarketPlace' , {'port' : '5984', 'username' : 'couchdb'}, {'1.6.1':'CouchDB is a powerful, open source document-oriented database system.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'SERVICE', 'PostgreSQL', 'postgres.png', '3', 'https://postgresql.org', 'Megam::MarketPlace' , {'port':'5432', 'username':'postgres', 'password':'megam'}, {'9.3':'PostgreSQL is a powerful, open source object-relational database system.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'SERVICE', 'Riak', 'riak.png', '3', 'http://s3.amazonaws.com/downloads.basho.com/riak/2.1/2.1.1/ubuntu/trusty/riak_2.1.1-1_amd64.deb', 'Megam::MarketPlace' , {'port':'8098'}, {'2.1.1.':'PostgreSQL is a powerful, open source object-relational database system.'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'SERVICE', 'Redis', 'redis.png', '3', 'https://redis.org', 'Megam::MarketPlace' , {'port':'6379'}, {'2.8.4':'Redis is a key-value store which acts as a data structure server with keys containing strings, hashes, lists, sets and sorted sets.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'SERVICE', 'RabbitMQ', 'rabbitmq.png', '3', 'https://www.rabbitmq.com', 'Megam::MarketPlace' , {'port':'5672', 'username':'guest', 'password':'guest'}, {'3.3.x':'RabbitMQ is a message broker software  that implements the Advanced Message Queuing Protocol (AMQP).'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'SERVICE', 'Mysql', 'mysql.png', '3', 'https://dev.mysql.com/', 'Megam::MarketPlace' , {'port':'3306', 'username':'root', 'password':'megam'}, {'5.x':'MySQL is a freely available open source Relational Database Management System (RDBMS) that uses Structured Query Language (SQL).'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'MICROSERVICES', 'DockerBox', 'dockerbox.png', '4', 'https://www.docker.com', 'Megam::MarketPlace' , {'port':'2375'}, {'0.9.0':'Launch a vm with docker in detached model.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'MICROSERVICES', 'DockerContainer',  'dockercontainer.png', '4', 'https://www.docker.com', 'Megam::MarketPlace' , {}, {'1.8.x':'Docker that automates the deployment of applications inside software containers.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'ANALYTICS', 'Hadoop',  'hadoop.png', '5', 'https://hadoop.apache.org', 'Megam::MarketPlace' , {'port':'50070'}, {'2.6.0':'Apache Hadoop is a set of algorithms (an open-source software framework) for distributed storage and distributed processing of very large data sets (Big Data) on computer clusters built from commodity hardware.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'ANALYTICS', 'Spark', 'spark.png', '5', 'http://spark.apache.org/', 'Megam::MarketPlace' , {'port':'9004'}, {'1.5.0':'Apache Spark is a fast and general-purpose cluster computing system. It provides high-level APIs in Java, Scala, Python and R, and an optimized engine that supports general execution graphs.'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'ANALYTICS', 'SparkNotebook', 'sparknotebook.png', '5', 'http://spark-notebook.io', 'Megam::MarketPlace' , {'port':'9000'}, {'0.6.x':'Use Apache Spark straight from the Browser. '});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'COLLABORATION', 'Zarafa', 'zarafa.png', '6', 'https://www.zarafa.com/', 'Megam::MarketPlace' , {'port':'9000'}, {'7.2':'Zarafa gives you the tools that help boost your day-to-day communication. File sharing, web meetings and business apps like email and calendar are integrated in one suite.'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'COLLABORATION', 'Owncloud', 'owncloud.png', '6', 'https://owncloud.org/', 'Megam::MarketPlace' , {'username':'admin', 'password':'admin'}, {'8.1':'Access your data from all your devices, on an open platform you can extend and modify.'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'COLLABORATION', ' VirtualOffice',  'virtual_office.png', '6', 'www.megam.io', 'Megam::MarketPlace' , {'host':'', 'port':'8069'}, {'0.9.0':'A virtual office provides bundled applications for everyday office use.'});
